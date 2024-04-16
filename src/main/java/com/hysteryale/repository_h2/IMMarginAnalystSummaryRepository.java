/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.repository_h2;

import com.hysteryale.model_h2.IMMarginAnalystSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface IMMarginAnalystSummaryRepository extends JpaRepository<IMMarginAnalystSummary, Integer> {
    @Query("SELECT m FROM IMMarginAnalystSummary m WHERE m.modelCode = ?1 and m.currency = ?2 and m.fileUUID = ?3 and m.type = ?4")
    List<IMMarginAnalystSummary> getIMMarginAnalystSummary(String modelCode, String currency, String fileUUID, int type);

    @Query("SELECT m FROM IMMarginAnalystSummary m WHERE m.modelCode = ?1 and m.currency = ?2 AND m.orderNumber = ?3 AND m.durationUnit = 'monthly' AND m.type = ?4")
    Optional<IMMarginAnalystSummary> getIMMarginAnalystSummaryMonthly(String modelCode, String currency, String orderNumber, int type);

    @Query("SELECT m FROM IMMarginAnalystSummary m WHERE m.modelCode = ?1 and m.currency = ?2 AND m.orderNumber = ?3 AND m.durationUnit = 'annually' AND m.type = ?4")
    Optional<IMMarginAnalystSummary> getIMMarginAnalystSummaryAnnually(String modelCode, String currency, String orderNumber, int type);
}
