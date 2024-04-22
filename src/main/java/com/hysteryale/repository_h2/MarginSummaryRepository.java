/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.repository_h2;

import com.hysteryale.model_h2.MarginSummary;
import com.hysteryale.model_h2.MarginSummaryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MarginSummaryRepository extends JpaRepository<MarginSummary, Integer> {
    @Query("SELECT DISTINCT new com.hysteryale.model_h2.MarginSummaryId(m.id.quoteNumber, m.id.type, m.id.modelCode, m.id.series, m.id.currency, m.id.userId, m.id.region) " +
            "FROM MarginSummary m " +
            "WHERE m.id.userId = ?1")
    List<MarginSummaryId> listHistoryMarginSummary(int userId);

    @Query("SELECT m FROM MarginSummary m " +
            "WHERE m.id.quoteNumber = ?1 " +
            "AND m.id.type = ?2 " +
            "AND m.id.modelCode = ?3 " +
            "AND m.id.series = ?4 " +
            "AND m.id.currency = ?5 " +
            "AND m.id.userId = ?6 " +
            "AND m.id.region = ?7 " +
            "AND m.id.durationUnit = ?8")
    Optional<MarginSummary> viewHistoryMarginSummary(String quoteNumber, int type, String modelCode, String series,
                                                     String currency, int userId, String region, String durationUnit);
}
