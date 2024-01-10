package com.hysteryale.repository.bookingorder;

import com.hysteryale.model.BookingOrder;
import com.hysteryale.model.TrendData;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;

public interface BookingOrderRepository extends JpaRepository<BookingOrder, String> {

    @Query("SELECT DISTINCT b.dealerName FROM BookingOrder b ORDER BY b.dealerName")
    public List<String> getAllDealerName();

    @Query("SELECT DISTINCT b.model FROM BookingOrder b ORDER BY b.model ASC ")
    List<String> getAllModel();

    @Query("SELECT b FROM BookingOrder b WHERE b.orderNo = ?1")
    Optional<BookingOrder> getBookingOrderByOrderNo(String orderNo);

    // it is not including condition on currency due to missing currency data
    @Query("SELECT DISTINCT b FROM BookingOrder b WHERE b.model = ?1 AND extract(year from b.date) = ?2 AND extract(month from b.date ) = ?3")
    List<BookingOrder> getDistinctBookingOrderByModelCode(String modelCode, int year, int month);

    @Query("SELECT new BookingOrder(c.region.region, c.productDimension.plant, c.productDimension.clazz," +
            " c.series, c.model, sum(c.quantity), sum(c.totalCost), sum(c.dealerNet), " +
            " sum(c.dealerNetAfterSurCharge), sum(c.marginAfterSurCharge)) " +
            " FROM BookingOrder c WHERE " +
            " ((:regions) IS Null OR c.region.region IN (:regions))" +
            " AND ((:plants) IS NULL OR c.productDimension.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(c.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR c.productDimension.clazz IN (:classes))" +
            " AND ((:models) IS NULL OR c.model IN (:models))" +
            " AND ((:dealerName) IS NULL OR c.dealerName IN (:dealerName))" +
            " AND ((:marginPercentageAfterSurCharge) IS NULL OR " +
            "   (:comparator = '<=' AND c.marginPercentageAfterSurCharge <= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>=' AND c.marginPercentageAfterSurCharge >= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '<' AND c.marginPercentageAfterSurCharge < :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>' AND c.marginPercentageAfterSurCharge > :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '=' AND c.marginPercentageAfterSurCharge = :marginPercentageAfterSurCharge))" +
            " AND ((:dealerName) IS NULL OR c.dealerName IN (:dealerName))" +
            " AND (cast(:fromDate as date) IS NULL OR c.date >= (:fromDate))" +
            " AND (cast(:toDate as date) IS NULL OR c.date <= (:toDate))" +
            " GROUP BY c.region.region, c.productDimension.plant, c.productDimension.clazz, c.series, c.model" +
            " ORDER BY c.region.region"
    )
    List<BookingOrder> getOrderForOutline(
            @Param("regions") List<String> regions,
            @Param("plants") List<String> plants,
            @Param("metaSeries") List<String> metaSeries,
            @Param("classes") List<String> classes,
            @Param("models") List<String> models,
            @Param("dealerName") List<String> dealerName,
            @Param("comparator") String comparator,
            @Param("marginPercentageAfterSurCharge") Double marginPercentageAfterSurCharge,
            @Param("fromDate") Calendar fromDate,
            @Param("toDate") Calendar toDate,
            Pageable pageable);

