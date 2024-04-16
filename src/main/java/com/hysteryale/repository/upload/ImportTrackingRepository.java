/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.repository.upload;

import com.hysteryale.model.ImportTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ImportTrackingRepository extends JpaRepository<ImportTracking, Integer> {

    @Query("SELECT i FROM ImportTracking i WHERE " +
            "   i.belongToTime IS NULL OR"+
            "   (i.fileUpload.modelType.frequency.type= 'Monthly' AND " +
            "       EXTRACT(YEAR FROM i.belongToTime) = EXTRACT(YEAR FROM CAST(:date AS timestamp )) AND " +
            "       EXTRACT(MONTH FROM i.belongToTime) = EXTRACT(MONTH FROM CAST(:date AS timestamp))) " +
            "   OR " +
            "   (i.fileUpload.modelType.frequency.type= 'Annual' AND " +
            "       EXTRACT(YEAR FROM i.belongToTime) = EXTRACT(YEAR FROM CAST(:date AS timestamp)))"
    )
    List<ImportTracking> findByMonthAndYear(LocalDate date);
}
