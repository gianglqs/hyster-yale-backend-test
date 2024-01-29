package com.hysteryale.repository_h2;

import com.hysteryale.model_h2.IMMarginAnalystData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IMMarginAnalystDataRepository extends JpaRepository<IMMarginAnalystData, Integer> {
    @Query("SELECT m from IMMarginAnalystData m " +
            "WHERE ((:model_code) IS NULL OR m.modelCode = (:model_code)) " +
            "AND ((:series) IS NULL OR m.series = (:series)) " +
            "AND ((:order_number) IS NULL OR m.orderNumber = (:order_number)) " +
            "AND m.currency = :currency " +
            "AND ((:type) IS NULL OR m.type = (:type)) " +
            "AND m.fileUUID = :fileuuid")
    List<IMMarginAnalystData> getIMMarginAnalystData(@Param("model_code") String modelCode, @Param("order_number") String orderNumber,
                                                            @Param("currency") String currency, @Param("type") Integer type,
                                                            @Param("fileuuid") String fileUUID, @Param("series") String series);

    @Query("SELECT DISTINCT m.modelCode FROM IMMarginAnalystData m WHERE m.fileUUID = ?1 and m.series = ?2")
    List<String> getModelCodesBySeries(String fileUUID, String series);

    @Query("SELECT CASE WHEN (COUNT(m) > 0) THEN true ELSE false END " +
            "FROM IMMarginAnalystData m WHERE m.fileUUID = ?1 AND currency = ?2")
    boolean isFileCalculated(String fileUIID, String currency);
}
