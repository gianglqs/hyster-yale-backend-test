package com.hysteryale.controller;

import com.hysteryale.service.CountryService;
import com.hysteryale.service.FilterService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
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
        return Map.of("country", countryService.getListCountryNameByRegion(region));
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

}