    @Query("SELECT new BookingOrder( COALESCE(sum(c.quantity), 0), COALESCE(sum(c.totalCost), 0), COALESCE(sum(c.dealerNet), 0), " +
            " COALESCE(sum(c.dealerNetAfterSurCharge), 0), COALESCE(sum(c.marginAfterSurCharge), 0), COALESCE(sum(c.marginAfterSurCharge) / sum(c.dealerNetAfterSurCharge), 0)) " +
            " FROM BookingOrder c WHERE " +
            " ((:regions) IS Null OR c.region.region IN (:regions))" +
            " AND ((:plants) IS NULL OR c.productDimension.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(c.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR c.productDimension.clazz IN (:classes))" +
            " AND ((:models) IS NULL OR c.model IN (:models))" +
            " AND ((:dealerName) IS NULL OR c.dealerName IN (:dealerName))" +
            " AND ((:marginPercentageAfterSurCharge) IS NULL OR " +
            "   (:comparator = '<=' AND c.marginPercentageAfterSurCharge <= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>=' AND c.marginPercentageAfterSurCharge >= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '<' AND c.marginPercentageAfterSurCharge < :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>' AND c.marginPercentageAfterSurCharge > :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '=' AND c.marginPercentageAfterSurCharge = :marginPercentageAfterSurCharge))" +
            " AND ((:dealerName) IS NULL OR c.dealerName IN (:dealerName))" +
            " AND (cast(:fromDate as date) IS NULL OR c.date >= (:fromDate))" +
            " AND (cast(:toDate as date) IS NULL OR c.date <= (:toDate))"
    )
    List<BookingOrder> getSumAllOrderForOutline(
            @Param("regions") List<String> regions,
            @Param("plants") List<String> plants,
            @Param("metaSeries") List<String> metaSeries,
            @Param("classes") List<String> classes,
            @Param("models") List<String> models,
            @Param("dealerName") List<String> dealerName,
            @Param("comparator") String comparator,
            @Param("marginPercentageAfterSurCharge") Double marginPercentageAfterSurCharge,
            @Param("fromDate") Calendar fromDate,
            @Param("toDate") Calendar toDate);

    //   @Query("SELECT COUNT(distinct (c.region.regionShortName || c.productDimension.plant || c.productDimension.clazz || c.series || c.productDimension.model) )" +
    @Query("SELECT COUNT(c)" +
            " FROM BookingOrder c WHERE " +
            " ((:regions) IS Null OR c.region.region IN (:regions) )" +
            " AND ((:plants) IS NULL OR c.productDimension.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(c.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR c.productDimension.clazz IN (:classes))" +
            " AND ((:models) IS NULL OR c.model IN (:models))" +
            " AND ((:dealerName) IS NULL OR c.dealerName IN (:dealerName))" +
            " AND ((:marginPercentageAfterSurCharge) IS NULL OR " +
            "   (:comparator = '<=' AND c.marginPercentageAfterSurCharge <= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>=' AND c.marginPercentageAfterSurCharge >= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '<' AND c.marginPercentageAfterSurCharge < :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>' AND c.marginPercentageAfterSurCharge > :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '=' AND c.marginPercentageAfterSurCharge = :marginPercentageAfterSurCharge))" +
            " AND ((:dealerName) IS NULL OR c.dealerName IN (:dealerName))" +
            " AND (cast(:fromDate as date) IS NULL OR c.date >= (:fromDate))" +
            " AND (cast(:toDate as date) IS NULL OR c.date <= (:toDate))" +
            " GROUP BY c.region.region, c.productDimension.plant, c.productDimension.clazz, c.series, c.model"
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
            @Param("fromDate") Calendar fromDate,
            @Param("toDate") Calendar toDate);


    @Query(value = "SELECT * FROM booking_order WHERE model = ?1 LIMIT 1", nativeQuery = true)
    Optional<BookingOrder> getDistinctBookingOrderByModelCode(String modelCode);

    @Query("SELECT COUNT(c) FROM BookingOrder c WHERE " +
            "((:orderNo) IS Null OR c.orderNo = :orderNo )" +
            " AND ((:regions) IS Null OR c.region.region IN (:regions) )" +
            " AND ((:plants) IS NULL OR c.productDimension.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(c.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR c.productDimension.clazz IN (:classes))" +
            " AND ((:models) IS NULL OR c.productDimension.model IN (:models))" +
            " AND ((:segments) IS NULL OR c.productDimension.segment IN (:segments))" +
            " AND ((:dealerName) IS NULL OR c.dealerName IN (:dealerName))" +
            " AND ((:AOPMarginPercentage) IS NULL OR " +
            "   (:AOPMarginPercentage = 'Above AOP Margin %' AND c.AOPMarginPercentage < c.marginPercentageAfterSurCharge) OR" +
            "   (:AOPMarginPercentage = 'Below AOP Margin %' AND c.AOPMarginPercentage >= c.marginPercentageAfterSurCharge))" +
            " AND ((:marginPercentageAfterSurCharge) IS NULL OR " +
            "   (:comparator = '<=' AND c.marginPercentageAfterSurCharge <= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>=' AND c.marginPercentageAfterSurCharge >= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '<' AND c.marginPercentageAfterSurCharge < :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>' AND c.marginPercentageAfterSurCharge > :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '=' AND c.marginPercentageAfterSurCharge = :marginPercentageAfterSurCharge))" +
            " AND ((:dealerName) IS NULL OR c.dealerName IN (:dealerName))" +
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
                 @Param("fromDate") Calendar fromDate,
                 @Param("toDate") Calendar toDate);

