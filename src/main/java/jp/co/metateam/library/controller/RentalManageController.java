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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;


//ここ追加インポート
import org.springframework.web.bind.annotation.RequestParam;//(5/14)

/**
 * 貸出管理関連クラスß
 */
@Log4j2
@Controller
public class RentalManageController {

    //final修飾子で変数が初期化された後の変更を拒否
    //フィールド宣言（AccountServiceなど）をして変数を定義（accountService）
    private final AccountService accountService;
    private final RentalManageService rentalManageService;
    private final StockService stockService;

    //インスタンス生成の際に呼び出されるコンストラクタの定義
    /*AutowiredによってSpringFrameworkがDI（Dependency Injection）を行う。
     *DIによって、クラス間の依存関係を明示的にせずに、それらの依存関係を容易に変更したりすることができる。
     *要は下記3つのインスタンスを、具体的な実装関係なく参照することができる。
     *　→参照するクラスの実装を変えても、このクラスのコードは変えなくてもいい。柔軟性の高いコーディング。
    */
    @Autowired
    public RentalManageController(
        //AccountServiceというクラスのaccountServiceというインスタンスを参照。
        AccountService accountService, 
        RentalManageService rentalManageService, 
        StockService stockService
    ) {
        //コンストラクターで受け取ったaccountServiceをRentalManageControllerクラスのインスタンス変数に格納する。
        //this.accountService→このクラスでの変数accountServiceは、右辺のaccountService(上で参照したのと同じ)と同じものです、と定義。
        this.accountService = accountService;
        this.rentalManageService = rentalManageService;
        this.stockService = stockService;
    }

    /**
     * 貸出一覧画面初期表示
     * @param model　//Modelオブジェクトが引数として渡されています。SpringMVCが提供するモデルオブジェクト。
     * @return //メソッドが返す値や戻り値に関する情報を提供するために使用される。つまりどういうこと？
     */
    //「/rental/index」というURI(貸出一覧画面)へのGETリクエストがこのメソッドに送信されると、そのリクエストを処理するためにこのメソッドが呼び出される。
    @GetMapping("/rental/index")
    //メソッド名がindex、引数名がmodel。
    public String index(Model model) {
        // 貸出管理テーブルから全件取得
        //取得した貸出管理情報は、変数RentalManageList に代入されており、この変数には全ての貸出管理情報が含まれる。
        //this.rentalManageServiceサービスについては上述
        List<RentalManage> RentalManageList = this.rentalManageService.findAll();
        // 貸出一覧画面に渡すデータをmodelに追加
        //addAttributeは属性追加のメソッド。()内に名前と、属性として追加する変数（一行上で定義）を記述する。
        model.addAttribute("rentalManageList", RentalManageList);
        // 貸出一覧画面に遷移
        //rental/indexを返している。引数が文字列だが、これはパス名を指している。
        //この行を使うことで、このメソッドが実行された後に、指定されたビュー(rental/index)に遷移することができる。
        return "rental/index";
    }
    

    //Springのアノテーションを使用。GETリクエストが "/rental/add" というURLに対応
    //アノテーション…
    @GetMapping("/rental/add")
    //引数のModelオブジェクトは、ビューにデータを渡すためのもの
    public String add(Model model) {
        //Stockクラスのインスタンスリストを取得。変数名stockList。
        List <Stock> stockList = this.stockService.findAll();
        List <Account> accounts = this.accountService.findAll();

        model.addAttribute("accounts", accounts);
        model.addAttribute("stockList", stockList);
        model.addAttribute("rentalStatus", RentalStatus.values());

        if (!model.containsAttribute("rentalManageDto")) {
            model.addAttribute("rentalManageDto", new RentalManageDto());
        }
 
        return "rental/add";
    }

    @PostMapping("/rental/add")
    public String save(@Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result, RedirectAttributes ra) {
        try {
            if (result.hasErrors()) {
                throw new Exception("Validation error.");
            }
            // 登録処理
            this.rentalManageService.save(rentalManageDto);
 
            return "redirect:/rental/index";
        } catch (Exception e) {
            log.error(e.getMessage());
 
            ra.addFlashAttribute("rentalManageDto", rentalManageDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);
 
            return "redirect:/rental/add";
        }
    }

