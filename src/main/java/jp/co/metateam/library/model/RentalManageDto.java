package jp.co.metateam.library.model;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jp.co.metateam.library.values.RentalStatus;
import lombok.Getter;
import lombok.Setter;

import jp.co.metateam.library.service.RentalManageService;
import java.util.List;

//import jp.co.metateam.library.service.StockService;

/**
 * 貸出管理DTO
 */
@Getter
@Setter
public class RentalManageDto {

    private Long id;

    @NotEmpty(message = "在庫管理番号は必須です")
    private String stockId;

    @NotEmpty(message = "社員番号は必須です")
    private String employeeId;

    @NotNull(message = "貸出ステータスは必須です")
    private Integer status;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "貸出予定日は必須です")
    private Date expectedRentalOn;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "返却予定日は必須です")
    private Date expectedReturnOn;

    private Timestamp rentaledAt;

    private Timestamp returnedAt;

    private Timestamp canceledAt;

    private Stock stock;

    private Account account;

    //// 加えました（5/13）
    private Integer newStatus;

    // getStatusメソッドを追加
    public Integer getStatus() {
        return this.status;
    }

    // getNewStatusメソッドを追加
    public Integer getNewStatus() {
        return this.newStatus;
    }
    //// ここまで

    //// 加えました（5/14）
    // 貸出状態が有効かどうかをチェックするメソッド
    public String isValidStatus(Integer previousStatus) {
        if (previousStatus == RentalStatus.RENT_WAIT.getValue() && this.status == RentalStatus.RETURNED.getValue()) {
            return "貸出ステータスは「貸出待ち」から「返却済み」に変更できません";
        } else if (previousStatus == RentalStatus.RENTAlING.getValue()
                && this.status == RentalStatus.RENT_WAIT.getValue()) {
            return "貸出ステータスは「貸出中」から「貸出待ち」に変更できません";
        } else if (previousStatus == RentalStatus.RENTAlING.getValue()
                && this.status == RentalStatus.CANCELED.getValue()) {
            return "貸出ステータスは「貸出中」から「キャンセル」に変更できません";
        } else if (previousStatus == RentalStatus.RETURNED.getValue()
                && this.status == RentalStatus.RENT_WAIT.getValue()) {
            return "貸出ステータスは「返却済み」から「貸出待ち」に変更できません";
        } else if (previousStatus == RentalStatus.RETURNED.getValue()
                && this.status == RentalStatus.RENTAlING.getValue()) {
            return "貸出ステータスは「返却済み」から「貸出中」に変更できません";
        } else if (previousStatus == RentalStatus.RETURNED.getValue()
                && this.status == RentalStatus.CANCELED.getValue()) {
            return "貸出ステータスは「返却済み」から「キャンセル」に変更できません";
        } else if (previousStatus == RentalStatus.CANCELED.getValue()
                && this.status == RentalStatus.RENT_WAIT.getValue()) {
            return "貸出ステータスは「キャンセル」から「貸出待ち」に変更できません";
        } else if (previousStatus == RentalStatus.CANCELED.getValue()
                && this.status == RentalStatus.RENTAlING.getValue()) {
            return "貸出ステータスは「キャンセル」から「貸出中」に変更できません";
        } else if (previousStatus == RentalStatus.CANCELED.getValue()
                && this.status == RentalStatus.RETURNED.getValue()) {
            return "貸出ステータスは「キャンセル」から「返却済み」に変更できません";
        }
        return null;
    }

    // 貸出可否チェック（登録時）
    public String rentalCheck(RentalManageService rentalManageService, RentalManageDto rentalManageDto, String id) {
        List<RentalManage> rentalAvailable = rentalManageService.findByStockIdAndStatusIn(id);
        if (rentalAvailable != null) {
            // ループ処理
            for (RentalManage rentalManage : rentalAvailable) {
                if (rentalManage.getExpectedReturnOn().after(rentalManageDto.getExpectedRentalOn())
                        &&
                        rentalManage.getExpectedRentalOn().before(rentalManageDto.getExpectedReturnOn())) {
                    return "貸出期間が重複しています";
                }
            }
            return null;
        } else {
            return null;
        }
    }

    // 貸出可否チェック(更新時)
    public String rentalCheck(RentalManageService rentalManageService,
            RentalManageDto rentalManageDto, String id, Long rentalId) {
        List<RentalManage> rentalAvailable = rentalManageService.findByStockIdAndStatusIn(id, rentalId);
        if (rentalAvailable != null) {
            // ループ処理
            for (RentalManage rentalManage : rentalAvailable) {
                if (rentalManage.getExpectedReturnOn().after(rentalManageDto.getExpectedRentalOn())
                        &&
                        rentalManage.getExpectedRentalOn().before(rentalManageDto.getExpectedReturnOn())) {
                    return "貸出期間が重複しています";
                }
            }
            return null;
        } else {
            return null;
        }
    }

    // 貸出ステータス変更時の日付チェック
    public String isValidDate(RentalManageDto rentalManageDto, RentalManage rentalManage) {
        LocalDate currentDate = LocalDate.now(ZoneId.of("Asia/Tokyo")); // 現在の日付を取得。
        // rentalDateとreturnDateをLocalDateに変換
        LocalDate rentalDate = rentalManageDto.getExpectedRentalOn().toInstant().atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate returnDate = rentalManageDto.getExpectedReturnOn().toInstant().atZone(ZoneId.systemDefault())
                .toLocalDate();

        Integer oldStatus = rentalManage.getStatus();
        Integer newStatus = rentalManageDto.getStatus();

        if (oldStatus == RentalStatus.RENT_WAIT.getValue() && newStatus == RentalStatus.RENTAlING.getValue()) {
            if (!rentalDate.equals(currentDate)) {
                return "貸出予定日は現在の日付で登録して下さい";
            }
        }
        if (oldStatus == RentalStatus.RENTAlING.getValue() && newStatus == RentalStatus.RETURNED.getValue()) {
            if (!returnDate.equals(currentDate)) {
                return "返却予定日は現在の日付で登録して下さい";
            }
        }
        if (rentalDate.isAfter(returnDate)) {
            return "貸出予定日は返却予定日よりも前に設定してください";
        }
        if (rentalDate.isBefore(currentDate)) {
            return "過去の日付を貸出予定日にすることはできません";
        }
        return null;
    }

    // 貸出予定日のチェック（登録時）（6/11）
    public String isValidRentalDate(RentalManageDto rentalManageDto) {
        LocalDate currentDate = LocalDate.now(ZoneId.of("Asia/Tokyo"));
        LocalDate rentalDate = rentalManageDto.getExpectedRentalOn().toInstant().atZone(ZoneId.systemDefault())
                .toLocalDate();
        if (rentalDate.isBefore(currentDate)) {
            return "過去の日付を貸出予定日にすることができません";
        }
        return null;
    }

    // 予定日フォーマットチェック
    public List<String> formatCheck(RentalManageDto rentalManageDto) {
        // 本来「\d」だが、「\」は改行のエスケープシーケンスのため、Javaの文字列リテラル内で \ を表現するには \\ と書く必要がある。
        String pattern = "\\d{4}-\\d{2}-\\d{2}";
        // 文字列変換の型を定義。yyyy-MM-ddの形式で日付を文字列に。
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        // 複数表示のためエラーリスト定義
        List<String> errors = new ArrayList<>();
        // String型の貸出予定日・返却予定日を定義
        String expectedRentalOnStr = sdf.format(rentalManageDto.getExpectedRentalOn());
        String expectedReturnOnStr = sdf.format(rentalManageDto.getExpectedReturnOn());

        // フォーマットチェック
        if (!expectedRentalOnStr.matches(pattern)) {
            errors.add("貸出予定日はyyyy-MM-ddで入力してください");
        }
        if (!expectedReturnOnStr.matches(pattern)) {
            errors.add("返却予定日はyyyy-MM-ddで入力してください");
        }

        return errors;
    }

}
