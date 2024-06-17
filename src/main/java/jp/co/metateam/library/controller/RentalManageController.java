package jp.co.metateam.library.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jp.co.metateam.library.service.AccountService;
import jp.co.metateam.library.service.RentalManageService;
import jp.co.metateam.library.service.StockService;
import lombok.extern.log4j.Log4j2;
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.RentalManageDto;

import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.values.RentalStatus;
import jp.co.metateam.library.values.StockStatus;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;

//追加インポート
import org.springframework.web.bind.annotation.RequestParam;//(5/14)

/**
 * 貸出管理関連クラスß
 */
@Log4j2
@Controller
public class RentalManageController {

    // final修飾子で変数が初期化された後の変更を拒否
    // フィールド宣言（AccountServiceなど）をして変数を定義（accountService）
    private final AccountService accountService;
    private final RentalManageService rentalManageService;
    private final StockService stockService;

    // インスタンス生成の際に呼び出されるコンストラクタの定義
    /*
     * AutowiredによってSpringFrameworkがDI（Dependency Injection）を行う。
     * DIによって、クラス間の依存関係を明示的にせずに、それらの依存関係を容易に変更したりすることができる。
     * 要は下記3つのインスタンスを、具体的な実装関係なく参照することができる。
     * →参照するクラスの実装を変えても、このクラスのコードは変えなくてもいい。柔軟性の高いコーディング。
     */
    @Autowired
    public RentalManageController(
            // AccountServiceというクラスのaccountServiceというインスタンスを参照。
            AccountService accountService,
            RentalManageService rentalManageService,
            StockService stockService) {
        // コンストラクターで受け取ったaccountServiceをRentalManageControllerクラスのインスタンス変数に格納する。
        // this.accountService→このクラスでの変数accountServiceは、右辺のaccountService(上で参照したのと同じ)と同じものです、と定義。
        this.accountService = accountService;
        this.rentalManageService = rentalManageService;
        this.stockService = stockService;
    }

    /**
     * 貸出一覧画面初期表示
     * 
     * @param model //Modelオブジェクトが引数として渡されています。SpringMVCが提供するモデルオブジェクト。
     * @return //メソッドが返す値や戻り値に関する情報を提供するために使用される。つまりどういうこと？
     */
    // 「/rental/index」というURI(貸出一覧画面)へのGETリクエストがこのメソッドに送信されると、そのリクエストを処理するためにこのメソッドが呼び出される。
    @GetMapping("/rental/index")
    // メソッド名がindex、引数名がmodel。
    public String index(Model model) {
        // 貸出管理テーブルから全件取得
        // 取得した貸出管理情報は、変数RentalManageList に代入されており、この変数には全ての貸出管理情報が含まれる。
        // this.rentalManageServiceサービスについては上述
        List<RentalManage> RentalManageList = this.rentalManageService.findAll();
        // 貸出一覧画面に渡すデータをmodelに追加
        // addAttributeは属性追加のメソッド。()内に名前と、属性として追加する変数（一行上で定義）を記述する。
        model.addAttribute("rentalManageList", RentalManageList);
        // 貸出一覧画面に遷移
        // rental/indexを返している。引数が文字列だが、これはパス名を指している。
        // この行を使うことで、このメソッドが実行された後に、指定されたビュー(rental/index)に遷移することができる。
        return "rental/index";
    }

