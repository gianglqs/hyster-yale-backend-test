package com.hysteryale.repository;

import com.hysteryale.model.Booking;
import com.hysteryale.model.TrendData;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BookingRepository extends JpaRepository<Booking, String> {

    @Query("SELECT DISTINCT b.product.modelCode FROM Booking b ORDER BY b.product.modelCode ASC ")
    List<String> getAllModel();

    @Query("SELECT b FROM Booking b WHERE b.orderNo = ?1")
    Optional<Booking> getBookingOrderByOrderNo(String orderNo);

    @Query("SELECT new Booking(c.country.region.regionName, c.product.plant, c.product.clazz," +
            " c.series, c.product.modelCode, sum(c.quantity), sum(c.totalCost), sum(c.dealerNet), " +
            " sum(c.dealerNetAfterSurcharge), sum(c.marginAfterSurcharge)) " +
            " FROM Booking c WHERE " +
            " ((:regions) IS Null OR c.country.region.regionName IN (:regions))" +
            " AND ((:plants) IS NULL OR c.product.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(c.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR c.product.clazz.clazzName IN (:classes))" +
            " AND ((:models) IS NULL OR c.product.modelCode IN (:models))" +
            " AND ((:dealerName) IS NULL OR c.dealer.name IN (:dealerName))" +
            " AND ((:marginPercentageAfterSurCharge) IS NULL OR " +
            "   (:comparator = '<=' AND c.marginPercentageAfterSurcharge <= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>=' AND c.marginPercentageAfterSurcharge >= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '<' AND c.marginPercentageAfterSurcharge < :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>' AND c.marginPercentageAfterSurcharge > :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '=' AND c.marginPercentageAfterSurcharge = :marginPercentageAfterSurCharge))" +
            " AND ((:dealerName) IS NULL OR c.dealer.name IN (:dealerName))" +
            " AND (cast(:fromDate as date) IS NULL OR c.date >= (:fromDate))" +
            " AND (cast(:toDate as date) IS NULL OR c.date <= (:toDate))" +
            " AND c.currency IS NOT NULL " +
            " GROUP BY c.country.region.regionName, c.product.plant, c.product.clazz, c.series, c.product.modelCode" +
            " ORDER BY c.country.region.regionName"
    )
    List<Booking> getOrderForOutlier(
            @Param("regions") List<String> regions,
            @Param("plants") List<String> plants,
            @Param("metaSeries") List<String> metaSeries,
            @Param("classes") List<String> classes,
            @Param("models") List<String> models,
            @Param("dealerName") List<String> dealerName,
            @Param("comparator") String comparator,
            @Param("marginPercentageAfterSurCharge") Double marginPercentageAfterSurCharge,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable);

    @Query("SELECT new Booking( COALESCE(sum(c.quantity), 0), COALESCE(sum(c.totalCost), 0), COALESCE(sum(c.dealerNet), 0), " +
            " COALESCE(sum(c.dealerNetAfterSurcharge), 0), COALESCE(sum(c.marginAfterSurcharge), 0), COALESCE(sum(c.marginAfterSurcharge) / sum(c.dealerNetAfterSurcharge), 0)) " +
            " FROM Booking c WHERE " +
            " ((:regions) IS Null OR c.country.region.regionName IN (:regions))" +
            " AND ((:plants) IS NULL OR c.product.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(c.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR c.product.clazz.clazzName IN (:classes))" +
            " AND ((:models) IS NULL OR c.product.modelCode IN (:models))" +
            " AND ((:dealerName) IS NULL OR c.dealer.name IN (:dealerName))" +
            " AND ((:marginPercentageAfterSurCharge) IS NULL OR " +
            "   (:comparator = '<=' AND c.marginPercentageAfterSurcharge <= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>=' AND c.marginPercentageAfterSurcharge >= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '<' AND c.marginPercentageAfterSurcharge < :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>' AND c.marginPercentageAfterSurcharge > :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '=' AND c.marginPercentageAfterSurcharge = :marginPercentageAfterSurCharge))" +
            " AND ((:dealerName) IS NULL OR c.dealer.name IN (:dealerName))" +
            " AND (cast(:fromDate as date) IS NULL OR c.date >= (:fromDate))" +
            " AND (cast(:toDate as date) IS NULL OR c.date <= (:toDate))" +
            " AND c.currency IS NOT NULL "
    )
    List<Booking> getSumAllOrderForOutlier(
            @Param("regions") List<String> regions,
            @Param("plants") List<String> plants,
            @Param("metaSeries") List<String> metaSeries,
            @Param("classes") List<String> classes,
            @Param("models") List<String> models,
            @Param("dealerName") List<String> dealerName,
            @Param("comparator") String comparator,
            @Param("marginPercentageAfterSurCharge") Double marginPercentageAfterSurCharge,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    @Query("SELECT COUNT(c)" +
            " FROM Booking c WHERE " +
            " ((:regions) IS Null OR c.country.region.regionName IN (:regions) )" +
            " AND ((:plants) IS NULL OR c.product.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(c.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR c.product.clazz.clazzName IN (:classes))" +
            " AND ((:models) IS NULL OR c.product.modelCode IN (:models))" +
            " AND ((:dealerName) IS NULL OR c.dealer.name IN (:dealerName))" +
            " AND ((:marginPercentageAfterSurCharge) IS NULL OR " +
            "   (:comparator = '<=' AND c.marginPercentageAfterSurcharge <= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>=' AND c.marginPercentageAfterSurcharge >= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '<' AND c.marginPercentageAfterSurcharge < :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>' AND c.marginPercentageAfterSurcharge > :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '=' AND c.marginPercentageAfterSurcharge = :marginPercentageAfterSurCharge))" +
            " AND ((:dealerName) IS NULL OR c.dealer.name IN (:dealerName))" +
            " AND (cast(:fromDate as date) IS NULL OR c.date >= (:fromDate))" +
            " AND (cast(:toDate as date) IS NULL OR c.date <= (:toDate))" +
            " AND c.currency IS NOT NULL " +
            " GROUP BY c.country.region.regionName, c.product.plant, c.product.clazz, c.series, c.product.modelCode"
    )
    List<Integer> countAllForOutlier(
            @Param("regions") List<String> regions,
            @Param("plants") List<String> plants,
            @Param("metaSeries") List<String> metaSeries,
            @Param("classes") List<String> classes,
            @Param("models") List<String> models,
            @Param("dealerName") List<String> dealerName,
            @Param("comparator") String comparator,
            @Param("marginPercentageAfterSurCharge") Double marginPercentageAfterSurCharge,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);


    @Query("SELECT COUNT(c) FROM Booking c WHERE " +
            "((:orderNo) IS Null OR LOWER(c.orderNo) LIKE LOWER(CONCAT('%', :orderNo, '%')))" +
            " AND ((:regions) IS Null OR c.country.region.regionName IN (:regions) )" +
            " AND ((:plants) IS NULL OR c.product.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(c.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR c.product.clazz.clazzName IN (:classes))" +
            " AND ((:models) IS NULL OR c.product.modelCode IN (:models))" +
            " AND ((:segments) IS NULL OR c.product.segment IN (:segments))" +
            " AND ((:dealerName) IS NULL OR c.dealer.name IN (:dealerName))" +
            " AND ((:AOPMarginPercentage) IS NULL OR " +
            "   (:AOPMarginPercentage = 'Above AOP Margin %' AND c.AOPMargin.marginSTD < c.marginPercentageAfterSurcharge) OR" +
            "   (:AOPMarginPercentage = 'Below AOP Margin %' AND c.AOPMargin.marginSTD >= c.marginPercentageAfterSurcharge))" +
            " AND ((:marginPercentageAfterSurCharge) IS NULL OR " +
            "   (:comparator = '<=' AND c.marginPercentageAfterSurcharge <= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>=' AND c.marginPercentageAfterSurcharge >= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '<' AND c.marginPercentageAfterSurcharge < :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>' AND c.marginPercentageAfterSurcharge > :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '=' AND c.marginPercentageAfterSurcharge = :marginPercentageAfterSurCharge))" +
            " AND ((:dealerName) IS NULL OR c.dealer.name IN (:dealerName))" +
            " AND (cast(:fromDate as date) IS NULL OR c.date >= (:fromDate))" +
            " AND (cast(:toDate as date) IS NULL OR c.date <= (:toDate))"
    )
    int getCount(@Param("orderNo") String orderNo,
                 @Param("regions") List<String> regions,
                 @Param("plants") List<String> plants,
                 @Param("metaSeries") List<String> metaSeries,
                 @Param("classes") List<String> classes,
                 @Param("models") List<String> models,
                 @Param("segments") List<String> segments,
                 @Param("dealerName") List<String> dealerName,
                 @Param("AOPMarginPercentage") String AOPMarginPercentage,
                 @Param("comparator") String comparator,
                 @Param("marginPercentageAfterSurCharge") Double marginPercentageAfterSurCharge,
                 @Param("fromDate") LocalDate fromDate,
                 @Param("toDate") LocalDate toDate);

    @Query("SELECT c FROM Booking c WHERE " +
            "((:orderNo) IS Null OR LOWER(c.orderNo) LIKE LOWER(CONCAT('%', :orderNo, '%')))" +
            " AND ((:regions) IS Null OR c.country.region.regionName IN (:regions) )" +
            " AND ((:plants) IS NULL OR c.product.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(c.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR c.product.clazz.clazzName IN (:classes))" +
            " AND ((:models) IS NULL OR c.product.modelCode IN (:models))" +
            " AND ((:segments) IS NULL OR c.product.segment IN (:segments))" +
            " AND ((:dealerName) IS NULL OR c.dealer.name IN (:dealerName))" +
            " AND ((:AOPMarginPercentage) IS NULL OR " +
            "   (:AOPMarginPercentage = 'Above AOP Margin %' AND c.AOPMargin.marginSTD < c.marginPercentageAfterSurcharge) OR" +
            "   (:AOPMarginPercentage = 'Below AOP Margin %' AND c.AOPMargin.marginSTD >= c.marginPercentageAfterSurcharge))" +
            " AND ((:marginPercentageAfterSurCharge) IS NULL OR " +
            "   (:comparator = '<=' AND c.marginPercentageAfterSurcharge <= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>=' AND c.marginPercentageAfterSurcharge >= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '<' AND c.marginPercentageAfterSurcharge < :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>' AND c.marginPercentageAfterSurcharge > :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '=' AND c.marginPercentageAfterSurcharge = :marginPercentageAfterSurCharge))" +
            " AND ((:dealerName) IS NULL OR c.dealer.name IN (:dealerName))" +
            " AND (cast(:fromDate as date ) IS NULL OR c.date >= :fromDate)" +
            " AND (cast(:toDate as date) IS NULL OR c.date <= :toDate) ORDER BY similarity(c.orderNo, :orderNo) DESC "
    )
    List<Booking> selectAllForBookingOrder(
            @Param("orderNo") String orderNo,
            @Param("regions") List<String> regions,
            @Param("plants") List<String> plants,
            @Param("metaSeries") List<String> metaSeries,
            @Param("classes") List<String> classes,
            @Param("models") List<String> models,
            @Param("segments") List<String> segments,
            @Param("dealerName") List<String> dealerName,
            @Param("AOPMarginPercentage") String AOPMarginPercentage,
            @Param("comparator") String comparator,
            @Param("marginPercentageAfterSurCharge") Double marginPercentageAfterSurCharge,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toData,
            @Param("pageable") Pageable pageable
    );

    @Query("SELECT b from Booking b where b.orderNo IN :listOrderNo")
    List<Booking> getListBookingExist(List<String> listOrderNo);

    Booking findByOrderNo(String orderNo);

    @Query("SELECT new com.hysteryale.model.TrendData( EXTRACT(month FROM b.date) as month, " +
            "AVG(b.marginPercentageAfterSurcharge) as marginPercentage, " +
            "AVG(b.totalCost) as costOrDealerNet ) " +
            "FROM Booking b WHERE " +
            " ((:regions) IS NULL OR b.country.region.regionName IN (:regions) )" +
            " AND ((:plants) IS NULL OR b.product.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(b.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR b.product.clazz.clazzName IN (:classes))" +
            " AND ((:models) IS NULL OR b.product.modelCode IN (:models))" +
            " AND ((:segments) IS NULL OR b.product.segment IN (:segments))" +
            " AND ((:dealerName) IS NULL OR b.dealer.name IN (:dealerName)) " +
            " AND EXTRACT(year FROM b.date) = :year" +
            " AND b.marginPercentageAfterSurcharge != 'NaN'" +
            " AND b.marginPercentageAfterSurcharge != '-Infinity'" +
            " AND b.marginPercentageAfterSurcharge != 'Infinity'" +
            " GROUP BY EXTRACT(month FROM b.date) ORDER BY month ASC"
    )
    List<TrendData> getMarginVsCostData(
            @Param("regions") List<String> regions,
            @Param("plants") List<String> plants,
            @Param("metaSeries") List<String> metaSeries,
            @Param("classes") List<String> classes,
            @Param("models") List<String> models,
            @Param("segments") List<String> segments,
            @Param("dealerName") List<String> dealerName,
            @Param("year") int year);

    @Query("SELECT new com.hysteryale.model.TrendData( EXTRACT(month FROM b.date) as month, " +
            "AVG(b.marginPercentageAfterSurcharge) as marginPercentage, " +
            "AVG(b.dealerNet) as costOrDealerNet ) " +
            "FROM Booking b WHERE " +
            " ((:regions) IS NULL OR b.country.region.regionName IN (:regions) )" +
            " AND ((:plants) IS NULL OR b.product.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(b.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR b.product.clazz.clazzName IN (:classes))" +
            " AND ((:models) IS NULL OR b.product.modelCode IN (:models))" +
            " AND ((:segments) IS NULL OR b.product.segment IN (:segments))" +
            " AND ((:dealerName) IS NULL OR b.dealer.name IN (:dealerName)) " +
            " AND EXTRACT(year FROM b.date) = :year" +
            " AND b.marginPercentageAfterSurcharge != 'NaN'" +
            " AND b.marginPercentageAfterSurcharge != '-Infinity'" +
            " AND b.marginPercentageAfterSurcharge != 'Infinity'" +
            " GROUP BY EXTRACT(month FROM b.date) ORDER BY month ASC"
    )
    List<TrendData> getMarginVsDNData(
            @Param("regions") List<String> regions,
            @Param("plants") List<String> plants,
            @Param("metaSeries") List<String> metaSeries,
            @Param("classes") List<String> classes,
            @Param("models") List<String> models,
            @Param("segments") List<String> segments,
            @Param("dealerName") List<String> dealerName,
            @Param("year") int year);


    @Query("SELECT new Booking(c.country.region.regionName, c.product, c.currency, " +
            "sum(c.totalCost), sum(c.dealerNetAfterSurcharge), sum(c.marginAfterSurcharge), count(c)) " +
            " FROM Booking c WHERE " +
            " ((:regions) IS Null OR c.country.region.regionName IN (:regions))" +
            " AND ((:plants) IS NULL OR c.product.plant IN (:plants))" +
            " AND ((:segments) IS NULL OR c.product.segment IN (:segments))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(c.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR c.product.clazz.clazzName IN (:classes))" +
            " AND ((:models) IS NULL OR c.product.modelCode IN (:models))" +
            " AND ((:dealerName) IS NULL OR c.dealer.name IN (:dealerName))" +
            " AND ((:marginPercentageAfterSurCharge) IS NULL OR " +
            "   (:comparator = '<=' AND c.marginPercentageAfterSurcharge <= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>=' AND c.marginPercentageAfterSurcharge >= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '<' AND c.marginPercentageAfterSurcharge < :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>' AND c.marginPercentageAfterSurcharge > :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '=' AND c.marginPercentageAfterSurcharge = :marginPercentageAfterSurCharge))" +
            " AND c.currency IS NOT NULL " +
            " GROUP BY c.country.region.regionName, c.product, c.currency " +
            " HAVING (:marginPercentageAfterSurChargeAfterAdj) IS NULL OR " +
            "   (:comparatorAfterAdj = '<' AND sum(c.dealerNetAfterSurcharge) <> 0 AND ((sum(c.dealerNetAfterSurcharge) * (1 + :dnAdjPercentage / 100.0) - (sum(c.totalCost) * (1 + :costAdjPercentage/100.0) - :freightAdj - :fxAdj)) / (sum(c.dealerNetAfterSurcharge) * (1 + :dnAdjPercentage / 100.0))) < :marginPercentageAfterSurChargeAfterAdj) OR" +
            "   (:comparatorAfterAdj = '>=' AND sum(c.dealerNetAfterSurcharge) <> 0 AND ((sum(c.dealerNetAfterSurcharge) * (1 + :dnAdjPercentage / 100.0) - (sum(c.totalCost) * (1 + :costAdjPercentage/100.0) - :freightAdj - :fxAdj)) / (sum(c.dealerNetAfterSurcharge) * (1 + :dnAdjPercentage / 100.0))) > :marginPercentageAfterSurChargeAfterAdj)"
    )
    List<Booking> selectForAdjustmentByFilter(
            @Param("regions") List<String> regions,
            @Param("dealerName") List<String> dealerName,
            @Param("plants") List<String> plants,
            @Param("segments") List<String> segments,
            @Param("classes") List<String> classes,
            @Param("metaSeries") List<String> metaSeries,
            @Param("models") List<String> models,
            @Param("comparator") String comparator,
            @Param("marginPercentageAfterSurCharge") Double marginPercentageAfterSurCharge,
            @Param("comparatorAfterAdj") String comparatorAfterAdj,
            @Param("marginPercentageAfterSurChargeAfterAdj") Double marginPercentageAfterSurChargeAfterAdj,
            @Param("costAdjPercentage") double costAdjPercentage,
            @Param("freightAdj") double freightAdj,
            @Param("fxAdj") double fxAdj,
            @Param("dnAdjPercentage") double dnAdjPercentage,
            Pageable pageable);

    @Query("SELECT COUNT(c) " +
            " FROM Booking c WHERE " +
            " ((:regions) IS Null OR c.country.region.regionName IN (:regions))" +
            " AND ((:plants) IS NULL OR c.product.plant IN (:plants))" +
            " AND ((:segments) IS NULL OR c.product.segment IN (:segments))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(c.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR c.product.clazz.clazzName IN (:classes))" +
            " AND ((:models) IS NULL OR c.product.modelCode IN (:models))" +
            " AND ((:dealerName) IS NULL OR c.dealer.name IN (:dealerName))" +
            " AND ((:marginPercentageAfterSurCharge) IS NULL OR " +
            "   (:comparator = '<=' AND c.marginPercentageAfterSurcharge <= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>=' AND c.marginPercentageAfterSurcharge >= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '<' AND c.marginPercentageAfterSurcharge < :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>' AND c.marginPercentageAfterSurcharge > :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '=' AND c.marginPercentageAfterSurcharge = :marginPercentageAfterSurCharge))" +
            " AND c.currency IS NOT NULL " +
            " GROUP BY c.country.region.regionName, c.product, c.currency" +
            " HAVING (:marginPercentageAfterSurChargeAfterAdj) IS NULL OR " +
            "   (:comparatorAfterAdj = '<' AND sum(c.dealerNetAfterSurcharge) <> 0 AND (sum(c.dealerNetAfterSurcharge) * (1 + :dnAdjPercentage / 100.0) - (sum(c.totalCost) * (1 + :costAdjPercentage/100.0) - :freightAdj - :fxAdj)) / (sum(c.dealerNetAfterSurcharge) * (1 + :dnAdjPercentage / 100.0)) < :marginPercentageAfterSurChargeAfterAdj) OR" +
            "   (:comparatorAfterAdj = '>=' AND sum(c.dealerNetAfterSurcharge) <> 0 AND (sum(c.dealerNetAfterSurcharge) * (1 + :dnAdjPercentage / 100.0) - (sum(c.totalCost) * (1 + :costAdjPercentage/100.0) - :freightAdj - :fxAdj)) / (sum(c.dealerNetAfterSurcharge) * (1 + :dnAdjPercentage / 100.0)) >= :marginPercentageAfterSurChargeAfterAdj)"
    )
    List<Integer> getCountAllForAdjustmentByFilter(
            @Param("regions") List<String> regions,
            @Param("dealerName") List<String> dealerName,
            @Param("plants") List<String> plants,
            @Param("segments") List<String> segments,
            @Param("classes") List<String> classes,
            @Param("metaSeries") List<String> metaSeries,
            @Param("models") List<String> models,
            @Param("comparator") String comparator,
            @Param("marginPercentageAfterSurCharge") Double marginPercentageAfterSurCharge,
            @Param("comparatorAfterAdj") String comparatorAfterAdj,
            @Param("marginPercentageAfterSurChargeAfterAdj") Double marginPercentageAfterSurChargeAfterAdj,
            @Param("costAdjPercentage") double costAdjPercentage,
            @Param("freightAdj") double freightAdj,
            @Param("fxAdj") double fxAdj,
            @Param("dnAdjPercentage") double dnAdjPercentage
    );

    @Query("SELECT c FROM Booking c WHERE " +
            "((:orderNo) IS Null OR LOWER(c.orderNo) LIKE LOWER(CONCAT('%', :orderNo, '%')))" +
            " AND ((:regions) IS Null OR c.country.region.regionName IN (:regions) )" +
            " AND ((:plants) IS NULL OR c.product.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(c.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR c.product.clazz.clazzName IN (:classes))" +
            " AND ((:models) IS NULL OR c.product.modelCode IN (:models))" +
            " AND ((:segments) IS NULL OR c.product.segment IN (:segments))" +
            " AND ((:dealerName) IS NULL OR c.dealer.name IN (:dealerName))" +
            " AND ((:AOPMarginPercentage) IS NULL OR " +
            "   (:AOPMarginPercentage = 'Above AOP Margin %' AND c.AOPMargin.marginSTD < c.marginPercentageAfterSurcharge) OR" +
            "   (:AOPMarginPercentage = 'Below AOP Margin %' AND c.AOPMargin.marginSTD >= c.marginPercentageAfterSurcharge))" +
            " AND ((:marginPercentageAfterSurCharge) IS NULL OR " +
            "   (:comparator = '<=' AND c.marginPercentageAfterSurcharge <= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>=' AND c.marginPercentageAfterSurcharge >= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '<' AND c.marginPercentageAfterSurcharge < :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>' AND c.marginPercentageAfterSurcharge > :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '=' AND c.marginPercentageAfterSurcharge = :marginPercentageAfterSurCharge))" +
            " AND ((:dealerName) IS NULL OR c.dealer.name IN (:dealerName))" +
            " AND (cast(:fromDate as date ) IS NULL OR c.date >= :fromDate)" +
            " AND (cast(:toDate as date) IS NULL OR c.date <= :toDate)"
    )
    List<Booking> getTotal(
            @Param("orderNo") String orderNo,
            @Param("regions") List<String> regions,
            @Param("plants") List<String> plants,
            @Param("metaSeries") List<String> metaSeries,
            @Param("classes") List<String> classes,
            @Param("models") List<String> models,
            @Param("segments") List<String> segments,
            @Param("dealerName") List<String> dealerName,
            @Param("AOPMarginPercentage") String AOPMarginPercentage,
            @Param("comparator") String comparator,
            @Param("marginPercentageAfterSurCharge") Double marginPercentageAfterSurCharge,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    @Query("SELECT COALESCE((sum(c.marginAfterSurcharge) / NULLIF( sum(c.dealerNetAfterSurcharge),0)),0) FROM Booking c WHERE " +
            "(:listOrderNo) IS Null OR c.orderNo IN (:listOrderNo) ")
    double getTotalMarginPercentage(List<String> listOrderNo);


    @Query("SELECT new Booking( sum(c.dealerNetAfterSurcharge), sum(c.totalCost), sum(c.marginAfterSurcharge), count(c)) " +
            " FROM Booking c WHERE " +
            " ((:regions) IS Null OR c.country.region.regionName IN (:regions))" +
            " AND ((:plants) IS NULL OR c.product.plant IN (:plants))" +
            " AND ((:segments) IS NULL OR c.product.segment IN (:segments))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(c.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR c.product.clazz.clazzName IN (:classes))" +
            " AND ((:models) IS NULL OR c.product.modelCode IN (:models))" +
            " AND ((:dealerName) IS NULL OR c.dealer.name IN (:dealerName))" +
            " AND ((:marginPercentageAfterSurCharge) IS NULL OR " +
            "   (:comparator = '<=' AND c.marginPercentageAfterSurcharge <= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>=' AND c.marginPercentageAfterSurcharge >= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '<' AND c.marginPercentageAfterSurcharge < :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>' AND c.marginPercentageAfterSurcharge > :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '=' AND c.marginPercentageAfterSurcharge = :marginPercentageAfterSurCharge))" +
            " AND c.currency IS NOT NULL " +
            " GROUP BY c.country.region.regionName, c.product" +
            " HAVING (:marginPercentageAfterSurChargeAfterAdj) IS NULL OR " +
            "   (:comparatorAfterAdj = '<' AND sum(c.dealerNetAfterSurcharge) <> 0 AND (sum(c.dealerNetAfterSurcharge) * (1 + :dnAdjPercentage / 100.0) - (sum(c.totalCost) * (1 + :costAdjPercentage/100.0) - :freightAdj - :fxAdj)) / (sum(c.dealerNetAfterSurcharge) * (1 + :dnAdjPercentage / 100.0)) < :marginPercentageAfterSurChargeAfterAdj) OR" +
            "   (:comparatorAfterAdj = '>=' AND sum(c.dealerNetAfterSurcharge) <> 0 AND (sum(c.dealerNetAfterSurcharge) * (1 + :dnAdjPercentage / 100.0) - (sum(c.totalCost) * (1 + :costAdjPercentage/100.0) - :freightAdj - :fxAdj)) / (sum(c.dealerNetAfterSurcharge) * (1 + :dnAdjPercentage / 100.0)) >= :marginPercentageAfterSurChargeAfterAdj)"

    )
    List<Booking> selectTotalForAdjustment(
            @Param("regions") List<String> regions,
            @Param("dealerName") List<String> dealerName,
            @Param("plants") List<String> plants,
            @Param("segments") List<String> segments,
            @Param("classes") List<String> classes,
            @Param("metaSeries") List<String> metaSeries,
            @Param("models") List<String> models,
            @Param("comparator") String comparator,
            @Param("marginPercentageAfterSurCharge") Double marginPercentageAfterSurCharge,
            @Param("comparatorAfterAdj") String comparatorAfterAdj,
            @Param("marginPercentageAfterSurChargeAfterAdj") Double marginPercentageAfterSurChargeAfterAdj,
            @Param("costAdjPercentage") double costAdjPercentage,
            @Param("freightAdj") double freightAdj,
            @Param("fxAdj") double fxAdj,
            @Param("dnAdjPercentage") double dnAdjPercentage);

    @Query(value = "SELECT b.orderNo FROM Booking b WHERE b.product.modelCode = :modelCode AND b.product.series = :metaSeries ")
    List<String> getOrderNosByModelCodeAndMetaSeries(String modelCode, String metaSeries);

    @Query(value = "SELECT m.latest_modified_at FROM booking m WHERE m.latest_modified_at is not null ORDER BY m.latest_modified_at DESC LIMIT 1", nativeQuery = true)
    Optional<LocalDateTime> getLatestUpdatedTime();

    @Query("SELECT b FROM Booking b WHERE b.orderNo IN (:setOrderNo)")
    List<Booking> getListBookingByListOrderNo(Set<String> setOrderNo);

    @Query("SELECT new Booking(b.product.segment, b.series, SUM(b.totalCost), SUM(b.dealerNetAfterSurcharge), SUM(b.quantity)) FROM Booking b " +
            " WHERE b.product.segment IS NOT NULL AND b.product.segment <> '' AND " +
            " ((:segments) IS NULL OR b.product.segment in (:segments)) " +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(b.series, 2,3) IN (:metaSeries))" +
            " GROUP BY b.series, b.product.segment ")
    List<Booking> getBookingForPriceVolumeSensitivityGroupBySeries(List<String> segments, List<String> metaSeries);

    @Query("SELECT new Booking(b.product.segment, SUM(b.totalCost), SUM(b.dealerNetAfterSurcharge), SUM(b.quantity)) FROM Booking b " +
            " WHERE b.product.segment IS NOT NULL AND b.product.segment <> '' AND " +
            " ((:segments) IS NULL OR b.product.segment in (:segments)) " +
            " GROUP BY b.product.segment ")
    List<Booking> getBookingForPriceVolumeSensitivityGroupBySegment(List<String> segments);

//    @Query("SELECT COUNT(b) FROM Booking b " +
//            " WHERE b.product.segment IS NOT NULL AND b.product.segment <> '' AND " +
//            " ((:segments) IS NULL OR b.product.segment in (:segments)) " +
//            " AND ((:metaSeries) IS NULL OR SUBSTRING(b.series, 2,3) IN (:metaSeries))" +
//            " GROUP BY b.series, b.product.segment ")
//    long countAllForPriceVolSensitivityGroupBySeries(List<String> segments, List<String> metaSeries);

    @Query(value = "select count(*) from (select p.series from "+
            " booking b inner join product p on b.product = p.id "+
            " where p.segment is not null and trim(p.segment) <> '' "+
            " and ((:segments) is null or p.segment in (:segments))"+
            " and ((:metaSeries) IS NULL OR SUBSTRING(b.series, 2,3) IN (:metaSeries))" +
            "group by p.series) as groupbySegment", nativeQuery = true)
    long countAllForPriceVolSensitivityGroupBySeries(List<String> segments, List<String> metaSeries);

    @Query(value = "select count(*) from (select p.segment from "+
            " booking b inner join product p on b.product = p.id "+
            " where p.segment is not null and trim(p.segment) <> '' "+
            " and ((:segments) is null or p.segment in (:segments))"+
            "group by p.segment) as groupbySegment", nativeQuery = true)
    long countAllForPriceVolSensitivityGroupBySegment(List<String> segments);

}
