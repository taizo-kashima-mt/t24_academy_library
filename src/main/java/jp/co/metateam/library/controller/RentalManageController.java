package jp.co.metateam.library.controller;

import java.util.List;

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
    // 引数のModelオブジェクトは、ビューにデータを渡すためのもの。
    public String add(Model model) {
        // Stock・Accountクラスのインスタンスリストを取得。変数名stockList・accounts。
        // 本クラスで定義したstockService・accountServiceの全ての情報を取得
        List<Stock> stockList = this.stockService.findAll();
        List<Account> accounts = this.accountService.findAll();
        // モデルに情報のリストを追加する。
        // addAttribute()はモデルに属性を追加するメソッド。属性は“名前”と値のペアで構成。
        // 上記より、ビュー（HTMLテンプレート）において${accounts}の様な仕方でこれらの属性にアクセスが可能。
        model.addAttribute("accounts", accounts);
        model.addAttribute("stockList", stockList);
        /*
         * RentalStatus.values()について。
         * //RentalStatusクラスの列挙型を全権取得している。
         * //Javaの列挙型(enum)は、複数の定数(値)を定義するための特殊な型。
         * //列挙型の使用で、関連する定数(値)をグループ化し、プログラム内で使いやすくなる。
         * 今回は、
         * public enum RentalStatus implements Values {
         * RENT_WAIT(0, "貸出待ち")
         * , RENTAlING(1, "貸出中")
         * , RETURNED(2, "返却済み")
         * , CANCELED(3, "キャンセル");
         * }
         */
        model.addAttribute("rentalStatus", RentalStatus.values());
        /*
         * モデルに"rentalManageDto"という名前の属性が含まれていない場合は、新しいRentalManageDtoオブジェクトをモデルに追加する。
         * 上記は、フォームを初期化するために使用される（？）。
         */
        if (!model.containsAttribute("rentalManageDto")) {
            model.addAttribute("rentalManageDto", new RentalManageDto());
        }

        // モデルに貸出管理DTOを追加
        if (!model.containsAttribute("rentalManageDto")) {
            model.addAttribute("rentalManageDto", new RentalManageDto());
        }

        return "rental/add";
    }

    // POSTリクエストが "/rental/add" というURLに対応。
    @PostMapping("/rental/add")
    /*
     * HTTP POSTリクエストが "/rental/add" に送信されたときに呼び出される。
     * 
     * @Validアノテーションは、rentalManageDtoオブジェクトのバリデーションを有効にする
     * BindingResultオブジェクトはバインディング結果を保持する。
     * 
     * @ModelAttributeアノテーションは、リクエストパラメーターをJavaオブジェクトにバインドします。
     */
    public String save(@Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result,
            RedirectAttributes ra, Model model) {
        try {

            // バリデーションチェック
            if (result.hasErrors()) {
                throw new Exception("Validation error.");
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
            }
            // ここまで
            // 登録処理。RentalMangeServiceを使用し、rentalManageDtoオブジェクトを保存する（返す？）
            this.rentalManageService.save(rentalManageDto);

            return "redirect:/rental/index";
        } catch (Exception e) {
            log.error(e.getMessage());
            /*
             * リダイレクト先のビューにデータを渡すためのフラッシュ属性"rentalManageDto"を追加。
             * フラッシュ属性...リダイレクト先のリクエスト「のみ」で利用可能であり、セッションに保存された後に自動的に削除される
             */
            ra.addFlashAttribute("rentalManageDto", rentalManageDto);
            // (バリデーションチェックの結果)をリダイレクト先で表示
            ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);

            return "redirect:/rental/add";
        }
    }

    /* 貸出編集画面 */
    // {id}はパス変数として受け取る。特定のIDを持つレンタルの編集画面を表示するために使用。
    @GetMapping("/rental/{id}/edit")
    // @PathVariableアノテーションを使用して、URLからidパラメーターを受け取る。
    public String edit(@PathVariable("id") String id, Model model,
            @RequestParam(name = "errorMessage", required = false) String errorMessage) {
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
                addCommonAttributes(model);
                return "rental/edit";
            }
            // 貸出情報のステータスをチェック
            RentalManage rentalManage = this.rentalManageService.findById(id);
            String statusErrorMessage = rentalManageDto.isValidStatus(rentalManage.getStatus());
            if (statusErrorMessage != null) {
                model.addAttribute("errorMessage", statusErrorMessage);
                // 無効な変更をした場合は編集画面に戻る
                addCommonAttributes(model);
                return "rental/edit";
            }
            // 日付チェック（5/20）
            String dateErrorMessage = rentalManageDto.isValidDate(rentalManageDto, rentalManage);
            if (dateErrorMessage != null) {
                model.addAttribute("errorMessage", dateErrorMessage);
                // 日付が現在日になっていない場合は編集画面に戻る
                addCommonAttributes(model);
                return "rental/edit";
            }
            // 利用可否チェック 追加（5/17）
            String errorMessage = this.checkInventoryStatus(rentalManageDto.getStockId());
            if (errorMessage != null) {
                result.addError(new FieldError("rentalManageDto", "stockId", errorMessage));
                // 書籍が利用不可の場合は編集画面に戻る
                addCommonAttributes(model);
                return "rental/edit";
            }
            // 貸出可否チェック 追加（5/16）
            String DateError = rentalManageDto.rentalCheck(rentalManageService, rentalManageDto,
                    rentalManageDto.getStockId(), rentalManageDto.getId());
            if (DateError != null) {
                result.addError(new FieldError("rentalManageDto", "expectedRentalOn", DateError));
                result.addError(new FieldError("rentalManageDto", "expectedReturnOn", DateError));
                // 貸出期間に重複があった場合は編集画面に戻る
                addCommonAttributes(model);
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
}
