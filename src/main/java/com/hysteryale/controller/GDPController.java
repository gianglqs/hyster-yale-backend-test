/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.controller;

import com.hysteryale.model.payLoad.BubbleChartGDPCountryPageLoad;
import com.hysteryale.service.GDPService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("gdp")
public class GDPController {

    @Resource
    private GDPService GDPService;

    @GetMapping("/collectData")
    public void collectData() {
        GDPService.collectData();
    }

    @GetMapping("/getDataForTable")
    public Map<String, Object> getDataForTable(@RequestParam int year, @RequestParam int pageNo, @RequestParam int perPage) {
        return GDPService.getDataForTable(year, pageNo, perPage);
    }

    @GetMapping("/getDataForBubbleChart")
    public List<BubbleChartGDPCountryPageLoad> getDataForBubbleChart(@RequestParam int year) {
        return GDPService.getDataForBubbleChart(year);
    }

    @GetMapping("/getDataForTopCountry")
    public Map<String, Object> getDataForTopCountry(@RequestParam int year) {
        return GDPService.getDataForTopCountry(year);
    }
}
