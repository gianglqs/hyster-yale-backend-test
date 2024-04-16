/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.service;

import com.hysteryale.exception.BlankSheetException;
import com.hysteryale.exception.MissingSheetException;
import com.hysteryale.model.importFailure.ImportFailure;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ResidualValueService {
    List<ImportFailure> importResidualValue(String filePath, String fileUUID, int year) throws IOException, MissingSheetException, BlankSheetException;

    Map<String, Object> getDataByFilter(String modelCode);
}
