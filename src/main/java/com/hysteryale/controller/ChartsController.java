package com.hysteryale.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("charts")
public class ChartsController {
    @PostMapping("/lineChartRegion")
    public List<Object> getDataForLineChartRegion(){
        return null;
    }

    @PostMapping("/lineChartPlant")
    public List<Object> getDataForLineChartPLant(){
        return null;
    }
}
