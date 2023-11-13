package com.hysteryale.repository;

import com.hysteryale.model.competitor.CompetitorPricing;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CompetitorPricingRepository extends JpaRepository<CompetitorPricing, Integer> {

    @Query("SELECT c.country, c.clazz, c.category, c.series FROM CompetitorPricing c GROUP BY c.country, c.clazz, c.category, c.series")
    List<String[]> getCompetitorGroup();

    @Query("SELECT c FROM CompetitorPricing c WHERE c.country = ?1 AND c.clazz = ?2 AND c.category = ?3 AND c.series = ?4")
    List<CompetitorPricing> getListOfCompetitorInGroup(String country, String clazz, String category, String series);

    @Query("SELECT new com.hysteryale.model.competitor.CompetitorPricing(c.region, SUM(c.actual), SUM(c.AOPF), SUM(c.LRFF)) FROM CompetitorPricing c WHERE ( (:regions) IS Null OR c.region IN (:regions) ) AND ((:plants) IS NULL OR c.plant IN (:plants)) AND ((:metaSeries) IS NULL OR SUBSTRING(c.series, 1,3) IN (:metaSeries))  AND ((:classes) IS NULL OR c.clazz IN (:classes)) AND ((:models) IS NULL OR c.model IN (:models)) AND ((:chineseBrand) IS NULL OR c.chineseBrand = (:chineseBrand)) GROUP BY c.region")
    public List<CompetitorPricing> findCompetitorByFilterForLineChartRegion(@Param("regions") List<String> regions, List<String> plants, List<String> metaSeries, List<String> classes, List<String> models, Boolean chineseBrand);

    @Query("SELECT new com.hysteryale.model.competitor.CompetitorPricing( SUM(c.actual), SUM(c.AOPF), SUM(c.LRFF),c.plant) FROM CompetitorPricing c WHERE c.plant IS NOT NULL AND ((:regions) IS Null OR c.region IN (:regions) ) AND ((:plants) IS NULL OR c.plant IN (:plants)) AND ((:metaSeries) IS NULL OR SUBSTRING(c.series, 1,3) IN (:metaSeries))  AND ((:classes) IS NULL OR c.clazz IN (:classes)) AND ((:models) IS NULL OR c.model IN (:models)) AND ((:chineseBrand) IS NULL OR c.chineseBrand = (:chineseBrand)) GROUP BY c.plant")
    public List<CompetitorPricing>  findCompetitorByFilterForLineChartPlant(@Param("regions") List<String> regions, List<String> plants, List<String> metaSeries, List<String> classes, List<String> models, Boolean chineseBrand);

    @Query("SELECT c FROM CompetitorPricing c WHERE ( (:regions) IS Null OR c.region IN :regions ) AND ((:plants) IS NULL OR c.plant IN (:plants)) AND ((:metaSeries) IS NULL OR SUBSTRING(c.series, 1,3) IN (:metaSeries))  AND ((:classes) IS NULL OR c.clazz IN (:classes)) AND ((:models) IS NULL OR c.model IN (:models)) AND ((:chineseBrand) IS NULL OR c.chineseBrand = (:chineseBrand))")
    public List<CompetitorPricing> findCompetitorByFilterForTable(@Param("regions") List<String> regions, List<String> plants, List<String> metaSeries, List<String> classes, List<String> models, Boolean chineseBrand, Pageable pageable);

    @Query("SELECT COUNT(c) from CompetitorPricing c")
    public int getCountAll();

    @Query("SELECT DISTINCT c.series FROM CompetitorPricing c")
    List<String> getDistinctSeries();

    @Query("SELECT DISTINCT c.category FROM CompetitorPricing c")
    List<String> getDistinctCategory();

    @Query("SELECT DISTINCT c.country FROM CompetitorPricing c")
    List<String> getDistinctCountry();

}
