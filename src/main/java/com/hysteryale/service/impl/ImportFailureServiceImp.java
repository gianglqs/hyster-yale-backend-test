package com.hysteryale.service.impl;

import com.hysteryale.model.enums.ImportFailureType;
import com.hysteryale.model.filters.ImportFailureFilter;
import com.hysteryale.model.importFailure.ImportFailure;
import com.hysteryale.repository.importFailure.ImportFailureRepository;
import com.hysteryale.service.ImportFailureService;
import com.hysteryale.utils.ConvertDataFilterUtil;
import com.hysteryale.utils.LocaleUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ImportFailureServiceImp implements ImportFailureService {

    @Resource
    private HashMap<String, HashMap<String, HashMap<String, String>>> messageJSON;

    @Resource
    private ImportFailureRepository importFailureRepository;

    @Override
    public void addIntoListImportFailure(List<ImportFailure> importFailures, String primaryKey, String reasonKey, String reasonValue, ImportFailureType type) {
        ImportFailure importFailure = new ImportFailure(primaryKey, reasonKey, reasonValue, type.getValue());
        importFailures.add(importFailure);
    }

    @Override
    public void setFileUUIDForListImportFailure(List<ImportFailure> importFailures, String fileUUID) {
        for (ImportFailure importFailure : importFailures) {
            importFailure.setFileUUID(fileUUID);
        }
    }

    @Override
    public List<ImportFailure> mapReasonKeyWithReasonValue(List<ImportFailure> importFailures, String locale) {
        for (ImportFailure importFailure : importFailures) {
            String[] reasonValues = importFailure.getReasonValue().split("###");
            String baseMessage = LocaleUtils.getMessage(messageJSON, locale, importFailure.getType(), importFailure.getReasonKey());
            importFailure.setReason(String.format(baseMessage, reasonValues));
        }
        return importFailures;
    }

    @Override
    public Map<String, Integer> countErrorAndWarning(List<ImportFailure> importFailures) {
        Map<String, Integer> result = new HashMap<>();
        int error = 0;
        int warning = 0;
        for (ImportFailure importFailure : importFailures) {
            if (importFailure.getType().equals(ImportFailureType.ERROR.getValue())) {
                error++;
                continue;
            }
            if (importFailure.getType().equals(ImportFailureType.WARNING.getValue()))
                warning++;
        }
        result.put(ImportFailureType.ERROR.getValue(), error);
        result.put(ImportFailureType.WARNING.getValue(), warning);

        return result;
    }

    @Override
    public Map<String, Object> getDataForTable(ImportFailureFilter filter, int pageNo, int perPage, String locale) {
        Map<String, Object> result = new HashMap<>();

        Pageable pageable = PageRequest.of(pageNo == 0 ? pageNo : pageNo - 1, perPage == 0 ? 100 : perPage);
        List<ImportFailure> getImportFailureList = importFailureRepository.getByFilter(ConvertDataFilterUtil.convertFilter(filter.getFileUUID()), ConvertDataFilterUtil.convertFilter(filter.getSearch()), pageable);
        mapReasonKeyWithReasonValue(getImportFailureList, locale);
        result.put("listImportFailure", getImportFailureList);

        long countAllWithFilter = importFailureRepository.countAllWithFilter(ConvertDataFilterUtil.convertFilter(filter.getFileUUID()), ConvertDataFilterUtil.convertFilter(filter.getSearch()));
        result.put("totalItems", countAllWithFilter);

        int total = importFailureRepository.countAll(filter.getFileUUID());
        int totalError = importFailureRepository.countError(filter.getFileUUID());
        int totalWarning = total - totalError;

        result.put("overview", mapMessageCountAll(locale, totalError, totalWarning));

        return result;
    }

    @Override
    public String mapMessageCountAll(String locale, int totalError, int totalWarning) {
        String baseMessage = LocaleUtils.getMessage(messageJSON, locale, "success", "totalError-totalWarning");
        return String.format(baseMessage, totalError, totalWarning);
    }
}


