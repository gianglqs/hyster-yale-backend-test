/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.utils;

import com.hysteryale.model.importFailure.ImportFailure;
import com.hysteryale.service.BasedService;
import com.hysteryale.service.ImportFailureService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class LocaleUtils extends BasedService {

    @Resource
    private  ImportFailureService importFailureService;

    @Resource
    private  HashMap<String, HashMap<String, HashMap<String, String>>> messagesMap;

    public static String getMessage(HashMap<String, HashMap<String, HashMap<String, String>>> messageMap, String locale, String type, String key) {
        return messageMap.get(locale).get(type).get(key);
    }

    public String getMessageImportComplete(List<ImportFailure> importFailures, String modelType, String locale) {
        importFailureService.mapReasonKeyWithReasonValue(importFailures, locale);
        Map<String, Integer> countErrorAndWarning = importFailureService.countErrorAndWarning(importFailures);
        String baseMessage = LocaleUtils.getMessage(messagesMap, locale, "success", "import-complete");
        return String.format(baseMessage, modelType, countErrorAndWarning.get("error"), countErrorAndWarning.get("warning"));
    }


    public void logStatusImportComplete(List<ImportFailure> importFailures, String modelType) {
        importFailureService.mapReasonKeyWithReasonValue(importFailures, "en");
        Map<String, Integer> countErrorAndWarning = importFailureService.countErrorAndWarning(importFailures);
        StringBuilder logErrorString = new StringBuilder(String.format("Import %s complete (%d error, %d warning)\n", modelType, countErrorAndWarning.get("error"), countErrorAndWarning.get("warning")));

        if (!importFailures.isEmpty()) {
            for (ImportFailure importFailure : importFailures) {
                logErrorString.append(importFailure.toString()).append("\n");
            }
        }

        logWarning(logErrorString.toString());
    }
}