    // Springのアノテーションを使用。GETリクエストが "/rental/add" というURLに対応。
    // アノテーション…
    @GetMapping("/rental/add")
    public String add(Model model,
            @RequestParam(value = "stockIdAvailebleRentalIndex1", required = false) String stockIdAvailebleRentalIndex1,
            @RequestParam(value = "currentDate", required = false) LocalDate currentDate) {
        List<Stock> stockList = this.stockService.findAll();
        List<Account> accounts = this.accountService.findAll();
        Stock stock = this.stockService.findById(stockIdAvailebleRentalIndex1);

        if (currentDate != null) {
            Date currentDateAsDate = Date.from(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            model.addAttribute("currentDate", currentDateAsDate);
            model.addAttribute("stock", stock);
        }

        model.addAttribute("accounts", accounts);
        model.addAttribute("stockList", stockList);
        model.addAttribute("rentalStatus", RentalStatus.values());

        if (!model.containsAttribute("rentalManageDto")) {
            model.addAttribute("rentalManageDto", new RentalManageDto());
        }

        return "rental/add";
    }

    // POSTリクエストが "/rental/add" というURLに対応。
    @PostMapping("/rental/add")
    /*
     * HTTP POSTリクエストが "/rental/add" に送信されたときに呼び出される。
     * > @Validアノテーションは、rentalManageDtoオブジェクトのバリデーションを有効にする
     * > BindingResultオブジェクトはバインディング結果を保持する。
     * > @ModelAttributeアノテーションでは、メソッドの引数で、RentalManageDtoという名前のクラスのオブジェクトが渡される。
     * > BindingResult resultは、 バリデーションの結果を保持するオブジェクト
     * > RedirectAttributes raは、リダイレクト先のページに情報を渡すためのオブジェクト
     * > Model modelは、データをビューに渡すためのオブジェクト
     */
    public String save(@Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result,
            RedirectAttributes ra, Model model) {
        try {// 例外がスローされたらcatchに行く

            // バリデーションチェック
            if (result.hasErrors()) {// もしバリデーションエラーが発生したら
                throw new Exception("Validation error.");// Validation errorというメッセージを持つ新しいExceptionオブジェクトが作成・スロー
            }
            // 貸出予定日フォーマットチェック
            List<String> formatErrors = rentalManageDto.formatCheck(rentalManageDto);// ListをStringで定義して、formatCheckに登録データ(rentalManageDto)を引数で渡す
            if (!formatErrors.isEmpty()) {// もし、formatErrorsにエラーメッセージが返されていたら
                for (String error : formatErrors) {// 拡張for文：formatErrorsリスト内の各要素(今回は文字列)をerrorというループ時に使用する変数に順番に代入して処理
                    if (error.equals("貸出予定日はyyyy-MM-ddで入力してください")) {// 等号演算子は、そのオブジェクトが参照しているもので比較する。ある変数の参照が“あ”で他の変数が新しく文字列オブジェクトを作成し、その上で“あ”と定義したとき、等号演算子ではfalseになる。よってequalsで文字を比較してあげる
                        result.addError(new FieldError("rentalManageDto", "expectedRentalOn", error));// FieldErrorオブジェクトが生成され、resultオブジェクトに追加
                    } else if (error.equals("返却予定日はyyyy-MM-ddで入力してください")) {
                        result.addError(new FieldError("rentalManageDto", "expectedReturnOn", error));
                    }
                }
                addCommonAttributes(model);
                return "rental/add";// redirectでは新しいURLを提供する。よって、ページ遷移がない場合はreturnの方がいい
            }
            // 貸出予定日のチェック（6/11）
            String rentalDateError = rentalManageDto.isValidRentalDate(rentalManageDto);
            if (rentalDateError != null) {
                result.addError(new FieldError("rentalManageDto", "expectedRentalOn", rentalDateError));
                addCommonAttributes(model);
                return "rental/add";
            }

            // 利用可否チェック 追加（5/17）
            String errorMessage = this.checkInventoryStatus(rentalManageDto.getStockId());
            if (errorMessage != null) {
                result.addError(new FieldError("rentalManageDto", "stockId", errorMessage));
                addCommonAttributes(model);
                return "rental/add";
            }
            // 貸出可否チェック 追加（5/16）
            String DateError = rentalManageDto.rentalCheck(rentalManageService, rentalManageDto,
                    rentalManageDto.getStockId());
            if (DateError != null) {
                // rentalManageDtoからexpectedRentalOnとexpectedReturnOnの値を取得して、FieldErrorオブジェクトに追加する
                result.addError(new FieldError("rentalManageDto", "expectedRentalOn", DateError));
                result.addError(new FieldError("rentalManageDto", "expectedReturnOn", DateError));
                addCommonAttributes(model);
                return "rental/add";
            }
            // ここまで
            // 登録処理。RentalMangeServiceを使用し、rentalManageDtoオブジェクトを保存する（返す？）
            this.rentalManageService.save(rentalManageDto);

            return "redirect:/rental/index";// ページ遷移のためredirect
        } catch (Exception e) {
            log.error(e.getMessage());// 例外が発生したことをログファイルに記録
            /*
             * リダイレクト先のビューにデータを渡すためのフラッシュ属性"rentalManageDto"を追加。
             * フラッシュ属性...リダイレクト先のリクエスト「のみ」で利用可能であり、セッションに保存された後に自動的に削除される
             */
            ra.addFlashAttribute("rentalManageDto", rentalManageDto);// 登録内容を再表示
            // (バリデーションチェックの結果)をリダイレクト先で表示
            ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);// バリデーション結果をFlash属性として追加

            return "redirect:/rental/add";
        }
    }