    @Query("SELECT c FROM BookingOrder c WHERE " +
            "((:orderNo) IS Null OR c.orderNo = :orderNo )" +
            " AND ((:regions) IS Null OR c.region.region IN (:regions) )" +
            " AND ((:plants) IS NULL OR c.productDimension.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(c.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR c.productDimension.clazz IN (:classes))" +
            " AND ((:models) IS NULL OR c.model IN (:models))" +
            " AND ((:segments) IS NULL OR c.productDimension.segment IN (:segments))" +
            " AND ((:dealerName) IS NULL OR c.dealerName IN (:dealerName))" +
            " AND ((:AOPMarginPercentage) IS NULL OR " +
            "   (:AOPMarginPercentage = 'Above AOP Margin %' AND c.AOPMarginPercentage < c.marginPercentageAfterSurCharge) OR" +
            "   (:AOPMarginPercentage = 'Below AOP Margin %' AND c.AOPMarginPercentage >= c.marginPercentageAfterSurCharge))" +
            " AND ((:marginPercentageAfterSurCharge) IS NULL OR " +
            "   (:comparator = '<=' AND c.marginPercentageAfterSurCharge <= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>=' AND c.marginPercentageAfterSurCharge >= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '<' AND c.marginPercentageAfterSurCharge < :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>' AND c.marginPercentageAfterSurCharge > :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '=' AND c.marginPercentageAfterSurCharge = :marginPercentageAfterSurCharge))" +
            " AND ((:dealerName) IS NULL OR c.dealerName IN (:dealerName))" +
            " AND (cast(:fromDate as date ) IS NULL OR c.date >= :fromDate)" +
            " AND (cast(:toDate as date) IS NULL OR c.date <= :toDate) ORDER BY c.orderNo"
    )
    List<BookingOrder> selectAllForBookingOrder(
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
            @Param("fromDate") Calendar fromDate,
            @Param("toDate") Calendar toData,
            @Param("pageable") Pageable pageable
    );

    @Query("SELECT b from BookingOrder b where b.orderNo IN :listOrderNo")
    List<BookingOrder> getListBookingExist(List<String> listOrderNo);

    BookingOrder findByOrderNo(String orderNo);

