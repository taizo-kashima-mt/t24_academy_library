package jp.co.metateam.library.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.metateam.library.constants.Constants;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.model.StockDto;
import jp.co.metateam.library.repository.BookMstRepository;
import jp.co.metateam.library.repository.StockRepository;

import org.springframework.ui.Model;

import java.util.Date;
import jp.co.metateam.library.repository.RentalManageRepository;
import java.util.Map;
import java.time.YearMonth;

@Service
public class StockService {
    private final BookMstRepository bookMstRepository;
    private final StockRepository stockRepository;
    private final RentalManageRepository rentalManageRepository;

    @Autowired
    public StockService(BookMstRepository bookMstRepository, StockRepository stockRepository,
            RentalManageRepository rentalManageRepository) {
        this.bookMstRepository = bookMstRepository;
        this.stockRepository = stockRepository;
        this.rentalManageRepository = rentalManageRepository;
    }

    @Transactional
    public List<Stock> findAll() {
        List<Stock> stocks = this.stockRepository.findByDeletedAtIsNull();

        return stocks;
    }

    @Transactional
    public List<Stock> findStockAvailableAll() {
        List<Stock> stocks = this.stockRepository.findByDeletedAtIsNullAndStatus(Constants.STOCK_AVAILABLE);

        return stocks;
    }

    @Transactional
    public Stock findById(String id) {
        return this.stockRepository.findById(id).orElse(null);
    }

    @Transactional
    public void save(StockDto stockDto) throws Exception {
        try {
            Stock stock = new Stock();
            BookMst bookMst = this.bookMstRepository.findById(stockDto.getBookId()).orElse(null);
            if (bookMst == null) {
                throw new Exception("BookMst record not found.");
            }

            stock.setBookMst(bookMst);
            stock.setId(stockDto.getId());
            stock.setStatus(stockDto.getStatus());
            stock.setPrice(stockDto.getPrice());

            // データベースへの保存
            this.stockRepository.save(stock);
        } catch (Exception e) {
            throw e;
        }
    }

    @Transactional
    public void update(String id, StockDto stockDto) throws Exception {
        try {
            Stock stock = findById(id);
            if (stock == null) {
                throw new Exception("Stock record not found.");
            }

            BookMst bookMst = stock.getBookMst();
            if (bookMst == null) {
                throw new Exception("BookMst record not found.");
            }

            stock.setId(stockDto.getId());
            stock.setBookMst(bookMst);
            stock.setStatus(stockDto.getStatus());
            stock.setPrice(stockDto.getPrice());

            // データベースへの保存
            this.stockRepository.save(stock);
        } catch (Exception e) {
            throw e;
        }
    }

    public List<Object> generateDaysOfWeek(int year, int month, int daysInMonth) {
        List<Object> daysOfWeek = new ArrayList<>();
        for (int dayOfMonth = 1; dayOfMonth <= daysInMonth; dayOfMonth++) {
            LocalDate date = LocalDate.of(year, month, dayOfMonth);
            DateTimeFormatter formmater = DateTimeFormatter.ofPattern("dd(E)", Locale.JAPANESE);
            daysOfWeek.add(date.format(formmater));
        }

        return daysOfWeek;
    }

    // 現在の年と月を取得
    YearMonth currentYearMonth = YearMonth.now();
    int currentYear = currentYearMonth.getYear();
    int currentMonth = currentYearMonth.getMonthValue();

    public List<Object[]> generateBookData(Model model, int year, int month) {
        List<Stock> stocks = findAll(); // 在庫情報を取得

        // 書籍名ごとの在庫数を保持するMapを作成
        Map<String, Long> bookCountMap = new HashMap<>();
        for (Stock stock : stocks) {
            String bookName = stock.getBookMst().getTitle();
            // 在庫ステータスが０の場合のみカウント
            if (stock.getStatus() == 0) {
                bookCountMap.put(bookName, bookCountMap.getOrDefault(bookName, 0L) + 1);
            }
        }

        // 書籍名と在庫数を組み合わせた2次元配列を作成する
        List<Object[]> bookData = new ArrayList<>();
        for (Map.Entry<String, Long> entry : bookCountMap.entrySet()) {
            String bookName = entry.getKey();
            Long stockCount = entry.getValue();
            Object[] row = { bookName, stockCount };
            bookData.add(row);
        }

        // 各書籍ごとに各日の在庫数を計算して配列に追加
        List<Object[]> updatedBookData = new ArrayList<>();
        for (Object[] row : bookData) {
            String title = (String) row[0];
            Long stockCount = (Long) row[1];

            // 各日の在庫状況を格納する配列を作成
            Object[] stockInfo = new Object[getDaysInMonth(year, month) + 2];

            stockInfo[0] = title; // 書籍名を配列の最初の要素に格納
            stockInfo[1] = stockCount; // 在庫数を配列の2番目の要素に格納

            // 各日の在庫状況を計算して配列に格納
            for (int dayOfMonth = 1; dayOfMonth <= getDaysInMonth(year, month); dayOfMonth++) {
                LocalDate currentDate = LocalDate.of(year, month, dayOfMonth);
                Date specifiedDate = Date.from(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                Long rentalCount = this.rentalManageRepository.countBySpecifiedDateRentals(title, specifiedDate);
                Long countAvailableRental = stockCount - rentalCount;
                List<String> stockIdAvailableRental = this.stockRepository.stockIdAvailableRental(title, currentDate);
                String stockIdAvailebleRentalIndex1 = (stockIdAvailableRental != null
                        && !stockIdAvailableRental.isEmpty()) ? stockIdAvailableRental.get(0) : "×";

                List<Object> infoList = new ArrayList<>();

                infoList.add(currentDate); // 日付
                infoList.add(stockIdAvailebleRentalIndex1); // 在庫管理番号
                infoList.add(countAvailableRental); // 在庫数

                stockInfo[dayOfMonth + 1] = infoList;
            }
            // 書籍情報をupdatedBookDataに追加
            updatedBookData.add(stockInfo);
        }

        // Modelに書籍データを追加する
        model.addAttribute("bookData", updatedBookData);

        // 生成された書籍データを返す
        return updatedBookData;
    }

    public int getDaysInMonth(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        return yearMonth.lengthOfMonth();
    }

}
