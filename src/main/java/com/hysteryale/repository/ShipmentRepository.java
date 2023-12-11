package com.hysteryale.repository;

import com.hysteryale.model.BookingOrder;
import com.hysteryale.model.Shipment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, String> {

    @Query("SELECT c FROM Shipment c WHERE " +
            "((:orderNo) IS Null OR c.orderNo = :orderNo )" +
            " AND ((:regions) IS Null OR c.region IS NULL OR COALESCE(c.region.region, NULL) IN (:regions) )" +
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
            " AND (cast(:fromDate as date ) IS NULL OR c.date >= :fromDate)" +
            " AND (cast(:toDate as date) IS NULL OR c.date <= :toDate) ORDER BY c.orderNo"
    )
    List<Shipment> findShipmentByFilterForTable(@Param("orderNo") Object orderNo,
                                                @Param("regions") Object regions,
                                                @Param("plants") Object plants,
                                                @Param("metaSeries") Object metaSeries,
                                                @Param("classes") Object classes,
                                                @Param("models") Object models,
                                                @Param("segments") Object segments,
                                                @Param("dealerName") Object dealerName,
                                                @Param("AOPMarginPercentage") Object AOPMarginPercentage,
                                                @Param("comparator") Object comparator,
                                                @Param("marginPercentageAfterSurCharge") Object marginPercentageAfterSurCharge,
                                                @Param("fromDate") Calendar fromDate,
                                                @Param("toDate") Calendar toDate,
                                                @Param("pageable") Pageable pageable);


    @Query("SELECT COUNT(c) FROM Shipment c WHERE " +
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
    int getCount(@Param("orderNo") Object orderNo,
                 @Param("regions") Object regions,
                 @Param("plants") Object plants,
                 @Param("metaSeries") Object metaSeries,
                 @Param("classes") Object classes,
                 @Param("models") Object models,
                 @Param("segments") Object segments,
                 @Param("dealerName") Object dealerName,
                 @Param("AOPMarginPercentage") Object AOPMarginPercentage,
                 @Param("comparator") Object comparator,
                 @Param("marginPercentageAfterSurCharge") Object marginPercentageAfterSurCharge,
                 @Param("fromDate") Calendar fromDate,
                 @Param("toDate") Calendar toDate);

    @Query("SELECT DISTINCT s.dealerName from Shipment s WHERE s.dealerName IS NOT NULL")
    List<String> findAllDealerName();


    @Query("SELECT s FROM Shipment s WHERE s.orderNo = :orderNo")
    Optional<Shipment> findShipmentByOrderNo(String orderNo);

    @Query("SELECT new Shipment('Total', sum(c.quantity), sum(c.dealerNet), sum(c.dealerNetAfterSurCharge), sum(c.totalCost), sum(c.netRevenue), sum(c.marginAfterSurCharge), (sum(c.marginAfterSurCharge) / sum(c.dealerNetAfterSurCharge)) ) FROM Shipment c WHERE " +
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
    List<Shipment> getTotal(@Param("orderNo") Object orderNo,
                                @Param("regions") Object regions,
                                @Param("plants") Object plants,
                                @Param("metaSeries") Object metaSeries,
                                @Param("classes") Object classes,
                                @Param("models") Object models,
                                @Param("segments") Object segments,
                                @Param("dealerName") Object dealerName,
                                @Param("AOPMarginPercentage") Object AOPMarginPercentage,
                                @Param("comparator") Object comparator,
                                @Param("marginPercentageAfterSurCharge") Object marginPercentageAfterSurCharge,
                                @Param("fromDate") Calendar fromDate,
                                @Param("toDate") Calendar toDate);
}