    @Query("SELECT new com.hysteryale.model.TrendData( EXTRACT(month FROM b.date) as month, " +
            "AVG(b.marginPercentageAfterSurCharge) as marginPercentage, " +
            "AVG(b.totalCost) as costOrDealerNet ) " +
            "FROM BookingOrder b WHERE " +
            " ((:regions) IS NULL OR b.region.region IN (:regions) )" +
            " AND ((:plants) IS NULL OR b.productDimension.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(b.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR b.productDimension.clazz IN (:classes))" +
            " AND ((:models) IS NULL OR b.model IN (:models))" +
            " AND ((:segments) IS NULL OR b.productDimension.segment IN (:segments))" +
            " AND ((:dealerName) IS NULL OR b.dealerName IN (:dealerName)) " +
            " AND EXTRACT(year FROM b.date) = :year" +
            " AND b.marginPercentageAfterSurCharge != 'NaN'" +
            " AND b.marginPercentageAfterSurCharge != '-Infinity'" +
            " AND b.marginPercentageAfterSurCharge != 'Infinity'" +
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
            "AVG(b.marginPercentageAfterSurCharge) as marginPercentage, " +
            "AVG(b.dealerNet) as costOrDealerNet ) " +
            "FROM BookingOrder b WHERE " +
            " ((:regions) IS NULL OR b.region.region IN (:regions) )" +
            " AND ((:plants) IS NULL OR b.productDimension.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(b.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR b.productDimension.clazz IN (:classes))" +
            " AND ((:models) IS NULL OR b.model IN (:models))" +
            " AND ((:segments) IS NULL OR b.productDimension.segment IN (:segments))" +
            " AND ((:dealerName) IS NULL OR b.dealerName IN (:dealerName)) " +
            " AND EXTRACT(year FROM b.date) = :year" +
            " AND b.marginPercentageAfterSurCharge != 'NaN'" +
            " AND b.marginPercentageAfterSurCharge != '-Infinity'" +
            " AND b.marginPercentageAfterSurCharge != 'Infinity'" +
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


    @Query("SELECT new BookingOrder(c.region.region, c.productDimension.plant, c.productDimension.clazz, c.series, c.model, " +
            "sum(c.totalCost), sum(c.dealerNetAfterSurCharge), sum(c.marginAfterSurCharge), count(c)) " +
            " FROM BookingOrder c WHERE " +
            " ((:regions) IS Null OR c.region.region IN (:regions))" +
            " AND ((:plants) IS NULL OR c.productDimension.plant IN (:plants))" +
            " AND ((:segments) IS NULL OR c.productDimension.segment IN (:segments))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(c.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR c.productDimension.clazz IN (:classes))" +
            " AND ((:models) IS NULL OR c.model IN (:models))" +
            " AND ((:dealerName) IS NULL OR c.dealerName IN (:dealerName))" +
            " AND ((:marginPercentageAfterSurCharge) IS NULL OR " +
            "   (:comparator = '<=' AND c.marginPercentageAfterSurCharge <= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>=' AND c.marginPercentageAfterSurCharge >= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '<' AND c.marginPercentageAfterSurCharge < :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>' AND c.marginPercentageAfterSurCharge > :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '=' AND c.marginPercentageAfterSurCharge = :marginPercentageAfterSurCharge))" +
            " GROUP BY c.region.region, c.productDimension.plant, c.productDimension.clazz, c.series, c.model " +
            " HAVING (:marginPercentageAfterSurChargeAfterAdj) IS NULL OR " +
            "   (:comparatorAfterAdj = '<' AND sum(c.dealerNetAfterSurCharge) <> 0 AND ((sum(c.dealerNetAfterSurCharge) * (1 + :dnAdjPercentage / 100.0) - (sum(c.totalCost) * (1 + :costAdjPercentage/100.0) - :freightAdj - :fxAdj)) / (sum(c.dealerNetAfterSurCharge) * (1 + :dnAdjPercentage / 100.0))) < :marginPercentageAfterSurChargeAfterAdj) OR" +
            "   (:comparatorAfterAdj = '>=' AND sum(c.dealerNetAfterSurCharge) <> 0 AND ((sum(c.dealerNetAfterSurCharge) * (1 + :dnAdjPercentage / 100.0) - (sum(c.totalCost) * (1 + :costAdjPercentage/100.0) - :freightAdj - :fxAdj)) / (sum(c.dealerNetAfterSurCharge) * (1 + :dnAdjPercentage / 100.0))) > :marginPercentageAfterSurChargeAfterAdj)"
    )
    List<BookingOrder> selectForAdjustmentByFilter(
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
            " FROM BookingOrder c WHERE " +
            " ((:regions) IS Null OR c.region.region IN (:regions))" +
            " AND ((:plants) IS NULL OR c.productDimension.plant IN (:plants))" +
            " AND ((:segments) IS NULL OR c.productDimension.segment IN (:segments))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(c.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR c.productDimension.clazz IN (:classes))" +
            " AND ((:models) IS NULL OR c.model IN (:models))" +
            " AND ((:dealerName) IS NULL OR c.dealerName IN (:dealerName))" +
            " AND ((:marginPercentageAfterSurCharge) IS NULL OR " +
            "   (:comparator = '<=' AND c.marginPercentageAfterSurCharge <= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>=' AND c.marginPercentageAfterSurCharge >= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '<' AND c.marginPercentageAfterSurCharge < :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>' AND c.marginPercentageAfterSurCharge > :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '=' AND c.marginPercentageAfterSurCharge = :marginPercentageAfterSurCharge))" +
            " GROUP BY c.region.region, c.productDimension.plant, c.productDimension.clazz, c.series, c.model" +
            " HAVING (:marginPercentageAfterSurChargeAfterAdj) IS NULL OR " +
            "   (:comparatorAfterAdj = '<' AND sum(c.dealerNetAfterSurCharge) <> 0 AND (sum(c.dealerNetAfterSurCharge) * (1 + :dnAdjPercentage / 100.0) - (sum(c.totalCost) * (1 + :costAdjPercentage/100.0) - :freightAdj - :fxAdj)) / (sum(c.dealerNetAfterSurCharge) * (1 + :dnAdjPercentage / 100.0)) < :marginPercentageAfterSurChargeAfterAdj) OR" +
            "   (:comparatorAfterAdj = '>=' AND sum(c.dealerNetAfterSurCharge) <> 0 AND (sum(c.dealerNetAfterSurCharge) * (1 + :dnAdjPercentage / 100.0) - (sum(c.totalCost) * (1 + :costAdjPercentage/100.0) - :freightAdj - :fxAdj)) / (sum(c.dealerNetAfterSurCharge) * (1 + :dnAdjPercentage / 100.0)) >= :marginPercentageAfterSurChargeAfterAdj)"
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

    @Query("SELECT new BookingOrder('Total', COALESCE(sum(c.quantity),0), COALESCE(sum(c.dealerNet),0), COALESCE(sum(c.dealerNetAfterSurCharge),0), COALESCE(sum(c.totalCost),0), COALESCE(sum(c.marginAfterSurCharge),0), COALESCE((sum(c.marginAfterSurCharge) / sum(c.dealerNetAfterSurCharge)),0 )) FROM BookingOrder c WHERE " +
            "((:orderNo) IS Null OR c.orderNo = :orderNo )" +
            " AND ((:regions) IS Null OR c.region.region IN (:regions) )" +
            " AND ((:plants) IS NULL OR c.productDimension.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(c.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR c.productDimension.clazz IN (:classes))" +
            " AND ((:models) IS NULL OR c.model IN (:models))" +
            " AND ((:segments) IS NULL OR c.productDimension.segment IN (:segments))" +
            " AND ((:dealerName) IS NULL OR c.dealerName IN (:dealerName))" +
            " AND ((:AOPMarginPercentage) IS NULL OR " +
            "   (:AOPMarginPercentage = 'Above AOP Margin %' AND c.AOPMarginPercentage < c.marginPercentageAfterSurCharge) OR" +
            "   (:AOPMarginPercentage = 'Below AOP Margin %' AND c.AOPMarginPercentage >= c.marginPercentageAfterSurCharge))" +
            " AND ((:marginPercentageAfterSurCharge) IS NULL OR " +
            "   (:comparator = '<=' AND c.marginPercentageAfterSurCharge <= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>=' AND c.marginPercentageAfterSurCharge >= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '<' AND c.marginPercentageAfterSurCharge < :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>' AND c.marginPercentageAfterSurCharge > :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '=' AND c.marginPercentageAfterSurCharge = :marginPercentageAfterSurCharge))" +
            " AND ((:dealerName) IS NULL OR c.dealerName IN (:dealerName))" +
            " AND (cast(:fromDate as date ) IS NULL OR c.date >= :fromDate)" +
            " AND (cast(:toDate as date) IS NULL OR c.date <= :toDate)"
    )
    List<BookingOrder> getTotal(
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
            @Param("fromDate") Calendar fromDate,
            @Param("toDate") Calendar toDate);

    @Query("SELECT COALESCE((sum(c.marginAfterSurCharge) / NULLIF( sum(c.dealerNetAfterSurCharge),0)),0) FROM BookingOrder c WHERE " +
            "((:orderNo) IS Null OR c.orderNo = :orderNo )" +
            " AND ((:regions) IS Null OR c.region.region IN (:regions) )" +
            " AND ((:plants) IS NULL OR c.productDimension.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(c.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR c.productDimension.clazz IN (:classes))" +
            " AND ((:models) IS NULL OR c.model IN (:models))" +
            " AND ((:segments) IS NULL OR c.productDimension.segment IN (:segments))" +
            " AND ((:dealerName) IS NULL OR c.dealerName IN (:dealerName))" +
            " AND ((:AOPMarginPercentage) IS NULL OR " +
            "   (:AOPMarginPercentage = 'Above AOP Margin %' AND c.AOPMarginPercentage < c.marginPercentageAfterSurCharge) OR" +
            "   (:AOPMarginPercentage = 'Below AOP Margin %' AND c.AOPMarginPercentage >= c.marginPercentageAfterSurCharge))" +
            " AND ((:marginPercentageAfterSurCharge) IS NULL OR " +
            "   (:comparator = '<=' AND c.marginPercentageAfterSurCharge <= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>=' AND c.marginPercentageAfterSurCharge >= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '<' AND c.marginPercentageAfterSurCharge < :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>' AND c.marginPercentageAfterSurCharge > :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '=' AND c.marginPercentageAfterSurCharge = :marginPercentageAfterSurCharge))" +
            " AND ((:dealerName) IS NULL OR c.dealerName IN (:dealerName))" +
            " AND (cast(:fromDate as date ) IS NULL OR c.date >= :fromDate)" +
            " AND (cast(:toDate as date) IS NULL OR c.date <= :toDate)"
    )
    double getTotalMarginPercentage(
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
            @Param("fromDate") Calendar fromDate,
            @Param("toDate") Calendar toDate);


    @Query("SELECT new BookingOrder( sum(c.dealerNetAfterSurCharge), sum(c.totalCost), sum(c.marginAfterSurCharge), count(c)) " +
            " FROM BookingOrder c WHERE " +
            " ((:regions) IS Null OR c.region.region IN (:regions))" +
            " AND ((:plants) IS NULL OR c.productDimension.plant IN (:plants))" +
            " AND ((:segments) IS NULL OR c.productDimension.segment IN (:segments))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(c.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR c.productDimension.clazz IN (:classes))" +
            " AND ((:models) IS NULL OR c.model IN (:models))" +
            " AND ((:dealerName) IS NULL OR c.dealerName IN (:dealerName))" +
            " AND ((:marginPercentageAfterSurCharge) IS NULL OR " +
            "   (:comparator = '<=' AND c.marginPercentageAfterSurCharge <= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>=' AND c.marginPercentageAfterSurCharge >= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '<' AND c.marginPercentageAfterSurCharge < :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>' AND c.marginPercentageAfterSurCharge > :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '=' AND c.marginPercentageAfterSurCharge = :marginPercentageAfterSurCharge))" +
            " GROUP BY c.region.region, c.productDimension.plant, c.productDimension.clazz, c.series, c.model" +
            " HAVING (:marginPercentageAfterSurChargeAfterAdj) IS NULL OR " +
            "   (:comparatorAfterAdj = '<' AND sum(c.dealerNetAfterSurCharge) <> 0 AND (sum(c.dealerNetAfterSurCharge) * (1 + :dnAdjPercentage / 100.0) - (sum(c.totalCost) * (1 + :costAdjPercentage/100.0) - :freightAdj - :fxAdj)) / (sum(c.dealerNetAfterSurCharge) * (1 + :dnAdjPercentage / 100.0)) < :marginPercentageAfterSurChargeAfterAdj) OR" +
            "   (:comparatorAfterAdj = '>=' AND sum(c.dealerNetAfterSurCharge) <> 0 AND (sum(c.dealerNetAfterSurCharge) * (1 + :dnAdjPercentage / 100.0) - (sum(c.totalCost) * (1 + :costAdjPercentage/100.0) - :freightAdj - :fxAdj)) / (sum(c.dealerNetAfterSurCharge) * (1 + :dnAdjPercentage / 100.0)) >= :marginPercentageAfterSurChargeAfterAdj)"

    )
    List<BookingOrder> selectTotalForAdjustment(
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


}
