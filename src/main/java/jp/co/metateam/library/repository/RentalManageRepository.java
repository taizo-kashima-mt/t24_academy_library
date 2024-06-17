package jp.co.metateam.library.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import jp.co.metateam.library.model.RentalManage;
import java.util.Date;

@Repository
public interface RentalManageRepository extends JpaRepository<RentalManage, Long> {
        @NonNull
        List<RentalManage> findAll();

        @NonNull
        Optional<RentalManage> findById(@NonNull Long id);

        // 追加(5/16)
        // JPQL（Java Persistence Query Language）またはSQLクエリを指定
        @Query("select rm"
                        + " from RentalManage rm " + " where (rm.status = 0 or rm.status = 1)"
                        + " and rm.stock.id = ?1 "
                        + " and rm.id <> ?2")
        List<RentalManage> findByStockIdAndStatusIn(String Id, Long rentalId);

        @Query("select rm"
                        + " from RentalManage rm " + " where (rm.status = 0 or rm.status = 1)"
                        + " and rm.stock.id = ?1 ")
        List<RentalManage> findByStockIdAndStatusIn(String Id);

        // ここまで
        /*
         * 1.2.3.4.5 rentalId
         * A.A.A.A.A id
         * String Id → 1.2.3.4.5のレコード５行取得
         * Long rentalId → rm.id ≠ 1 → .2.3.4.5のレコード４行取得
         */
        @Query(value = "SELECT COUNT(*) FROM rental_manage rm INNER JOIN stocks s ON rm.stock_id = s.id INNER JOIN book_mst bm ON s.book_id = bm.id"
                        + " WHERE bm.title = ?1 AND (rm.expected_rental_on <= ?2 AND rm.expected_return_on >= ?2) AND (rm.status = 0 OR rm.status = 1)", nativeQuery = true)
        Long countBySpecifiedDateRentals(String title, Date specifiedDate);

        RentalManage findByExpectedRentalOn(Date expectedRentalOn);
}
