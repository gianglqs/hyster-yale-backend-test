package com.hysteryale.repository;

import com.hysteryale.model.Currency;
import com.hysteryale.model.Part;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;


public interface PartRepository extends JpaRepository<Part, String> {

    @Query("SELECT DISTINCT p FROM Part p WHERE p.orderNumber = ?1 ")
    public Set<Part> getPartByOrderNumber(String orderNumber) ;

    @Query("SELECT CASE WHEN (COUNT(p) > 0) THEN 1 ELSE 0 END " +
            "FROM Part p WHERE p.modelCode = ?1 AND p.partNumber = ?2 AND p.orderNumber = ?3 AND p.recordedTime = ?4 and p.currency.currency = ?5")
    Integer isPartExisted(String modelCode, String partNumber, String orderNumber, LocalDate recordedTime, String currency);

    @Query("SELECT p FROM Part p WHERE p.modelCode = ?1 AND p.currency.currency = ?2 AND p.recordedTime = ?3 AND p.partNumber = ?4")
    List<Part> getNetPriceInPart(String modelCode, String currency, LocalDate recordedTime, String partNumber);

    @Query("SElECT DISTINCT p FROM Part p WHERE p.partNumber = ?1 AND p.series = ?2 ")
    public Set<Part> getPartByPartNumberAndSeries(String partNumber, String series);

    @Query("SELECT DISTINCT p.modelCode FROM Part p WHERE p.recordedTime = ?1")
    public List<String> getDistinctModelCodeByMonthYear(LocalDate monthYear);

    @Query(
            value =
            "select p.id, p.bill_to, p.customer_price, p.description, p.discount, p.discount_percentage, " +
                    "p.discount_to_customer_percentage, p.extended_customer_price, p.list_price, p.part_number, p.model_code, " +
                    "p.net_price_each, p.option_type, p.order_booked_date, p.order_request_date, p.order_number, p.quantity, p.quote_id, " +
                    "p.recorded_time, p.series, p.currency_currency, p.clazz, p.issped, p.region " +
            "from (select *, row_number() over (partition by model_code, part_number, currency_currency order by model_code asc) rn from part) p\n" +
            "where rn = 1 and model_code = :modelCode and currency_currency = :currency",
            nativeQuery = true
    )
    public List<Part> getDistinctPart(@Param("modelCode") String modelCode, @Param("currency") String currency);

    @Query("SELECT DISTINCT p FROM Part p WHERE p.partNumber IN ?1 ")
    public Set<Part> getPartsByPartNumbers(List<String> partNumbers, LocalDate date, String series);

    @Query("SELECT AVG(p.netPriceEach) FROM Part p WHERE p.region = ?1 AND p.clazz = ?2 AND p.series = ?3")
    Double getAverageDealerNet(String region, String clazz, String series);

    @Query("SELECT p.partNumber FROM Part p WHERE p.orderNumber = ?1 ")
    List<String> getPartNumberByOrderNo(String orderNo);

    @Query("SELECT DISTINCT p.currency FROM Part p WHERE p.orderNumber = ?1 ")
    Currency getCurrencyByOrderNo(String orderNo);

    @Query(value = "SELECT * FROM Part p WHERE " +
            "p.model_code = :modelCode AND p.part_number = :partNumber AND p.order_number = :orderNumber " +
            "AND p.recorded_time = :recordedTime AND p.currency_currency = :currency LIMIT 1", nativeQuery = true)
    Optional<Part> getPart(@Param("modelCode") String modelCode, @Param("partNumber") String partNumber,
                           @Param("orderNumber") String orderNumber, @Param("recordedTime") LocalDate recordedTime, @Param("currency") String currency);

    @Query(value =  "SELECT p FROM Part p WHERE " +
                    "   p.modelCode = :modelCode " +
                    "   AND (:orderNumbers IS NULL OR p.orderNumber in (:orderNumbers))"+
                    "   ORDER BY p.partNumber"

    )
    List<Part> getPartForProductDimensionDetail(String modelCode,
                                                List<String> orderNumbers,
                                                Pageable pageable);

    @Query(value =  "SELECT COUNT(p) FROM Part p WHERE " +
            "   p.modelCode = :modelCode " +
            "   AND (:orderNumbers IS NULL OR p.orderNumber in (:orderNumbers))")
    long countAllForProductDetail(String modelCode, List<String> orderNumbers);
}
