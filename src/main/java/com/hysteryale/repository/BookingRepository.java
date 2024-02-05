package com.hysteryale.repository;

import com.hysteryale.model.Booking;
import com.hysteryale.model.TrendData;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingOrderRepository extends JpaRepository<Booking, String> {

    @Query("SELECT DISTINCT b.dealer.name FROM Booking b ORDER BY b.dealer.name")
    List<String> getAllDealerName();

    @Query("SELECT DISTINCT b.product.modelCode FROM Booking b ORDER BY b.product.modelCode ASC ")
    List<String> getAllModel();

    @Query("SELECT b FROM Booking b WHERE b.orderNo = ?1")
    Optional<Booking> getBookingOrderByOrderNo(String orderNo);

    // it is not including condition on currency due to missing currency data
    @Query("SELECT DISTINCT b FROM Booking b WHERE b.product.modelCode = ?1 AND extract(year from b.date) = ?2 AND extract(month from b.date ) = ?3")
    List<Booking> getDistinctBookingOrderByModelCode(String modelCode, int year, int month);

    @Query("SELECT new Booking(c.region.regionName, c.product.plant, c.product.clazz," +
            " c.series, c.product.modelCode, sum(c.quantity), sum(c.totalCost), sum(c.dealerNet), " +
            " sum(c.dealerNetAfterSurcharge), sum(c.marginAfterSurcharge)) " +
            " FROM Booking c WHERE " +
            " ((:regions) IS Null OR c.region.regionName IN (:regions))" +
            " AND ((:plants) IS NULL OR c.product.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(c.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR c.product.clazz IN (:classes))" +
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
            " GROUP BY c.region.regionName, c.product.plant, c.product.clazz, c.series, c.product.modelCode" +
            " ORDER BY c.region.regionName"
    )
    List<Booking> getOrderForOutline(
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
            " ((:regions) IS Null OR c.region.regionName IN (:regions))" +
            " AND ((:plants) IS NULL OR c.product.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(c.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR c.product.clazz IN (:classes))" +
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
            " AND (cast(:toDate as date) IS NULL OR c.date <= (:toDate))"
    )
    List<Booking> getSumAllOrderForOutline(
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

    //   @Query("SELECT COUNT(distinct (c.region.regionShortName || c.productDimension.plant || c.productDimension.clazz || c.series || c.productDimension.model) )" +
    @Query("SELECT COUNT(c)" +
            " FROM Booking c WHERE " +
            " ((:regions) IS Null OR c.region.regionName IN (:regions) )" +
            " AND ((:plants) IS NULL OR c.product.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(c.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR c.product.clazz IN (:classes))" +
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
            " GROUP BY c.region.regionName, c.product.plant, c.product.clazz, c.series, c.product.modelCode"
    )
    List<Integer> countAllForOutline(
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
            "((:orderNo) IS Null OR c.orderNo = :orderNo )" +
            " AND ((:regions) IS Null OR c.region.regionName IN (:regions) )" +
            " AND ((:plants) IS NULL OR c.product.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(c.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR c.product.clazz IN (:classes))" +
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
            "((:orderNo) IS Null OR c.orderNo = :orderNo )" +
            " AND ((:regions) IS Null OR c.region.regionName IN (:regions) )" +
            " AND ((:plants) IS NULL OR c.product.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(c.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR c.product.clazz IN (:classes))" +
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
            " AND (cast(:toDate as date) IS NULL OR c.date <= :toDate) ORDER BY c.orderNo"
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
            " ((:regions) IS NULL OR b.region.regionName IN (:regions) )" +
            " AND ((:plants) IS NULL OR b.product.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(b.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR b.product.clazz IN (:classes))" +
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
            " ((:regions) IS NULL OR b.region.regionName IN (:regions) )" +
            " AND ((:plants) IS NULL OR b.product.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(b.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR b.product.clazz IN (:classes))" +
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


    @Query("SELECT new Booking(c.region.regionName, c.product, c.currency, " +
            "sum(c.totalCost), sum(c.dealerNetAfterSurcharge), sum(c.marginAfterSurcharge), count(c)) " +
            " FROM Booking c WHERE " +
            " ((:regions) IS Null OR c.region.regionName IN (:regions))" +
            " AND ((:plants) IS NULL OR c.product.plant IN (:plants))" +
            " AND ((:segments) IS NULL OR c.product.segment IN (:segments))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(c.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR c.product.clazz IN (:classes))" +
            " AND ((:models) IS NULL OR c.product.modelCode IN (:models))" +
            " AND ((:dealerName) IS NULL OR c.dealer.name IN (:dealerName))" +
            " AND ((:marginPercentageAfterSurCharge) IS NULL OR " +
            "   (:comparator = '<=' AND c.marginPercentageAfterSurcharge <= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>=' AND c.marginPercentageAfterSurcharge >= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '<' AND c.marginPercentageAfterSurcharge < :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>' AND c.marginPercentageAfterSurcharge > :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '=' AND c.marginPercentageAfterSurcharge = :marginPercentageAfterSurCharge))" +
            " AND c.currency IS NOT NULL "+
            " GROUP BY c.region.regionName, c.product, c.currency " +
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
            " ((:regions) IS Null OR c.region.regionName IN (:regions))" +
            " AND ((:plants) IS NULL OR c.product.plant IN (:plants))" +
            " AND ((:segments) IS NULL OR c.product.segment IN (:segments))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(c.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR c.product.clazz IN (:classes))" +
            " AND ((:models) IS NULL OR c.product.modelCode IN (:models))" +
            " AND ((:dealerName) IS NULL OR c.dealer.name IN (:dealerName))" +
            " AND ((:marginPercentageAfterSurCharge) IS NULL OR " +
            "   (:comparator = '<=' AND c.marginPercentageAfterSurcharge <= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>=' AND c.marginPercentageAfterSurcharge >= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '<' AND c.marginPercentageAfterSurcharge < :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>' AND c.marginPercentageAfterSurcharge > :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '=' AND c.marginPercentageAfterSurcharge = :marginPercentageAfterSurCharge))" +
            " AND c.currency IS NOT NULL "+
            " GROUP BY c.region.regionName, c.product, c.currency" +
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

    @Query("SELECT new Booking(c.orderNo, c.currency, c.dealerNet, c.dealerNetAfterSurcharge, c.totalCost) FROM Booking c WHERE " +
            "((:orderNo) IS Null OR c.orderNo = :orderNo )" +
            " AND ((:regions) IS Null OR c.region.regionName IN (:regions) )" +
            " AND ((:plants) IS NULL OR c.product.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(c.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR c.product.clazz IN (:classes))" +
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
            " ((:regions) IS Null OR c.region.regionName IN (:regions))" +
            " AND ((:plants) IS NULL OR c.product.plant IN (:plants))" +
            " AND ((:segments) IS NULL OR c.product.segment IN (:segments))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(c.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR c.product.clazz IN (:classes))" +
            " AND ((:models) IS NULL OR c.product.modelCode IN (:models))" +
            " AND ((:dealerName) IS NULL OR c.dealer.name IN (:dealerName))" +
            " AND ((:marginPercentageAfterSurCharge) IS NULL OR " +
            "   (:comparator = '<=' AND c.marginPercentageAfterSurcharge <= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>=' AND c.marginPercentageAfterSurcharge >= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '<' AND c.marginPercentageAfterSurcharge < :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>' AND c.marginPercentageAfterSurcharge > :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '=' AND c.marginPercentageAfterSurcharge = :marginPercentageAfterSurCharge))" +
            " AND c.currency IS NOT NULL "+
            " GROUP BY c.region.regionName, c.product" +
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

    @Query(value =
            "with exchange_rate_cte as (" +
                    "select " +
                    "   er.from_currency ," +
                    "   er.rate as latest_rate " +
                    "from" +
                    "   exchange_rate er " +
                    "join" +
                    "   (" +
                    "       select " +
                    "           from_currency ," +
                    "           max(date) as latest_date" +
                    "       from" +
                    "           exchange_rate" +
                    "       where " +
                    "           to_currency = 'USD' " +
                    "       group by " +
                    "           from_currency " +
                    "   ) latest_date " +
                    "on" +
                    "   er.from_currency = latest_date.from_currency" +
                    "   and er.date = latest_date.latest_date " +
                    "where " +
                    "   er.to_currency = 'USD' " +
                    ")" +
                    "select " +
                    "   'Total' as order_no, " +
                    "   null as aopmargin, " +
                    "   null as bill_to, " +
                    "   null as comment, " +
                    "   null as ctry_code, " +
                    "   null as date, " +
                    "   null as dealer_name, " +
                    "   null as dealerpo, " +
                    "   'USD' as currency, " +
                    "   null as order_type, " +
                    "   null as series, " +
                    "   null as truck_class, " +
                    "   null as product, " +
                    "   null as region, " +
                    "   null as model, " +
                    "   coalesce(total_cost, 0) as total_cost, " +
                    "   coalesce(dealer_net, 0) as dealer_net, " +
                    "   coalesce(dealer_net_after_surcharge, 0) as dealer_net_after_surcharge, " +
                    "   coalesce(dealer_net_after_surcharge - total_cost, 0)as margin_after_surcharge, " +
                    "   coalesce((dealer_net_after_surcharge - total_cost) / nullif(dealer_net_after_surcharge, 0), 0) as margin_percentage_after_surcharge, " +
                    "   coalesce(quantity, 0) as quantity " +
                    "from " +
                    "   (" +
                    "   select" +
                    "       sum(" +
                    "            case " +
                    "                when bo.currency = 'USD' then bo.total_cost " +
                    "                when bo.currency is not null then coalesce(er.latest_rate, 1) * bo.total_cost " +
                    "            end " +
                    "        ) as total_cost," +
                    "       sum( " +
                    "            case " +
                    "                when bo.currency = 'USD' then bo.dealer_net " +
                    "                when bo.currency is not null then coalesce(er.latest_rate, 1) * bo.dealer_net " +
                    "            end " +
                    "        ) as dealer_net, " +
                    "       sum(" +
                    "            case " +
                    "                when bo.currency = 'USD' then bo.dealer_net_after_sur_charge " +
                    "                when bo.currency is not null then coalesce(er.latest_rate, 1) * bo.dealer_net_after_sur_charge " +
                    "            end " +
                    "        ) as dealer_net_after_surcharge, " +
                    "       sum(bo.quantity) as quantity " +
                    "    from " +
                    "       booking bo " +
                    "   left join exchange_rate_cte er on " +
                    "       bo.currency = er.from_currency " +
                    "   left join product pd on " +
                    "       bo.product = pd.model_code " +
                    "   left join region r on " +
                    "       bo.region = r.id " +
                    "   where " +
                    "      (:orderNo is null or lower(bo.order_no) = lower( :orderNo))  " +
                    "       and ( (:regions) is null or r.region_name in (:regions)) " +
                    "       and ((:dealerNames) is null or bo.dealer_name in (:dealerNames)) " +
                    "       and ((:classes) is null or pd.clazz in (:classes)) " +
                    "       and ((:plants) is null or pd.plant in (:plants)) " +
                    "       and ((:segments) is null or pd.segment in (:segments)) " +
                    "       and ((:metaSeries) is null or substring(bo.series, 2, 3) in (:metaSeries)) " +
                    "       and ((:models) is null or pd.model_code in (:models)) " +
                    "       AND ((:marginPercentageAfterSurCharge) IS NULL OR " +
                    "           (:comparator = '<=' AND bo.margin_percentage_after_surcharge <= :marginPercentageAfterSurCharge) OR" +
                    "           (:comparator = '>=' AND bo.margin_percentage_after_surcharge >= :marginPercentageAfterSurCharge) OR" +
                    "           (:comparator = '<' AND bo.margin_percentage_after_surcharge < :marginPercentageAfterSurCharge) OR" +
                    "           (:comparator = '>' AND bo.margin_percentage_after_surcharge > :marginPercentageAfterSurCharge) OR" +
                    "           (:comparator = '=' AND bo.margin_percentage_after_surcharge = :marginPercentageAfterSurCharge))" +
                    "       and ((:AOPMarginPercentage) IS NULL or " +
                    "           (:AOPMarginPercentage = 'Above AOP Margin %' and bo.margin_percentage_after_surcharge > bo.aopmargin.marginstd ) or " +
                    "           (:AOPMarginPercentage = 'Below AOP Margin %' and bo.margin_percentage_after_surcharge <= bo.aopmargin.marginstd ) )" +
                    "       and ( bo.date >= :fromDate) " +
                    "       and ( bo.date <= :toDate) " +
                    ") as subquery; ", nativeQuery = true)
    List<Booking> getTotalRowForBookingPage(
            @Param("orderNo") String orderNo,
            @Param("regions") List<String> regions,
            @Param("plants") List<String> plants,
            @Param("metaSeries") List<String> metaSeries,
            @Param("classes") List<String> classes,
            @Param("models") List<String> models,
            @Param("segments") List<String> segments,
            @Param("dealerNames") List<String> dealerNames,
            @Param("AOPMarginPercentage") String AOPMarginPercentage,
            @Param("comparator") String comparator,
            @Param("marginPercentageAfterSurCharge") Double marginPercentageAfterSurCharge,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );


    @Query(value = "SELECT b.order_no FROM booking b WHERE b.product = :modelCode", nativeQuery = true)
    List<String> getOrderNosByModelCode(String modelCode);
}
