package com.hysteryale.repository;

import com.hysteryale.model.Shipment;
import com.hysteryale.model.TrendData;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, String> {

    @Query("SELECT c FROM Shipment c WHERE " +
            "((:orderNo) IS Null OR LOWER(c.orderNo) LIKE LOWER(CONCAT('%', :orderNo, '%')))" +
            " AND ((:regions) IS Null OR c.country IS NULL OR COALESCE(c.country.region.regionName, NULL) IN (:regions) )" +
            " AND ((:plants) IS NULL OR c.product.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(c.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR c.product.clazz.clazzName IN (:classes))" +
            " AND ((:models) IS NULL OR c.product.modelCode IN (:models))" +
            " AND ((:segments) IS NULL OR c.product.segment IN (:segments))" +
            " AND ((:dealerName) IS NULL OR c.dealer.name IN (:dealerName))" +
            " AND ((:dealerId) IS NULL OR c.dealer.id = (:dealerId))" +
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
            " AND (cast(:toDate as date) IS NULL OR c.date <= :toDate) ORDER BY  similarity(c.orderNo, :orderNo) DESC "
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
                                                @Param("pageable") Pageable pageable,
                                                @Param("dealerId") Integer dealerId);


    @Query("SELECT COUNT(c) FROM Shipment c WHERE " +
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

    @Query("SELECT s FROM Shipment s WHERE s.orderNo = :orderNo")
    Optional<Shipment> findShipmentByOrderNo(String orderNo);

    @Query("SELECT new com.hysteryale.model.TrendData( EXTRACT(month FROM b.date) as month, " +
            "AVG(b.marginPercentageAfterSurcharge) as marginPercentage, " +
            "AVG(b.totalCost) as costOrDealerNet ) " +
            "FROM Shipment b WHERE " +
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
    List<TrendData> getMarginVsCostData(@Param("regions") List<String> regions,
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
            "FROM Shipment b WHERE " +
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

    @Query("SELECT new Shipment(c.orderNo, c.currency, c.dealerNet, c.dealerNetAfterSurcharge, c.totalCost, c.netRevenue, c.bookingDealerNetAfterSurcharge, c.bookingMarginAfterSurcharge) FROM Shipment c WHERE " +
            "((:orderNo) IS Null OR LOWER(c.orderNo) LIKE LOWER(CONCAT('%', :orderNo, '%')))" +
            " AND ((:regions) IS Null OR c.country.region.regionName IN (:regions) )" +
            " AND ((:plants) IS NULL OR c.product.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(c.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR c.product.clazz.clazzName IN (:classes))" +
            " AND ((:models) IS NULL OR c.product.modelCode IN (:models))" +
            " AND ((:segments) IS NULL OR c.product.segment IN (:segments))" +
            " AND ((:dealerName) IS NULL OR c.dealer.name IN (:dealerName))" +
            " AND ((:dealerId) IS NULL OR c.dealer.id = (:dealerId))" +
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
                            @Param("toDate") LocalDate toDate,
                            @Param("dealerId") Integer dealerId);

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
                    "   null as serial_number, " +
                    "   null as product, " +
                    "   null as region, " +
                    "   null as model, " +
                    "   coalesce(total_cost, 0) as total_cost, " +
                    "   coalesce(dealer_net, 0) as dealer_net, " +
                    "   coalesce(netrevenue, 0) as netrevenue, " +
                    "   coalesce(dealer_net_after_surcharge, 0) as dealer_net_after_surcharge, " +
                    "   coalesce(dealer_net_after_surcharge - total_cost, 0)as margin_after_surcharge, " +
                    "   coalesce((dealer_net_after_surcharge - total_cost) / nullif(dealer_net_after_surcharge, 0), 0) as margin_percentage_after_surcharge, " +
                    "   coalesce(quantity, 0) as quantity, " +
                    "   0 as booking_margin_percentage_after_surcharge " +
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
                    "                when bo.currency = 'USD' then bo.dealer_net_after_surcharge " +
                    "                when bo.currency is not null then coalesce(er.latest_rate, 1) * bo.dealer_net_after_surcharge " +
                    "            end " +
                    "        ) as dealer_net_after_surcharge, " +
                    "       sum(bo.quantity) as quantity " +
                    "    from " +
                    "       shipment bo " +
                    "   left join exchange_rate_cte er on " +
                    "       bo.currency = er.from_currency " +
                    "   left join product pd on " +
                    "       bo.product = pd.modelcode " +
                    "   left join region r on " +
                    "       bo.region = r.id " +
                    "   where " +
                    "      (:orderNo is null or lower(bo.order_no) = lower( :orderNo))  " +
                    "       and ((:regions) is null or r.region_name in (:regions)) " +
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
                    "           (:AOPMarginPercentage = 'Above AOP Margin %' and bo.margin_percentage_after_surcharge > bo.aopmargin ) or " +
                    "           (:AOPMarginPercentage = 'Below AOP Margin %' and bo.margin_percentage_after_surcharge <= bo.aopmargin ) )" +
                    "       and ( bo.date >= :fromDate) " +
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

    @Query(value = "SELECT m.latest_modified_at FROM shipment m WHERE m.latest_modified_at is not null ORDER BY m.latest_modified_at DESC LIMIT 1", nativeQuery = true)
    Optional<LocalDateTime> getLatestUpdatedTime();

    @Query("SELECT s FROM Shipment s WHERE " +
            " (:orderNos) IS NULL OR s.orderNo IN (:orderNos)")
    List<Shipment> getShipmentByOrderNos(List<String> orderNos);
}
