package com.hysteryale.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Map;

@SpringBootTest
public class FilterServiceTest {
    @Resource
    FilterService filterService;

    @Test
    public void testGetCompetitorPricingFilter() {
        Map<String, Object> filters = filterService.getCompetitorPricingFilter();

        Assertions.assertNotNull(filters.get("classes"));
        Assertions.assertNotNull(filters.get("plants"));
        Assertions.assertNotNull(filters.get("metaSeries"));
        Assertions.assertNotNull(filters.get("chineseBrands"));
        Assertions.assertNotNull(filters.get("marginPercentageGrouping"));
        Assertions.assertNotNull(filters.get("regions"));
        Assertions.assertNotNull(filters.get("dealers"));
        Assertions.assertNotNull(filters.get("series"));
        Assertions.assertNotNull(filters.get("categories"));
        Assertions.assertNotNull(filters.get("countries"));
    }

    @Test
    public void testGetOrderFilter() {
        Map<String, Object> filters = filterService.getOrderFilter();

        Assertions.assertNotNull(filters.get("regions"));
        Assertions.assertNotNull(filters.get("classes"));
        Assertions.assertNotNull(filters.get("plants"));
        Assertions.assertNotNull(filters.get("metaSeries"));
        Assertions.assertNotNull(filters.get("models"));
        Assertions.assertNotNull(filters.get("marginPercentageGroup"));
        Assertions.assertNotNull(filters.get("AOPMarginPercentageGroup"));
        Assertions.assertNotNull(filters.get("dealers"));
        Assertions.assertNotNull(filters.get("segments"));
    }

    @Test
    public void testGetOutlierFilter() {
        Map<String, Object> filters = filterService.getOutlierFilter();

        Assertions.assertNotNull(filters.get("regions"));
        Assertions.assertNotNull(filters.get("classes"));
        Assertions.assertNotNull(filters.get("plants"));
        Assertions.assertNotNull(filters.get("metaSeries"));
        Assertions.assertNotNull(filters.get("models"));
        Assertions.assertNotNull(filters.get("marginPercentageGroup"));
        Assertions.assertNotNull(filters.get("dealers"));
        Assertions.assertNotNull(filters.get("series"));
    }

    @Test
    public void testGetTrendFilter() {
        Map<String, Object> filters = filterService.getTrendsFilter();

        Assertions.assertNotNull(filters.get("regions"));
        Assertions.assertNotNull(filters.get("plants"));
        Assertions.assertNotNull(filters.get("metaSeries"));
        Assertions.assertNotNull(filters.get("classes"));
        Assertions.assertNotNull(filters.get("models"));
        Assertions.assertNotNull(filters.get("segments"));
        Assertions.assertNotNull(filters.get("years"));
        Assertions.assertNotNull(filters.get("dealers"));
    }

    @Test
    public void testGetProductFilter() {
        Map<String, Object> filters = filterService.getProductFilter();

        Assertions.assertNotNull(filters.get("plants"));
        Assertions.assertNotNull(filters.get("metaSeries"));
        Assertions.assertNotNull(filters.get("classes"));
        Assertions.assertNotNull(filters.get("segments"));
        Assertions.assertNotNull(filters.get("brands"));
        Assertions.assertNotNull(filters.get("truckType"));
        Assertions.assertNotNull(filters.get("family"));
    }
}
