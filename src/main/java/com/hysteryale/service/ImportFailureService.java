package com.hysteryale.service;

import com.hysteryale.model.enums.ImportFailureType;
import com.hysteryale.model.importFailure.ImportFailure;

import java.util.List;

public interface ImportFailureService {

    void addIntoListImportFailure(List<ImportFailure> importFailures, String primaryKey, String reason, ImportFailureType type);

    void setFileNameForListImportFailure(List<ImportFailure> importFailures, String fileName);
}
