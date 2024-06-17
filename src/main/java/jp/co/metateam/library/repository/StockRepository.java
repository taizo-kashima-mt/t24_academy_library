package jp.co.metateam.library.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.Stock;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

        @NonNull
        List<Stock> findAll();

        @NonNull
        List<Stock> findByDeletedAtIsNull();

        @NonNull
        List<Stock> findByDeletedAtIsNullAndStatus(Integer status);

        @NonNull
        Optional<Stock> findById(String id);

        @NonNull
        List<Stock> findByBookMstIdAndStatus(Long book_id, Integer status);

        @Query("SELECT COUNT(*) AS count " +
                        "FROM BookMst bm " +
                        "JOIN Stock s ON bm.id = s.bookMst.id AND s.status = 0 " +
                        "GROUP BY bm.title")
        List<Long> countStocksByBookTitle();

        // @Query("SELECT COUNT(DISTINCT bm.title) FROM Stock s JOIN s.bookMst bm WHERE
        // s.status = 0")
        // Long countStocksByBookTitle();

        @Query("select rm"
                        + " from RentalManage rm "
                        + " where (rm.status = 0 or rm.status = 1)")
        List<RentalManage> rentalManage0or1();

        @Query(value = "SELECT s.id " +
                        "FROM stocks s " +
                        "LEFT JOIN rental_manage rm ON s.id = rm.stock_id " +
                        "INNER JOIN book_mst bm ON s.book_id = bm.id " +
                        "WHERE bm.title = ?1 AND (rm.expected_rental_on IS NULL OR rm.expected_rental_on > ?2 OR rm.expected_return_on < ?2) AND s.status = 0", nativeQuery = true)
        List<String> stockIdAvailableRental(String title, LocalDate currentDate);

}