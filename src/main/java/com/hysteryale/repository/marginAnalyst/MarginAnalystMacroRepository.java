/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.repository.marginAnalyst;

import com.hysteryale.model.marginAnalyst.MarginAnalystMacro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MarginAnalystMacroRepository extends JpaRepository<MarginAnalystMacro, Integer> {

    @Query(value = "SELECT * FROM margin_analyst_macro m " +
            "WHERE m.model_code = :modelCode " +
            "AND m.part_number = :partNumber " +
            "AND m.currency_currency = :currency " +
            "AND m.month_year = :monthYear LIMIT 1", nativeQuery = true)
    Optional<MarginAnalystMacro> getMarginAnalystMacroByMonthYear(@Param("modelCode") String modelCode, @Param("partNumber") String partNumber,
                                                                  @Param("currency") String strCurrency, @Param("monthYear") LocalDate monthYear);

    @Query(value = "SELECT * FROM margin_analyst_macro m " +
            "WHERE m.model_code = :model_code " +
            "AND m.part_number = :part_number " +
            "AND m.currency_currency = :currency " +
            "AND m.month_year = :month_year " +
            "AND m.description = :description " +
            "AND m.clazz = :class " +
            "AND m.region = :region LIMIT 1", nativeQuery = true)
    Optional<MarginAnalystMacro> getMacroForTesting(@Param("model_code") String modelCode, @Param("part_number") String partNumber,
                                                    @Param("currency") String strCurrency, @Param("month_year") LocalDate monthYear,
                                                    @Param("description") String description, @Param("class") String clazz, @Param("region") String region);

    @Query(value = "SELECT m.costrmb FROM margin_analyst_macro m " +
            "WHERE m.model_code LIKE CONCAT ('%', :modelCode, '%') " +
            "AND m.part_number = :partNumber " +
            "AND m.currency_currency = :currency " +
            "AND m.plant in :plants " +
            "AND m.month_year = (SELECT MAX(month_year) FROM margin_analyst_macro) " +
            "ORDER BY similarity(m.model_code, :modelCode) desc limit 1", nativeQuery = true)
    Double getManufacturingCost(@Param("modelCode") String modelCode, @Param("partNumber") String partNumber, @Param("currency") String strCurrency,
                                @Param("plants") List<String> plants);

    @Query(value = "SELECT m.costrmb FROM margin_analyst_macro m " +
            "WHERE m.model_code LIKE CONCAT ('%', :modelCode, '%') " +
            "AND m.part_number = :partNumber " +
            "AND m.currency_currency = :currency " +
            "AND m.plant in :plants " +
            "AND m.month_year = (SELECT MAX(month_year) FROM margin_analyst_macro) " +
            "AND m.region = :region " +
            "ORDER BY similarity(m.model_code, :modelCode) desc limit 1", nativeQuery = true)
    Double getSNManufacturingCost(@Param("modelCode") String modelCode, @Param("partNumber") String partNumber, @Param("currency") String strCurrency,
                                @Param("plants") List<String> plants, @Param("region") String region);
    @Query("SELECT m FROM MarginAnalystMacro m WHERE m.modelCode LIKE CONCAT ('%', ?1, '%') AND m.partNumber IN (?2) AND m.currency.currency = ?3 AND m.plant = ?4 AND m.monthYear = ?5")
    List<MarginAnalystMacro> getMarginAnalystMacroByPlantAndListPartNumber(String modelCode, List<String> partNumber, String currency, String plant, LocalDate monthYear);

    @Query("SELECT m FROM MarginAnalystMacro m WHERE m.modelCode LIKE CONCAT ('%', ?1, '%') AND m.partNumber IN (?2) AND m.currency.currency = ?3  AND m.plant != 'SN' AND m.monthYear = ?4")
    List<MarginAnalystMacro> getMarginAnalystMacroByHYMPlantAndListPartNumber(String modelCode, List<String> partNumber, String currency, LocalDate monthYear);

    @Query(value = "SELECT m.clazz FROM margin_analyst_macro m WHERE m.series_code = :series LIMIT 1", nativeQuery = true)
    String getClassBySeries(@Param("series") String series);

    @Query("SELECT m FROM MarginAnalystMacro m WHERE m.plant = ?1 AND m.currency.currency = ?2 AND m.monthYear = ?3 AND m.region.region = ?4")
    List<MarginAnalystMacro> loadListMacroData(String plant, String currency, LocalDate monthYear, String region);

    @Query("SELECT m FROM MarginAnalystMacro m WHERE m.plant != 'SN' AND m.currency.currency = ?1 AND monthYear = ?2")
    List<MarginAnalystMacro> loadListHYMMacroData(String currency, LocalDate monthYear);

    @Query(value = "SELECT m.plant FROM margin_analyst_macro m WHERE m.series_code = :series LIMIT 1", nativeQuery = true)
    String getPlantBySeries(@Param("series") String series);

}
