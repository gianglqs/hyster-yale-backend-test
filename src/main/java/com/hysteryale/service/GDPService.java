/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.service;

import com.hysteryale.model.payLoad.BubbleChartGDPCountryPageLoad;

import java.util.List;
import java.util.Map;

public interface GDPService {

    void collectData();

    Map<String, Object> getDataForTable(int year, int pageNo, int perPage);

    List<BubbleChartGDPCountryPageLoad> getDataForBubbleChart(int year);

    Map<String, Object> getDataForTopCountry(int year);
}
