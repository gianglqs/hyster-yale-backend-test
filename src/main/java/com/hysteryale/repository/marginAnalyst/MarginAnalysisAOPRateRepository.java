package com.hysteryale.repository.marginAnalyst;

import com.hysteryale.model.marginAnalyst.MarginAnalysisAOPRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

public interface MarginAnalysisAOPRateRepository extends JpaRepository<MarginAnalysisAOPRate, Integer> {
    @Query("SELECT m FROM MarginAnalysisAOPRate m WHERE m.plant = ?1 AND m.currency.currency = ?2 AND m.monthYear = ?3 AND m.durationUnit = ?4")
    Optional<MarginAnalysisAOPRate> getMarginAnalysisAOPRate(String plant, String strCurrency, LocalDate monthYear, String durationUnit);
}
