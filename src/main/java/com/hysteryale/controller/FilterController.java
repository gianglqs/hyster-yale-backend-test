/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.controller;

import com.hysteryale.service.CountryService;
import com.hysteryale.service.FilterService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("filters")
public class FilterController {
    @Resource
    FilterService filterService;
    @Resource
    CountryService countryService;

    @GetMapping("/competitorPricing")
    public Map<String, Object> getCompetitorPricingFilters() {
        return filterService.getCompetitorPricingFilter();
    }

    @GetMapping("/competitorPricing/get-country-name")
    public Map<String, Object> getCountryByRegion(@RequestParam String region) {
        List<String> countryList = countryService.getListCountryNameByRegion(region);
        countryList.removeIf(String::isEmpty);
        return Map.of("country", countryList);
    }

    @GetMapping("/shipment")
    public Map<String, Object> getShipmentFilters() {
        return filterService.getOrderFilter();
    }

    @GetMapping("/booking")
    public Map<String, Object> getBookingFilters() {
        return filterService.getOrderFilter();
    }

    @GetMapping("/outlier")
    public Map<String, Object> getOutlierFilters() {
        return filterService.getOutlierFilter();
    }

    @GetMapping("/trends")
    public Map<String, Object> getTrendsFilters(){ return filterService.getTrendsFilter();}

    @GetMapping("/product")
    public Map<String, Object> getProductFilters(){ return filterService.getProductFilter();}

    @GetMapping("/productDetail")
    public Map<String, Object> getOrderNoForProductDetail(@RequestParam String modelCode, @RequestParam String metaSeries){
        return filterService.getProductDetailFilter(modelCode, metaSeries);
    }

    @GetMapping("/currency")
    public Map<String, Object> getCurrencyFilter() {
        return Map.of(
                "currencyFilter", filterService.getCurrencyFilter()
        );
    }

    @GetMapping("/segments")
    public Map<String, Object> getSegmentFilters() {
        return Map.of(
                "segments", filterService.getAllSegments()
        );
    }

    @GetMapping("/bookingMarginTrialTest")
    public Map<String, Object> getBookingMarginTrialTest() {
        return filterService.getOrderFilter();
    }

    @GetMapping("/modelCodeOfResidualValue")
    public List<Map<String, String>> getModelCodeForResidualValueFilter(@RequestParam String modelType, @RequestParam String brand){
        return filterService.getResidualValueFilter(modelType, brand);
    }

    @GetMapping("/getAllModelTypeAndBrand")
    public Map<String, Object> getAllModelTypeAndBrandForResidualValueFilter(){
        return filterService.getModelTypeAndBrand();
    }

    @GetMapping("/gdp")
    public List<Integer> getGDPFilters() {
            return filterService.getGDPFilter();
    }

}

