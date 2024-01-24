package com.hysteryale.repository;

import com.hysteryale.model.Shipment;
import com.hysteryale.model.TrendData;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, String> {

    @Query("SELECT c FROM Shipment c WHERE " +
            "((:orderNo) IS Null OR c.orderNo = :orderNo )" +
            " AND ((:regions) IS Null OR c.region IS NULL OR COALESCE(c.region.region, NULL) IN (:regions) )" +
            " AND ((:plants) IS NULL OR c.productDimension.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(c.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR c.productDimension.clazz IN (:classes))" +
            " AND ((:models) IS NULL OR c.productDimension.modelCode IN (:models))" +
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
    List<Shipment> findShipmentByFilterForTable(@Param("orderNo") String orderNo,
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
                                                @Param("toDate") LocalDate toDate,
                                                @Param("pageable") Pageable pageable);


    @Query("SELECT COUNT(c) FROM Shipment c WHERE " +
            "((:orderNo) IS Null OR c.orderNo = :orderNo )" +
            " AND ((:regions) IS Null OR c.region.region IN (:regions) )" +
            " AND ((:plants) IS NULL OR c.productDimension.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(c.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR c.productDimension.clazz IN (:classes))" +
            " AND ((:models) IS NULL OR c.productDimension.modelCode IN (:models))" +
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
                 @Param("fromDate") LocalDate fromDate,
                 @Param("toDate") LocalDate toDate);

    @Query("SELECT DISTINCT s.dealerName from Shipment s WHERE s.dealerName IS NOT NULL")
    List<String> findAllDealerName();


    @Query("SELECT s FROM Shipment s WHERE s.orderNo = :orderNo")
    Optional<Shipment> findShipmentByOrderNo(String orderNo);

    @Query("SELECT new com.hysteryale.model.TrendData( EXTRACT(month FROM b.date) as month, " +
            "AVG(b.marginPercentageAfterSurCharge) as marginPercentage, " +
            "AVG(b.totalCost) as costOrDealerNet ) " +
            "FROM Shipment b WHERE " +
            " ((:regions) IS NULL OR b.region.region IN (:regions) )" +
            " AND ((:plants) IS NULL OR b.productDimension.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(b.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR b.productDimension.clazz IN (:classes))" +
            " AND ((:models) IS NULL OR b.productDimension.modelCode IN (:models))" +
            " AND ((:segments) IS NULL OR b.productDimension.segment IN (:segments))" +
            " AND ((:dealerName) IS NULL OR b.dealerName IN (:dealerName)) " +
            " AND EXTRACT(year FROM b.date) = :year" +
            " AND b.marginPercentageAfterSurCharge != 'NaN'" +
            " AND b.marginPercentageAfterSurCharge != '-Infinity'" +
            " AND b.marginPercentageAfterSurCharge != 'Infinity'" +
            " GROUP BY EXTRACT(month FROM b.date) ORDER BY month ASC"
    )
    List<TrendData> getMarginVsCostData(@Param("regions") List<String> regions,
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
            "FROM Shipment b WHERE " +
            " ((:regions) IS NULL OR b.region.region IN (:regions) )" +
            " AND ((:plants) IS NULL OR b.productDimension.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(b.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR b.productDimension.clazz IN (:classes))" +
            " AND ((:models) IS NULL OR b.productDimension.modelCode IN (:models))" +
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

    @Query("SELECT new Shipment('Total', COALESCE(sum(c.quantity),0), COALESCE(sum(c.dealerNet),0), COALESCE(sum(c.dealerNetAfterSurCharge),0), COALESCE(sum(c.totalCost),0), COALESCE(sum(c.netRevenue),0), COALESCE(sum(c.marginAfterSurCharge),0), COALESCE((sum(c.marginAfterSurCharge) / sum(c.dealerNetAfterSurCharge)),0) ) FROM Shipment c WHERE " +
            "((:orderNo) IS Null OR c.orderNo = :orderNo )" +
            " AND ((:regions) IS Null OR c.region.region IN (:regions) )" +
            " AND ((:plants) IS NULL OR c.productDimension.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(c.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR c.productDimension.clazz IN (:classes))" +
            " AND ((:models) IS NULL OR c.productDimension.modelCode IN (:models))" +
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
    List<Shipment> getTotal(@Param("orderNo") String orderNo,
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
                    "   0 as aopmargin_percentage, " +
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
                    "   null as serial_number, " +
                    "   null as product_dimension, " +
                    "   null as region, " +
                    "   null as model, " +
                    "   coalesce(total_cost, 0) as total_cost, " +
                    "   coalesce(dealer_net, 0) as dealer_net, " +
                    "   coalesce(netrevenue, 0) as netrevenue, " +
                    "   coalesce(dealer_net_after_sur_charge, 0) as dealer_net_after_sur_charge, " +
                    "   coalesce(dealer_net_after_sur_charge - total_cost, 0)as margin_after_sur_charge, " +
                    "   coalesce((dealer_net_after_sur_charge - total_cost) / nullif(dealer_net_after_sur_charge, 0), 0) as margin_percentage_after_sur_charge, " +
                    "   coalesce(quantity, 0) as quantity, " +
                    "   0 as booking_margin_percentage_after_sur_charge " +
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
                    "       sum( " +
                    "            case " +
                    "                when bo.currency = 'USD' then bo.netrevenue " +
                    "                when bo.currency is not null then coalesce(er.latest_rate, 1) * bo.netrevenue " +
                    "            end " +
                    "        ) as netrevenue, " +
                    "       sum(" +
                    "            case " +
                    "                when bo.currency = 'USD' then bo.dealer_net_after_sur_charge " +
                    "                when bo.currency is not null then coalesce(er.latest_rate, 1) * bo.dealer_net_after_sur_charge " +
                    "            end " +
                    "        ) as dealer_net_after_sur_charge, " +
                    "       sum(bo.quantity) as quantity " +
                    "    from " +
                    "       shipment bo " +
                    "   left join exchange_rate_cte er on " +
                    "       bo.currency = er.from_currency " +
                    "   left join productdimension pd on " +
                    "       bo.product_dimension = pd.modelcode " +
                    "   left join region r on " +
                    "       bo.region = r.id " +
                    "   where " +
                    "      (:orderNo is null or lower(bo.order_no) = lower( :orderNo))  " +
                    "       and ((:regions) is null or r.region in (:regions)) " +
                    "       and ((:dealerNames) is null or bo.dealer_name in (:dealerNames)) " +
                    "       and ((:classes) is null or pd.clazz in (:classes)) " +
                    "       and ((:plants) is null or pd.plant in (:plants)) " +
                    "       and ((:segments) is null or pd.segment in (:segments)) " +
                    "       and ((:metaSeries) is null or substring(bo.series, 2, 3) in (:metaSeries)) " +
                    "       and ((:models) is null or pd.modelcode in (:models)) " +
                    "       AND ((:marginPercentageAfterSurCharge) IS NULL OR " +
                    "           (:comparator = '<=' AND bo.margin_percentage_after_sur_charge <= :marginPercentageAfterSurCharge) OR" +
                    "           (:comparator = '>=' AND bo.margin_percentage_after_sur_charge >= :marginPercentageAfterSurCharge) OR" +
                    "           (:comparator = '<' AND bo.margin_percentage_after_sur_charge < :marginPercentageAfterSurCharge) OR" +
                    "           (:comparator = '>' AND bo.margin_percentage_after_sur_charge > :marginPercentageAfterSurCharge) OR" +
                    "           (:comparator = '=' AND bo.margin_percentage_after_sur_charge = :marginPercentageAfterSurCharge))" +
                    "       and ((:AOPMarginPercentage) IS NULL or " +
                    "           (:AOPMarginPercentage = 'Above AOP Margin %' and bo.margin_percentage_after_sur_charge > bo.aopmargin_percentage ) or " +
                    "           (:AOPMarginPercentage = 'Below AOP Margin %' and bo.margin_percentage_after_sur_charge <= bo.aopmargin_percentage ) )" +
                    "       and ( bo.date >= :fromDate) "+
                    "       and ( bo.date <= :toDate) " +
                    ") as subquery; ", nativeQuery = true)
    List<Shipment> getTotalRowForShipmentPage(
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
}
