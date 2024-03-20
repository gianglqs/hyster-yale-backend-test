package com.hysteryale.repository_h2;

import com.hysteryale.model_h2.IMMarginAnalystData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IMMarginAnalystDataRepository extends JpaRepository<IMMarginAnalystData, Integer> {
    @Query("SELECT m from IMMarginAnalystData m " +
            "WHERE ((:model_code) IS NULL OR m.modelCode = (:model_code)) " +
            "AND ((:series) IS NULL OR m.series = (:series)) " +
            "AND ((:order_number) IS NULL OR m.orderNumber = (:order_number)) " +
            "AND m.currency = :currency " +
            "AND ((:type) IS NULL OR m.type = (:type)) " +
            "AND m.fileUUID = :fileuuid " +
            "AND ((:region) IS NULL OR m.region = (:region))")
    List<IMMarginAnalystData> getIMMarginAnalystData(@Param("model_code") String modelCode, @Param("order_number") String orderNumber,
                                                     @Param("currency") String currency, @Param("type") Integer type,
                                                     @Param("fileuuid") String fileUUID, @Param("series") String series,
                                                     @Param("region") String region);

    @Query("SELECT DISTINCT m.modelCode FROM IMMarginAnalystData m WHERE m.fileUUID = ?1 and m.series = ?2")
    List<String> getModelCodesBySeries(String fileUUID, String series);

    @Query("SELECT CASE WHEN (COUNT(m) > 0) THEN true ELSE false END " +
            "FROM IMMarginAnalystData m " +
            "WHERE m.fileUUID = ?1 " +
            "AND currency = ?2 " +
            "AND m.region = ?3")
    boolean isFileCalculated(String fileUIID, String currency, String region);

    @Query("SELECT m from IMMarginAnalystData m " +
            "WHERE m.modelCode = ?1 " +
            "AND m.optionCode = ?2 " +
            "AND m.type = ?3 " +
            "AND m.fileUUID = ?4")
    Optional<IMMarginAnalystData> getIMMarginAnalystDataForTesting(String modelCode, String partNumber, int type, String fileUUID);
}
