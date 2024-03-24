package com.hysteryale.service.impl;

import com.hysteryale.model.enums.ImportFailureType;
import com.hysteryale.model.importFailure.ImportFailure;
import com.hysteryale.service.ImportFailureService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ImportFailureServiceImp implements ImportFailureService {


    @Override
    public void addIntoListImportFailure(List<ImportFailure> importFailures, String primaryKey, String reason, ImportFailureType type) {
        ImportFailure importFailure = new ImportFailure(primaryKey, reason, type.getValue());
        importFailures.add(importFailure);
    }

    @Override
    public void setFileNameForListImportFailure(List<ImportFailure> importFailures, String fileName) {
        for (ImportFailure importFailure : importFailures) {
            importFailure.setFileName(fileName);
        }
    }
}


