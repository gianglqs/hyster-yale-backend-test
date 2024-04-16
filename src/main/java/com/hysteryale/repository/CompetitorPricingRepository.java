/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.repository;

import com.hysteryale.model.competitor.CompetitorPricing;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CompetitorPricingRepository extends JpaRepository<CompetitorPricing, Integer> {

    @Query("SELECT c.country.countryName, c.clazz.clazzName, c.category, c.series FROM CompetitorPricing c GROUP BY c.country.countryName, c.clazz.clazzName, c.category, c.series")
    List<String[]> getCompetitorGroup();

    @Query("SELECT c FROM CompetitorPricing c WHERE c.country.countryName = ?1 AND c.clazz.clazzName = ?2 AND c.category = ?3 AND c.series = ?4")
    List<CompetitorPricing> getListOfCompetitorInGroup(String country, String clazz, String category, String series);

    @Query("SELECT new com.hysteryale.model.competitor.CompetitorPricing(c.country.region.regionName, SUM(c.actual), SUM(c.AOPF), SUM(c.LRFF))" +
            " FROM CompetitorPricing c WHERE " +
            " c.country.region.regionName IS NOT NULL " +
            " AND ((:regions) IS Null OR c.country.region.regionName IN (:regions))" +
            " AND ((:plants) IS NULL OR c.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(c.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR c.clazz.clazzName IN (:classes))" +
            " AND ((:models) IS NULL OR c.model IN (:models))" +
            " AND ((:marginPercentageAfterSurCharge) IS NULL OR " +
            "   (:comparator = '<=' AND c.dealerPricingPremiumPercentage <= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>=' AND c.dealerPricingPremiumPercentage >= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '<' AND c.dealerPricingPremiumPercentage < :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>' AND c.dealerPricingPremiumPercentage > :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '=' AND c.dealerPricingPremiumPercentage = :marginPercentageAfterSurCharge))" +
            " AND ((:chineseBrand) IS NULL OR c.chineseBrand = (:chineseBrand)) GROUP BY c.country.region.regionName")
    List<CompetitorPricing> findCompetitorByFilterForLineChartRegion(
            @Param("regions") List<String> regions,
            @Param("plants") List<String> plants,
            @Param("metaSeries") List<String> metaSeries,
            @Param("classes") List<String> classes,
            @Param("models") List<String> models,
            @Param("chineseBrand") Boolean chineseBrand,
            @Param("comparator") String comparator,
            @Param("marginPercentageAfterSurCharge") Double marginPercentageAfterSurCharge);

    @Query("SELECT new com.hysteryale.model.competitor.CompetitorPricing( SUM(c.actual), SUM(c.AOPF), SUM(c.LRFF),c.plant)" +
            " FROM CompetitorPricing c WHERE " +
            " c.plant IS NOT NULL " +
            " AND ((:regions) IS Null OR c.country.region.regionName IN (:regions))" +
            " AND ((:plants) IS NULL OR c.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(c.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR c.clazz.clazzName IN (:classes))" +
            " AND ((:models) IS NULL OR c.model IN (:models))" +
            " AND ((:marginPercentageAfterSurCharge) IS NULL OR " +
            "   (:comparator = '<=' AND c.dealerPricingPremiumPercentage <= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>=' AND c.dealerPricingPremiumPercentage >= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '<' AND c.dealerPricingPremiumPercentage < :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>' AND c.dealerPricingPremiumPercentage > :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '=' AND c.dealerPricingPremiumPercentage = :marginPercentageAfterSurCharge))" +
            " AND ((:chineseBrand) IS NULL OR c.chineseBrand = (:chineseBrand)) GROUP BY c.plant")
     List<CompetitorPricing> findCompetitorByFilterForLineChartPlant(
            @Param("regions") List<String> regions,
            @Param("plants") List<String> plants,
            @Param("metaSeries") List<String> metaSeries,
            @Param("classes") List<String> classes,
            @Param("models") List<String> models,
            @Param("chineseBrand") Boolean chineseBrand,
            @Param("comparator") String comparator,
            @Param("marginPercentageAfterSurCharge") Double marginPercentageAfterSurCharge);

    @Query("SELECT c FROM CompetitorPricing c WHERE " +
            "((:regions) IS Null OR c.country.region.regionName IN (:regions))" +
            " AND ((:plants) IS NULL OR c.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(c.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR c.clazz.clazzName IN (:classes))" +
            " AND ((:models) IS NULL OR c.model IN (:models))" +
            " AND ((:marginPercentageAfterSurCharge) IS NULL OR " +
            "   (:comparator = '<=' AND c.dealerPricingPremiumPercentage <= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>=' AND c.dealerPricingPremiumPercentage >= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '<' AND c.dealerPricingPremiumPercentage < :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>' AND c.dealerPricingPremiumPercentage > :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '=' AND c.dealerPricingPremiumPercentage = :marginPercentageAfterSurCharge))" +
            " AND ((:chineseBrand) IS NULL OR c.chineseBrand = (:chineseBrand))")
    List<CompetitorPricing> findCompetitorByFilterForTable(
            @Param("regions") List<String> regions,
                                                           @Param("plants") List<String> plants,
                                                           @Param("metaSeries") List<String> metaSeries,
                                                           @Param("classes") List<String> classes,
                                                           @Param("models") List<String> models,
                                                           @Param("chineseBrand") Boolean chineseBrand,
                                                           @Param("comparator") String comparator,
                                                           @Param("marginPercentageAfterSurCharge") Double marginPercentageAfterSurCharge,
                                                           Pageable pageable);

    @Query("SELECT new CompetitorPricing('Total', COALESCE(sum(c.actual),0), COALESCE(sum(c.AOPF),0), COALESCE(sum(c.LRFF),0), COALESCE(sum(c.dealerHandlingCost),0), COALESCE(sum(c.competitorPricing),0), " +
            " COALESCE(sum(c.dealerStreetPricing),0),  COALESCE(sum(c.averageDN),0) , " +
            " COALESCE((sum(c.competitorPricing) - (sum(c.dealerStreetPricing) + sum(c.dealerPricingPremium))) / sum(c.competitorPricing),0) )" +
            " FROM CompetitorPricing c WHERE " +
            "((:regions) IS Null OR c.country.region.regionName IN (:regions))" +
            " AND ((:plants) IS NULL OR c.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(c.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR c.clazz.clazzName IN (:classes))" +
            " AND ((:models) IS NULL OR c.model IN (:models))" +
            " AND ((:marginPercentageAfterSurCharge) IS NULL OR " +
            "   (:comparator = '<=' AND c.dealerPricingPremiumPercentage <= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>=' AND c.dealerPricingPremiumPercentage >= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '<' AND c.dealerPricingPremiumPercentage < :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>' AND c.dealerPricingPremiumPercentage > :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '=' AND c.dealerPricingPremiumPercentage = :marginPercentageAfterSurCharge))" +
            " AND ((:chineseBrand) IS NULL OR c.chineseBrand = (:chineseBrand))")
    List<CompetitorPricing> getTotal(@Param("regions") List<String> regions,
                                     @Param("plants") List<String> plants,
                                     @Param("metaSeries") List<String> metaSeries,
                                     @Param("classes") List<String> classes,
                                     @Param("models") List<String> models,
                                     @Param("chineseBrand") Boolean chineseBrand,
                                     @Param("comparator") String comparator,
                                     @Param("marginPercentageAfterSurCharge") Double marginPercentageAfterSurCharge);

    @Query("SELECT COUNT(c) from CompetitorPricing c WHERE " +
            "((:regions) IS Null OR c.country.region.regionName IN (:regions))" +
            " AND ((:plants) IS NULL OR c.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(c.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR c.clazz.clazzName IN (:classes))" +
            " AND ((:models) IS NULL OR c.model IN (:models))" +
            " AND ((:marginPercentageAfterSurCharge) IS NULL OR " +
            "   (:comparator = '<=' AND c.dealerPricingPremiumPercentage <= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>=' AND c.dealerPricingPremiumPercentage >= :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '<' AND c.dealerPricingPremiumPercentage < :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '>' AND c.dealerPricingPremiumPercentage > :marginPercentageAfterSurCharge) OR" +
            "   (:comparator = '=' AND c.dealerPricingPremiumPercentage = :marginPercentageAfterSurCharge))" +
            " AND ((:chineseBrand) IS NULL OR c.chineseBrand = (:chineseBrand))")
    int getCountAll(@Param("regions") List<String> regions,
                    @Param("plants") List<String> plants,
                    @Param("metaSeries") List<String> metaSeries,
                    @Param("classes") List<String> classes,
                    @Param("models") List<String> models,
                    @Param("chineseBrand") Boolean chineseBrand,
                    @Param("comparator") String comparator,
                    @Param("marginPercentageAfterSurCharge") Double marginPercentageAfterSurCharge);


    @Query("SELECT DISTINCT c.series FROM CompetitorPricing c WHERE c.series != '' AND c.series IS NOT NULL")
    List<String> getDistinctSeries();

    @Query("SELECT DISTINCT c.category FROM CompetitorPricing c")
    List<String> getDistinctCategory();

    @Query("SELECT new CompetitorPricing (AVG(c.competitorLeadTime), AVG(c.competitorPricing), AVG(c.marketShare), c.color) FROM CompetitorPricing c " +
            "WHERE ((:regions) IS NULL OR c.country.region.regionName IN (:regions)) " +
            "AND ((:countries) IS NULL OR c.country.countryName IN (:countries)) " +
            "AND ((:classes) IS NULL OR c.clazz.clazzName IN (:classes)) " +
            "AND ((:category) IS NULL OR c.category IN (:category)) " +
            "AND ((:series) IS NULL OR c.series IN (:series)) GROUP BY c.color ORDER BY AVG(c.marketShare)")
    List<CompetitorPricing> getDataForBubbleChart(@Param("regions") List<String> regions, @Param("countries") List<String> countries,
                                                  @Param("classes") List<String> classes, @Param("category") List<String> categories,
                                                  @Param("series") List<String> series);

    @Query("SELECT c FROM CompetitorPricing c WHERE c.country.countryName = ?1 AND c.clazz.clazzName = ?2 AND c.category = ?3 AND " +
            "c.series = ?4 AND c.competitorName = ?5 AND c.model = ?6")
    Optional<CompetitorPricing> getCompetitorPricing(String country, String clazz, String category,
                                                     String series, String competitorName, String model);

    @Query(value = "SELECT m.latest_modified_at FROM competitor_pricing m WHERE m.latest_modified_at is not null ORDER BY m.latest_modified_at DESC LIMIT 1", nativeQuery = true)
    Optional<LocalDateTime> getLatestUpdatedTime();

}
