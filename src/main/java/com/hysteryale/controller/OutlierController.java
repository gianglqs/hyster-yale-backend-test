/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.controller;

import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.service.OutlierService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.Map;

@RestController
public class OutlierController {

    @Resource
    OutlierService outlierService;

    @PostMapping("/table/getOutlierTable")
    public Map<String, Object> getDataForTable(@RequestBody FilterModel filters,
                                               @RequestParam(defaultValue = "0") int pageNo,
                                               @RequestParam(defaultValue = "100") int perPage) throws ParseException {
        filters.setPageNo(pageNo);
        filters.setPerPage(perPage);
        return outlierService.getDataForTable(filters);

    }

    @PostMapping("/chart/getOutliers")
    public Map<String, Object> getOutliersForChart(@RequestBody FilterModel filters) throws ParseException {
        return outlierService.getDataForChart(filters);
    }

}
