package com.hysteryale.repository.marginAnalyst;

import com.hysteryale.model.marginAnalyst.MarginAnalystSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

public interface MarginAnalystSummaryRepository extends JpaRepository<MarginAnalystSummary, Integer> {
    @Query("SELECT m FROM MarginAnalystSummary m WHERE m.modelCode = ?1 AND m.currency.currency= ?2 AND m.monthYear = ?3")
    Optional<MarginAnalystSummary> getMarginAnalystSummaryMonthly(String modelCode, String currency, LocalDate monthYear);

    @Query("SELECT m FROM MarginAnalystSummary m WHERE m.modelCode = ?1 AND m.currency.currency= ?2 AND m.monthYear = ?3")
    Optional<MarginAnalystSummary> getMarginAnalystSummaryAnnually(String modelCode, String currency, LocalDate monthYear);
}
