/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.service;

import com.hysteryale.exception.CanNotExtractMonthAnhYearException;
import com.hysteryale.exception.CannotExtractDateException;
import com.hysteryale.exception.CannotExtractYearException;
import com.hysteryale.model.enums.FrequencyImport;

import java.time.LocalDate;
import java.util.Map;

public interface ImportTrackingService {

    Map<String, Object> getDataByFilter(LocalDate time);

    void updateImport(String fileUUID, String originalFileName, FrequencyImport frequency) throws CannotExtractDateException, CanNotExtractMonthAnhYearException, CannotExtractYearException;
}
