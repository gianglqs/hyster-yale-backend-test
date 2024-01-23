package com.hysteryale.repository.marginAnalyst;

import com.hysteryale.model.marginAnalyst.MarginAnalystData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface MarginAnalystDataRepository extends JpaRepository<MarginAnalystData, Integer> {
    @Query("SELECT m FROM MarginAnalystData m WHERE m.modelCode = ?1 AND m.currency.currency = ?2 AND m.monthYear = ?3")
    List<MarginAnalystData> getMarginDataForAnalysis(String modelCode, String currency, LocalDate monthYear);

    @Query("SELECT m FROM MarginAnalystData m WHERE m.modelCode = ?1 AND m.currency.currency = ?2 AND m.monthYear = ?3 AND m.dealer = ?4")
    List<MarginAnalystData> getMarginDataForAnalysisByDealer(String modelCode, String currency, LocalDate monthYear, String dealer);

    @Query("SELECT DISTINCT m.modelCode FROM MarginAnalystData m WHERE m.monthYear = ?1 AND m.currency.currency = ?2")
    List<String> getModelCodesByMonthYearAndCurrency(LocalDate monthYear, String currency);

    @Query("SELECT DISTINCT m.dealer FROM MarginAnalystData m")
    List<String> getDealersFromMarginAnalystData();
}
