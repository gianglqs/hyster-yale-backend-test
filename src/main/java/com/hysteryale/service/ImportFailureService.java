package com.hysteryale.service;

import com.hysteryale.model.enums.ImportFailureType;
import com.hysteryale.model.filters.ImportFailureFilter;
import com.hysteryale.model.importFailure.ImportFailure;

import java.util.List;
import java.util.Map;

public interface ImportFailureService {

    void addIntoListImportFailure(List<ImportFailure> importFailures, String primaryKey, String reasonKey, String reasonValue, ImportFailureType type);

    void setFileUUIDForListImportFailure(List<ImportFailure> importFailures, String fileUUID);

    List<ImportFailure> mapReasonKeyWithReasonValue(List<ImportFailure> importFailures, String locale);

    Map<String, Integer> countErrorAndWarning(List<ImportFailure> importFailures);

    Map<String, Object> getDataForTable(ImportFailureFilter filter, int pageNo, int perPage, String locale);

    String mapMessageCountAll(String locale, int totalError, int totalWarning);
}