    /*　貸出編集画面 */
    @GetMapping("/rental/{id}/edit")
    public String edit(@PathVariable("id") String id, Model model, @RequestParam(name = "errorMessage", required = false) String errorMessage) {
        List<RentalManage> rentalManageList = this.rentalManageService.findAll();
 
        List<Account> accounts = this.accountService.findAll();
        List<Stock> stockList = this.stockService.findAll();
 
        model.addAttribute("accounts", accounts);
        model.addAttribute("stockList", stockList);
        model.addAttribute("rentalStatus", RentalStatus.values());
 
        model.addAttribute("rentalManageList", rentalManageList);
        model.addAttribute("rentalStockStatus", StockStatus.values());
 
        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage);
        }

        if (!model.containsAttribute("rentalManageDto")) {
            RentalManageDto rentalManageDto = new RentalManageDto();
            Long idLong = Long.parseLong(id);
            RentalManage rentalManage = this.rentalManageService.findById(idLong);

 
            rentalManageDto.setEmployeeId(rentalManage.getAccount().getEmployeeId());
 
            rentalManageDto.setId(rentalManage.getId());
            rentalManageDto.setExpectedRentalOn(rentalManage.getExpectedRentalOn());
            rentalManageDto.setExpectedReturnOn(rentalManage.getExpectedReturnOn());
            rentalManageDto.setStatus(rentalManage.getStatus());
            rentalManageDto.setStockId(rentalManage.getStock().getId());
 
            model.addAttribute("rentalManageDto", rentalManageDto);
        }
 
        return "rental/edit";
    }



    
    @PostMapping("/rental/{id}/edit")
    public String update(@PathVariable("id") Long id, @Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result, Model model) {
        try {
            // バリデーションエラーチェック
            if (result.hasErrors()) {
                model.addAttribute("errorMessage", "入力内容にエラーがあります");
                // バリデーションエラーがある場合は編集画面に戻る
                List<Stock> stockList = this.stockService.findStockAvailableAll();
                List<Account> accounts = this.accountService.findAll();
                model.addAttribute("accounts", accounts);
                model.addAttribute("stockList", stockList);
                model.addAttribute("rentalStatus", RentalStatus.values());
                return "rental/edit";
            }
    
            // 貸出情報を取得
            RentalManage rentalManage = this.rentalManageService.findById(id);
            if (rentalManage == null) {
                model.addAttribute("errorMessage", "指定された貸出情報が見つかりません");
                // 貸出情報が見つからない場合は編集画面に戻る
                List<Stock> stockList = this.stockService.findStockAvailableAll();
                List<Account> accounts = this.accountService.findAll();
                model.addAttribute("accounts", accounts);
                model.addAttribute("stockList", stockList);
                model.addAttribute("rentalStatus", RentalStatus.values());
                return "rental/edit";
            }
    
            // 貸出情報のステータスをチェック
            String statusErrorMessage = rentalManageDto.isValidStatus(rentalManage.getStatus());
            if (statusErrorMessage != null) {
                model.addAttribute("errorMessage", statusErrorMessage);
                // ステータスが無効な場合は編集画面に戻る
                List<Stock> stockList = this.stockService.findStockAvailableAll();
                List<Account> accounts = this.accountService.findAll();
                model.addAttribute("accounts", accounts);
                model.addAttribute("stockList", stockList);
                model.addAttribute("rentalStatus", RentalStatus.values());
                return "rental/edit";
            }
    
            // 貸出予定日のバリデーションチェック
            if (rentalManage.getStatus() == RentalStatus.RENT_WAIT.getValue() &&
                rentalManageDto.getStatus() == RentalStatus.RENTAlING.getValue()){
            if (!rentalManageDto.isValidRentalDate()) {
       
                model.addAttribute("errorMessage", "貸出予定日は現在の日付に設定してください");
                List<Stock> stockList = this.stockService.findStockAvailableAll();
                List<Account> accounts = this.accountService.findAll();
                model.addAttribute("accounts", accounts);
                model.addAttribute("stockList", stockList);
                model.addAttribute("rentalStatus", RentalStatus.values());
                return "rental/edit";
                }
            //返却予定日のバリデーションチェック
            }else if (rentalManage.getStatus() == RentalStatus.RENTAlING.getValue() &&
                rentalManageDto.getStatus() == RentalStatus.RETURNED.getValue()) {
            if(!rentalManageDto.isValidReturnDate()) {
        
                model.addAttribute("errorMessage", "返却予定日は現在の日付に設定してください");
                List<Stock> stockList = this.stockService.findStockAvailableAll();
                List<Account> accounts = this.accountService.findAll();
                model.addAttribute("accounts", accounts);
                model.addAttribute("stockList", stockList);
                model.addAttribute("rentalStatus", RentalStatus.values());
                return "rental/edit";
                }
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
    
    
} 
  
