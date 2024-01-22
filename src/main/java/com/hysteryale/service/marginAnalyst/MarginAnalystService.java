package com.hysteryale.service.marginAnalyst;

import com.hysteryale.model.marginAnalyst.MarginAnalystData;
import com.hysteryale.model.marginAnalyst.MarginAnalystSummary;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface MarginAnalystService {

    /**
     * To query the margin analyst data of a model code
     *
     * @param modelCode
     * @param currency
     * @return
     */
    Map<String, List<MarginAnalystData>> getMarginAnalystData(String modelCode, String currency, LocalDate monthYear);

    /**
     * To query the margin analyst summarized of a model code
     * @param modelCode
     * @param currency
     * @return
     */
    Map<String, MarginAnalystSummary> getMarginAnalystSummary(String modelCode, String currency, LocalDate monthYear);

    void importMarginAnalystData() throws IOException;
    Map<String, List<Map<String, String>>>  getDealersFromMarginAnalystData();
    Map<String, List<MarginAnalystData>> getMarginDataForAnalysisByDealer(String modelCode, String currency, LocalDate monthYear, String dealer);

}
