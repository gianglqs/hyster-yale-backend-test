/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.controller;

import com.hysteryale.model.filters.AdjustmentFilterModel;
import com.hysteryale.model.filters.CalculatorModel;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.service.AdjustmentService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
public class AdjustmentController {

    @Resource
    AdjustmentService adjustmentService;

    @PostMapping(path = "/getAdjustmentData", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getDataAdjustment(@RequestBody AdjustmentFilterModel adjustmentFilter,
                                                 @RequestParam(defaultValue = "1") int pageNo,
                                                 @RequestParam(defaultValue = "100") int perPage) throws java.text.ParseException {

        //get Calculator value
        CalculatorModel calculatorValue = adjustmentFilter.getDataCalculate();
        FilterModel filters = adjustmentFilter.getDataFilter();

        filters.setPageNo(pageNo);
        filters.setPerPage(perPage);
        return adjustmentService.getAdjustmentByFilter(filters, calculatorValue);
    }

}
