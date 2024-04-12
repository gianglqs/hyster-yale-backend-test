/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.controller;

import com.hysteryale.model.TrendData;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.service.TrendsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("trends")
public class TrendsController {
    @Resource
    TrendsService trendsService;

    @PostMapping("/getMarginVsCostData")
    public Map<String, Object> getMarginVsCostData(@RequestBody FilterModel filters) throws ParseException {
        Map<String, List<TrendData>>marginVsCostData = trendsService.getMarginVsCostData(filters);
        return Map.of("marginVsCostData", marginVsCostData);
    }

    @PostMapping("/getMarginVsDNData")
    public Map<String, Object> getMarginVsDNData(@RequestBody FilterModel filters) throws ParseException {
        Map<String, List<TrendData>>marginVsDNData = trendsService.getMarginVsDNData(filters);
        return Map.of("marginVsDNData", marginVsDNData);
    }

}
