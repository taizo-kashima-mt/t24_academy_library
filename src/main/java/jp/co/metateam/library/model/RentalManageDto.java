package jp.co.metateam.library.model;

import java.sql.Timestamp;
/* import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId; */
import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jp.co.metateam.library.values.RentalStatus;
import lombok.Getter;
import lombok.Setter;

/* import jp.co.metateam.library.service.RentalManageService;
import jp.co.metateam.library.service.StockService; */


/**
 * 貸出管理DTO
 */
@Getter
@Setter
public class RentalManageDto {

    private Long id;

    @NotEmpty(message="在庫管理番号は必須です")
    private String stockId;

    @NotEmpty(message="社員番号は必須です")
    private String employeeId;

    @NotNull(message="貸出ステータスは必須です")
    private Integer status;

    @DateTimeFormat(pattern="yyyy-MM-dd")
    @NotNull(message="貸出予定日は必須です")
    private Date expectedRentalOn;

    @DateTimeFormat(pattern="yyyy-MM-dd")
    @NotNull(message="返却予定日は必須です")
    private Date expectedReturnOn;

    private Timestamp rentaledAt;

    private Timestamp returnedAt;

    private Timestamp canceledAt;

    private Stock stock;

    private Account account;

    ////加えました（5/13）
    private Integer newStatus;  

    // getStatusメソッドを追加
    public Integer getStatus() {
        return this.status;
    }

    // getNewStatusメソッドを追加
    public Integer getNewStatus() {
        return this.newStatus;
    }    
    ////ここまでです

    ////加えました（5/14） 
        // 貸出状態が有効かどうかをチェックするメソッド
            public String isValidStatus(Integer previousStatus) {
                if(previousStatus == RentalStatus.RENT_WAIT.getValue() && this.status == RentalStatus.RETURNED.getValue()){
                    return "貸出ステータスは「貸出待ち」から「返却済み」に変更できません";
                }else if(previousStatus == RentalStatus.RENTAlING.getValue() && this.status == RentalStatus.RENT_WAIT.getValue()){
                    return "貸出ステータスは「貸出中」から「貸出待ち」に変更できません";
                }else if(previousStatus == RentalStatus.RENTAlING.getValue() && this.status == RentalStatus.CANCELED.getValue()){
                    return "貸出ステータスは「貸出中」から「キャンセル」に変更できません";
                }else if(previousStatus == RentalStatus.RETURNED.getValue() && this.status == RentalStatus.RENT_WAIT.getValue()){
                    return "貸出ステータスは「返却済み」から「貸出待ち」に変更できません";
                }else if(previousStatus == RentalStatus.RETURNED.getValue() && this.status == RentalStatus.RENTAlING.getValue()){
                     return "貸出ステータスは「返却済み」から「貸出中」に変更できません";
                }else if(previousStatus == RentalStatus.RETURNED.getValue() && this.status == RentalStatus.CANCELED.getValue()){
                    return "貸出ステータスは「返却済み」から「キャンセル」に変更できません";
                }else if(previousStatus == RentalStatus.CANCELED.getValue() && this.status == RentalStatus.RENT_WAIT.getValue()){
                    return "貸出ステータスは「キャンセル」から「貸出待ち」に変更できません";
                }else if(previousStatus == RentalStatus.CANCELED.getValue() && this.status == RentalStatus.RENTAlING.getValue()){
                    return "貸出ステータスは「キャンセル」から「貸出中」に変更できません";
                }else if(previousStatus == RentalStatus.CANCELED.getValue() && this.status == RentalStatus.RETURNED.getValue()){
                    return "貸出ステータスは「キャンセル」から「返却済み」に変更できません";
                }
                return null;
            }

            //Controller　→　Dto移行用
/*             //利用可日チェック
            private StockService stockService;
            public String checkInventoryStatus(String id) {
                // 在庫ステータスを確認するロジックを記述
                Stock stock = stockService.findById(id);
                    if (stock.getStatus() == 0) {
                        return null; // 利用可の場合はエラーメッセージなし
                    } else {
                        return "この本は利用できません"; // 利用不可の場合はエラーメッセージを返す
                    }
            }

            //貸出可否チェック（登録時）
            public String rentalCheck(RentalManageService rentalManageService,RentalManageDto rentalManageDto, String id) {
                List<RentalManage> rentalAvailable = rentalManageService.findByStockIdAndStatusIn(id);
                if (rentalAvailable != null) {
                    // ループ処理
                    for (RentalManage rentalManage : rentalAvailable) {
                        if (rentalManage.getExpectedReturnOn().after(rentalManageDto.getExpectedRentalOn())
                                && rentalManage.getExpectedRentalOn().before(rentalManageDto.getExpectedReturnOn())) {
                            return "貸出期間が重複しています";
                        }
                    }
                    return null;
                } else {
                    return null;
                }
            }

            //貸出可否チェック(更新時)
            public String rentalCheck(RentalManageService rentalManageService, RentalManageDto rentalManageDto, String id, Long rentalId) {
                List<RentalManage> rentalAvailable = rentalManageService.findByStockIdAndStatusIn(id, rentalId);
                if (rentalAvailable != null) {
                // ループ処理
                    for (RentalManage rentalManage : rentalAvailable) {
                        if (rentalManage.getExpectedReturnOn().after(rentalManageDto.getExpectedRentalOn())
                        && rentalManage.getExpectedRentalOn().before(rentalManageDto.getExpectedReturnOn())) {
                        return "貸出期間が重複しています";
                    }
                }
                return null;
            } else {
                return null;
            }
    }
    
            //貸出ステータス変更時の日付チェック
            public String isValidDate(RentalManageDto rentalManageDto, RentalManage rentalManage) {
                LocalDate currentDate = LocalDate.now(ZoneId.of("Asia/Tokyo")); // 現在の日付を取得。おそらく貸出開始日のタイムゾーンがずれている
            
                // rentalDateとreturnDateをLocalDateに変換
                LocalDate rentalDate = rentalManageDto.getExpectedRentalOn().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDate returnDate = rentalManageDto.getExpectedReturnOn().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            
                Integer oldStatus = rentalManage.getStatus();
                Integer newStatus = rentalManageDto.getStatus();
            
                if (oldStatus == 0 && newStatus == 1) {
                    if (!rentalDate.equals(currentDate)) {
                        return "貸出予定日は現在の日付で登録して下さい";
                    }
                }
                if (oldStatus == 1 && newStatus == 2) {
                    if (!returnDate.equals(currentDate)) {
                        return "返却予定日は現在の日付で登録して下さい";
                    }
                }
                if (rentalDate.isAfter(returnDate)) {
                    return "貸出予定日は返却予定日よりも前に設定してください";
                }
                return null;
            } */
}
