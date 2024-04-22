/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.repository_h2;

import com.hysteryale.model_h2.MarginData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MarginDataRepository extends JpaRepository<MarginData, Integer> {
    @Query("SELECT m from MarginData m " +
            "WHERE ((:model_code) IS NULL OR m.id.modelCode = (:model_code)) " +
            "AND ((:series) IS NULL OR m.series = (:series)) " +
            "AND ((:order_number) IS NULL OR m.orderNumber = (:order_number)) " +
            "AND m.id.currency = :currency " +
            "AND ((:type) IS NULL OR m.id.type = (:type)) " +
            "AND m.fileUUID = :fileuuid " +
            "AND ((:region) IS NULL OR m.id.region = (:region))")
    List<MarginData> getIMMarginAnalystData(@Param("model_code") String modelCode, @Param("order_number") String orderNumber,
                                            @Param("currency") String currency, @Param("type") Integer type,
                                            @Param("fileuuid") String fileUUID, @Param("series") String series,
                                            @Param("region") String region);

    @Query("SELECT DISTINCT m.id.modelCode FROM MarginData m WHERE m.fileUUID = ?1 and m.series = ?2")
    List<String> getModelCodesBySeries(String fileUUID, String series);

    @Query("SELECT CASE WHEN (COUNT(m) > 0) THEN true ELSE false END " +
            "FROM MarginData m " +
            "WHERE m.fileUUID = ?1 " +
            "AND m.id.currency = ?2 " +
            "AND m.id.region = ?3")
    boolean isFileCalculated(String fileUIID, String currency, String region);

    @Query("SELECT m from MarginData m " +
            "WHERE m.id.modelCode = ?1 " +
            "AND m.id.partNumber = ?2 " +
            "AND m.id.type = ?3 " +
            "AND m.fileUUID = ?4")
    Optional<MarginData> getIMMarginAnalystDataForTesting(String modelCode, String partNumber, int type, String fileUUID);
}