    /* 貸出編集画面 */
    // {id}はパス変数として受け取る。特定のIDを持つレンタルの編集画面を表示するために使用。
    @GetMapping("/rental/{id}/edit")
    // @PathVariableアノテーションを使用して、URLからidパラメーターを受け取る。
    public String edit(@PathVariable("id") String id, Model model, // @RequestParam アノテーションは、HTTPリクエストのパラメーターを取得するために使用
            @RequestParam(name = "errorMessage", required = false) String errorMessage) {// required=falseはerrorMessageパラメーターが必須ではないことを示す。StringerrorMessageの中にエラーメッセージを格納
        // 全件取得
        List<RentalManage> rentalManageList = this.rentalManageService.findAll();
        List<Account> accounts = this.accountService.findAll();
        List<Stock> stockList = this.stockService.findAll();
        // modelに情報を追加し、必要に応じて列挙型を属性として取得
        model.addAttribute("accounts", accounts);
        model.addAttribute("stockList", stockList);
        model.addAttribute("rentalStatus", RentalStatus.values());
        model.addAttribute("rentalManageList", rentalManageList);
        model.addAttribute("rentalStockStatus", StockStatus.values());
        // エラーメッセージがある場合はそのメッセージをmodelに追加
        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage);
        }
        /*
         * containsAttributeメソッドは、モデルに指定された属性が含まれているかどうかを確認するために使用。
         * 含まれていなければ、新しいRentalManageDtoオブジェクトを作成するための条件が成立する。
         */
        if (!model.containsAttribute("rentalManageDto")) {
            // 新しくrentalManageDtoを作成
            RentalManageDto rentalManageDto = new RentalManageDto();
            // パス変数から取得した文字列形式のIDをLong型に変換
            Long idLong = Long.parseLong(id);
            // 指定されたIDに対応するRentalManageオブジェクトをデータベースから取得
            RentalManage rentalManage = this.rentalManageService.findById(idLong);

            /*
             * 取得したRentalManageオブジェクトから必要な情報を取り出し、RentalManageDtoオブジェクトに設定
             * 1.rentalManage.getAccount()は、RentalManageエンティティに関連付けられたAccountエンティティを取得。
             * 2.getEmployeeId()は、AccountエンティティからemployeeIdを取得。
             * 3.取得したemployeeIdは、RentalManageDtoの対応するフィールドであるemployeeIdにセット。
             */
            rentalManageDto.setEmployeeId(rentalManage.getAccount().getEmployeeId());
            rentalManageDto.setId(rentalManage.getId());
            rentalManageDto.setExpectedRentalOn(rentalManage.getExpectedRentalOn());
            rentalManageDto.setExpectedReturnOn(rentalManage.getExpectedReturnOn());
            rentalManageDto.setStatus(rentalManage.getStatus());
            rentalManageDto.setStockId(rentalManage.getStock().getId());
            // RentalManageDtoオブジェクトをモデルに"rentalManageDto"という属性名で追加
            model.addAttribute("rentalManageDto", rentalManageDto);
        }

        return "rental/edit";
    }

    @PostMapping("/rental/{id}/edit")
    public String update(@PathVariable("id") Long id, @Valid @ModelAttribute RentalManageDto rentalManageDto,
            BindingResult result, Model model) {
        try {
            // バリデーションエラーチェック
            if (result.hasErrors()) {
                model.addAttribute("errorMessage", "入力内容にエラーがあります");
                // バリデーションエラーがある場合は編集画面に戻る
                editCommonAttributes(model, rentalManageDto.getId(), rentalManageDto);
                return "rental/edit";
            }
            // 予定日フォーマットチェック
            // エラーリスト定義
            List<String> formatErrors = rentalManageDto.formatCheck(rentalManageDto);
            if (!formatErrors.isEmpty()) {
                for (String error : formatErrors) {
                    if (error.equals("貸出予定日はyyyy-MM-ddで入力してください")) {
                        result.addError(new FieldError("rentalManageDto", "expectedRentalOn", error));
                    } else if (error.equals("返却予定日はyyyy-MM-ddで入力してください")) {
                        result.addError(new FieldError("rentalManageDto", "expectedReturnOn", error));
                    }
                }
                editCommonAttributes(model, rentalManageDto.getId(), rentalManageDto);
                return "rental/add";
            }
            // 貸出情報のステータスをチェック
            RentalManage rentalManage = this.rentalManageService.findById(id);
            String statusErrorMessage = rentalManageDto.isValidStatus(rentalManage.getStatus());
            if (statusErrorMessage != null) {
                model.addAttribute("errorMessage", statusErrorMessage);
                // 無効な変更をした場合は編集画面に戻る
                editCommonAttributes(model, rentalManageDto.getId(), rentalManageDto);
                return "rental/edit";
            }
            // 日付チェック（5/20）
            String dateErrorMessage = rentalManageDto.isValidDate(rentalManageDto, rentalManage);
            if (dateErrorMessage != null) {
                model.addAttribute("errorMessage", dateErrorMessage);
                // 日付が現在日になっていない場合は編集画面に戻る
                editCommonAttributes(model, rentalManageDto.getId(), rentalManageDto);
                return "rental/edit";
            }
            // 利用可否チェック 追加（5/17）
            String errorMessage = this.checkInventoryStatus(rentalManageDto.getStockId());
            if (errorMessage != null) {
                result.addError(new FieldError("rentalManageDto", "stockId", errorMessage));
                // 書籍が利用不可の場合は編集画面に戻る
                editCommonAttributes(model, rentalManageDto.getId(), rentalManageDto);
                return "rental/edit";
            }
            // 貸出可否チェック 追加（5/16）
            String DateError = rentalManageDto.rentalCheck(rentalManageService, rentalManageDto,
                    rentalManageDto.getStockId(), rentalManageDto.getId());
            if (DateError != null) {
                result.addError(new FieldError("rentalManageDto", "expectedRentalOn", DateError));
                result.addError(new FieldError("rentalManageDto", "expectedReturnOn", DateError));
                // 貸出期間に重複があった場合は編集画面に戻る
                editCommonAttributes(model, rentalManageDto.getId(), rentalManageDto);
                return "rental/edit";
            }
            // 更新処理
            this.rentalManageService.update(id, rentalManageDto);
            return "redirect:/rental/index";
        } catch (Exception e) {
            // エラーが発生した場合の処理
            log.error("更新処理中にエラーが発生しました: " + e.getMessage());
            model.addAttribute("errorMessage", "更新処理中にエラーが発生しました");
            return "rental/edit";
        }
    }

    // 利用可否チェック
    private String checkInventoryStatus(String id) {
        Stock stock = this.stockService.findById(id);
        if (stock.getStatus() != StockStatus.RENT_AVAILABLE.getValue()) {// 修正いたしました（5/21）
            return "この本は利用できません"; // 利用不可の場合はエラーメッセージを返す
        } else {
            return null; // 利用可の場合はエラーメッセージなし
        }
    }

    // エラー時の表示遷移に
    private void addCommonAttributes(Model model) {
        List<Stock> stockList = this.stockService.findStockAvailableAll();
        List<Account> accounts = this.accountService.findAll();
        model.addAttribute("accounts", accounts);
        model.addAttribute("stockList", stockList);
        model.addAttribute("rentalStatus", RentalStatus.values());
    }

    private void editCommonAttributes(Model model, Long Id, RentalManageDto rentalManageDto) {
        List<Stock> stockList = this.stockService.findStockAvailableAll();
        List<Account> accounts = this.accountService.findAll();
        model.addAttribute("accounts", accounts);
        model.addAttribute("stockList", stockList);
        model.addAttribute("rentalStatus", RentalStatus.values());

        // Idを取得、RentalManageDtoを更新する
        RentalManage rentalManage = rentalManageService.findById(Id);
        rentalManageDto.setEmployeeId(rentalManage.getAccount().getEmployeeId());
        rentalManageDto.setId(rentalManage.getId());
        rentalManageDto.setExpectedRentalOn(rentalManage.getExpectedRentalOn());
        rentalManageDto.setExpectedReturnOn(rentalManage.getExpectedReturnOn());
        rentalManageDto.setStatus(rentalManage.getStatus());
        rentalManageDto.setStockId(rentalManage.getStock().getId());

        model.addAttribute("rentalManageDto", rentalManageDto);
    }

    // 在庫カレンダーからの遷移時のメソッドを作成（6/5）
    /*
     * @GetMapping("/rental/add")
     * public String addByCalender(@RequestParam("bookName") String
     * bookName, @RequestParam("date") String date,
     * Model model) {
     * List<Stock> stockList = this.stockService.findAll();
     * List<Account> accounts = this.accountService.findAll();
     * model.addAttribute("accounts", accounts);
     * model.addAttribute("stockList", stockList);
     * model.addAttribute("rentalStatus", RentalStatus.values());
     * 
     * if (!model.containsAttribute("calendarDto")) {
     * model.addAttribute("calendarDto", new calendarDto());
     * }
     * 
     * return "rental/add";
     * }
     */

}
