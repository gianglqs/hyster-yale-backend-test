/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.service;

import com.hysteryale.model.filters.CalculatorModel;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.model.payLoad.AdjustmentPayLoad;
import com.hysteryale.utils.CurrencyFormatUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@SpringBootTest
@Slf4j
@SuppressWarnings("unchecked")
public class AdjustmentServiceTest {
    @Resource
    AdjustmentService adjustmentService;

    FilterModel filters;
    CalculatorModel calculatorModel;

    /**
     * Reset the filters to initial state
     */
    private void resetFilters() {
        filters = new FilterModel(
                "",
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                "",
                "",
                "",
                "",
                "",
                null,
                1500,
                1,
                "",
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                "",
                null);
    }

    @BeforeEach
    public void setUp(){
        resetFilters();
        calculatorModel = new CalculatorModel(0, 0, 0, 0);
    }

    private void assertTotalValue(AdjustmentPayLoad totalResult, long noOfOrders, int totalAdditionalVolume,
                                  double totalAdjCost, double totalManualAdjFreight, double totalManualAdjFx,
                                  double totalOriginalDN, double totalOriginalMargin, double totalOriginalMarginPercentage,
                                  double totalNewDN, double totalNewMargin, double totalManualAdjCost) {
        Assertions.assertEquals(noOfOrders, totalResult.getNoOfOrder());
        Assertions.assertEquals(totalAdditionalVolume, totalResult.getAdditionalVolume());

        Assertions.assertEquals(
                CurrencyFormatUtils.formatDoubleValue(totalAdjCost, CurrencyFormatUtils.decimalFormatFourDigits),
                CurrencyFormatUtils.formatDoubleValue(totalResult.getManualAdjCost(), CurrencyFormatUtils.decimalFormatFourDigits)
        );
        Assertions.assertEquals(
                CurrencyFormatUtils.formatDoubleValue(totalManualAdjFreight, CurrencyFormatUtils.decimalFormatFourDigits),
                CurrencyFormatUtils.formatDoubleValue(totalResult.getManualAdjFreight(), CurrencyFormatUtils.decimalFormatFourDigits)
        );
        Assertions.assertEquals(
                CurrencyFormatUtils.formatDoubleValue(totalManualAdjFx, CurrencyFormatUtils.decimalFormatFourDigits),
                CurrencyFormatUtils.formatDoubleValue(totalResult.getManualAdjFX(), CurrencyFormatUtils.decimalFormatFourDigits)
        );
        Assertions.assertEquals(
                CurrencyFormatUtils.formatDoubleValue(totalOriginalDN, CurrencyFormatUtils.decimalFormatFourDigits),
                CurrencyFormatUtils.formatDoubleValue(totalResult.getOriginalDN(), CurrencyFormatUtils.decimalFormatFourDigits)
        );
        Assertions.assertEquals(
                CurrencyFormatUtils.formatDoubleValue(totalOriginalMargin, CurrencyFormatUtils.decimalFormatFourDigits),
                CurrencyFormatUtils.formatDoubleValue(totalResult.getOriginalMargin(), CurrencyFormatUtils.decimalFormatFourDigits)
        );
//        Assertions.assertEquals(
//                CurrencyFormatUtils.formatDoubleValue(totalOriginalMarginPercentage, CurrencyFormatUtils.decimalFormatFourDigits),
//                CurrencyFormatUtils.formatDoubleValue(totalResult.getOriginalMarginPercentage(), CurrencyFormatUtils.decimalFormatFourDigits)
//        );
        Assertions.assertEquals(
                CurrencyFormatUtils.formatDoubleValue(totalNewDN, CurrencyFormatUtils.decimalFormatFourDigits),
                CurrencyFormatUtils.formatDoubleValue(totalResult.getNewDN(), CurrencyFormatUtils.decimalFormatFourDigits)
        );
        Assertions.assertEquals(
                CurrencyFormatUtils.formatDoubleValue(totalNewMargin, CurrencyFormatUtils.decimalFormatFourDigits),
                CurrencyFormatUtils.formatDoubleValue(totalResult.getNewMargin(), CurrencyFormatUtils.decimalFormatFourDigits)
        );
        Assertions.assertEquals(
                CurrencyFormatUtils.formatDoubleValue(totalManualAdjCost, CurrencyFormatUtils.decimalFormatFourDigits),
                CurrencyFormatUtils.formatDoubleValue(totalResult.getTotalManualAdjCost(), CurrencyFormatUtils.decimalFormatFourDigits)
        );

    }

    @Test
    public void testGetAdjustmentByFilter_region() throws ParseException {
        resetFilters();

        String region = "Asia";
        filters.setRegions(Collections.singletonList(region));

        Map<String, Object> result = adjustmentService.getAdjustmentByFilter(filters, calculatorModel);
        Assertions.assertNotNull(result.get("total"));
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertNotNull(result.get("listAdjustment"));

        AdjustmentPayLoad totalResult = ((List<AdjustmentPayLoad>) result.get("total")).get(0);
        List<AdjustmentPayLoad> resultList = (List<AdjustmentPayLoad>) result.get("listAdjustment");

        long noOfOrders = 0;
        int totalAdditionalVolume = 0;
        double totalAdjCost = 0.0, totalManualAdjFreight = 0.0, totalManualAdjFx = 0.0, totalManualAdjCost = 0.0;
        double totalOriginalDN = 0.0, totalOriginalMargin = 0.0, totalOriginalMarginPercentage = 0.0;
        double totalNewDN = 0.0, totalNewMargin = 0.0;

        Assertions.assertFalse(resultList.isEmpty());
        for(AdjustmentPayLoad adj : resultList) {
            Assertions.assertEquals(region, adj.getRegion());

            noOfOrders += adj.getNoOfOrder();
            totalAdditionalVolume += adj.getAdditionalVolume();
            totalAdjCost += adj.getManualAdjCost();
            totalManualAdjFreight += adj.getManualAdjFreight();
            totalManualAdjFx += adj.getManualAdjFX();
            totalManualAdjCost += adj.getTotalManualAdjCost();
            totalOriginalDN += adj.getOriginalDN();
            totalOriginalMargin += adj.getOriginalMargin();
            totalOriginalMarginPercentage += adj.getOriginalMarginPercentage();
            totalNewDN += adj.getNewDN();
            totalNewMargin += adj.getNewMargin();
        }
        totalManualAdjFreight = totalManualAdjFreight / resultList.size();
        totalManualAdjFx = totalManualAdjFx / resultList.size();

        assertTotalValue(totalResult, noOfOrders, totalAdditionalVolume, totalAdjCost, totalManualAdjFreight, totalManualAdjFx,
                totalOriginalDN, totalOriginalMargin, totalOriginalMarginPercentage, totalNewDN, totalNewMargin,totalManualAdjCost);
    }

    @Test
    public void testGetAdjustmentByFilter_plant() throws ParseException {
        resetFilters();

        String plant = "Ruyi";
        filters.setPlants(Collections.singletonList(plant));

        Map<String, Object> result = adjustmentService.getAdjustmentByFilter(filters, calculatorModel);
        Assertions.assertNotNull(result.get("total"));
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertNotNull(result.get("listAdjustment"));

        AdjustmentPayLoad totalResult = ((List<AdjustmentPayLoad>) result.get("total")).get(0);
        List<AdjustmentPayLoad> resultList = (List<AdjustmentPayLoad>) result.get("listAdjustment");

        long noOfOrders = 0;
        int totalAdditionalVolume = 0;
        double totalAdjCost = 0.0, totalManualAdjFreight = 0.0, totalManualAdjFx = 0.0, totalManualAdjCost = 0.0;
        double totalOriginalDN = 0.0, totalOriginalMargin = 0.0, totalOriginalMarginPercentage = 0.0;
        double totalNewDN = 0.0, totalNewMargin = 0.0;

        Assertions.assertFalse(resultList.isEmpty());
        for(AdjustmentPayLoad adj : resultList) {
            Assertions.assertEquals(plant, adj.getPlant());

            noOfOrders += adj.getNoOfOrder();
            totalAdditionalVolume += adj.getAdditionalVolume();
            totalAdjCost += adj.getManualAdjCost();
            totalManualAdjFreight += adj.getManualAdjFreight();
            totalManualAdjFx += adj.getManualAdjFX();
            totalManualAdjCost += adj.getTotalManualAdjCost();
            totalOriginalDN += adj.getOriginalDN();
            totalOriginalMargin += adj.getOriginalMargin();
            totalOriginalMarginPercentage += adj.getOriginalMarginPercentage();
            totalNewDN += adj.getNewDN();
            totalNewMargin += adj.getNewMargin();
        }
        totalManualAdjFreight = totalManualAdjFreight / resultList.size();
        totalManualAdjFx = totalManualAdjFx / resultList.size();

        assertTotalValue(totalResult, noOfOrders, totalAdditionalVolume, totalAdjCost, totalManualAdjFreight, totalManualAdjFx,
                totalOriginalDN, totalOriginalMargin, totalOriginalMarginPercentage, totalNewDN, totalNewMargin,totalManualAdjCost);
    }

    @Test
    public void testGetAdjustmentByFilter_metaSeries() throws ParseException {
        resetFilters();

        String metaSeries = "3C9";
        filters.setMetaSeries(Collections.singletonList(metaSeries));

        Map<String, Object> result = adjustmentService.getAdjustmentByFilter(filters, calculatorModel);
        Assertions.assertNotNull(result.get("total"));
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertNotNull(result.get("listAdjustment"));

        AdjustmentPayLoad totalResult = ((List<AdjustmentPayLoad>) result.get("total")).get(0);
        List<AdjustmentPayLoad> resultList = (List<AdjustmentPayLoad>) result.get("listAdjustment");

        long noOfOrders = 0;
        int totalAdditionalVolume = 0;
        double totalAdjCost = 0.0, totalManualAdjFreight = 0.0, totalManualAdjFx = 0.0, totalManualAdjCost = 0.0;
        double totalOriginalDN = 0.0, totalOriginalMargin = 0.0, totalOriginalMarginPercentage = 0.0;
        double totalNewDN = 0.0, totalNewMargin = 0.0;

        Assertions.assertFalse(resultList.isEmpty());
        for(AdjustmentPayLoad adj : resultList) {
            Assertions.assertEquals(metaSeries, adj.getMetaSeries().substring(1));

            noOfOrders += adj.getNoOfOrder();
            totalAdditionalVolume += adj.getAdditionalVolume();
            totalAdjCost += adj.getManualAdjCost();
            totalManualAdjFreight += adj.getManualAdjFreight();
            totalManualAdjFx += adj.getManualAdjFX();
            totalManualAdjCost += adj.getTotalManualAdjCost();
            totalOriginalDN += adj.getOriginalDN();
            totalOriginalMargin += adj.getOriginalMargin();
            totalOriginalMarginPercentage += adj.getOriginalMarginPercentage();
            totalNewDN += adj.getNewDN();
            totalNewMargin += adj.getNewMargin();
        }
        totalManualAdjFreight = totalManualAdjFreight / resultList.size();
        totalManualAdjFx = totalManualAdjFx / resultList.size();

        assertTotalValue(totalResult, noOfOrders, totalAdditionalVolume, totalAdjCost, totalManualAdjFreight, totalManualAdjFx,
                totalOriginalDN, totalOriginalMargin, totalOriginalMarginPercentage, totalNewDN, totalNewMargin,totalManualAdjCost);
    }

    @Test
    public void testGetAdjustmentByFilter_class() throws ParseException {
        resetFilters();

        String clazz = "Class 3";
        filters.setClasses(Collections.singletonList(clazz));

        Map<String, Object> result = adjustmentService.getAdjustmentByFilter(filters, calculatorModel);
        Assertions.assertNotNull(result.get("total"));
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertNotNull(result.get("listAdjustment"));

        AdjustmentPayLoad totalResult = ((List<AdjustmentPayLoad>) result.get("total")).get(0);
        List<AdjustmentPayLoad> resultList = (List<AdjustmentPayLoad>) result.get("listAdjustment");

        long noOfOrders = 0;
        int totalAdditionalVolume = 0;
        double totalAdjCost = 0.0, totalManualAdjFreight = 0.0, totalManualAdjFx = 0.0, totalManualAdjCost = 0.0;
        double totalOriginalDN = 0.0, totalOriginalMargin = 0.0, totalOriginalMarginPercentage = 0.0;
        double totalNewDN = 0.0, totalNewMargin = 0.0;

        Assertions.assertFalse(resultList.isEmpty());
        for(AdjustmentPayLoad adj : resultList) {
            Assertions.assertEquals(clazz, adj.getClazz());

            noOfOrders += adj.getNoOfOrder();
            totalAdditionalVolume += adj.getAdditionalVolume();
            totalAdjCost += adj.getManualAdjCost();
            totalManualAdjFreight += adj.getManualAdjFreight();
            totalManualAdjFx += adj.getManualAdjFX();
            totalManualAdjCost += adj.getTotalManualAdjCost();
            totalOriginalDN += adj.getOriginalDN();
            totalOriginalMargin += adj.getOriginalMargin();
            totalOriginalMarginPercentage += adj.getOriginalMarginPercentage();
            totalNewDN += adj.getNewDN();
            totalNewMargin += adj.getNewMargin();
        }
        totalManualAdjFreight = totalManualAdjFreight / resultList.size();
        totalManualAdjFx = totalManualAdjFx / resultList.size();

        assertTotalValue(totalResult, noOfOrders, totalAdditionalVolume, totalAdjCost, totalManualAdjFreight, totalManualAdjFx,
                totalOriginalDN, totalOriginalMargin, totalOriginalMarginPercentage, totalNewDN, totalNewMargin,totalManualAdjCost);
    }

    @Test
    public void testGetAdjustmentByFilter_model() throws ParseException {
        resetFilters();

        String modelCode = "PC1.5";
        filters.setModels(Collections.singletonList(modelCode));

        Map<String, Object> result = adjustmentService.getAdjustmentByFilter(filters, calculatorModel);
        Assertions.assertNotNull(result.get("total"));
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertNotNull(result.get("listAdjustment"));

        AdjustmentPayLoad totalResult = ((List<AdjustmentPayLoad>) result.get("total")).get(0);
        List<AdjustmentPayLoad> resultList = (List<AdjustmentPayLoad>) result.get("listAdjustment");

        long noOfOrders = 0;
        int totalAdditionalVolume = 0;
        double totalAdjCost = 0.0, totalManualAdjFreight = 0.0, totalManualAdjFx = 0.0, totalManualAdjCost = 0.0;
        double totalOriginalDN = 0.0, totalOriginalMargin = 0.0, totalOriginalMarginPercentage = 0.0;
        double totalNewDN = 0.0, totalNewMargin = 0.0;

        Assertions.assertFalse(resultList.isEmpty());
        for(AdjustmentPayLoad adj : resultList) {
            Assertions.assertEquals(modelCode, adj.getModel());

            noOfOrders += adj.getNoOfOrder();
            totalAdditionalVolume += adj.getAdditionalVolume();
            totalAdjCost += adj.getManualAdjCost();
            totalManualAdjFreight += adj.getManualAdjFreight();
            totalManualAdjFx += adj.getManualAdjFX();
            totalManualAdjCost += adj.getTotalManualAdjCost();
            totalOriginalDN += adj.getOriginalDN();
            totalOriginalMargin += adj.getOriginalMargin();
            totalOriginalMarginPercentage += adj.getOriginalMarginPercentage();
            totalNewDN += adj.getNewDN();
            totalNewMargin += adj.getNewMargin();
        }
        totalManualAdjFreight = totalManualAdjFreight / resultList.size();
        totalManualAdjFx = totalManualAdjFx / resultList.size();

        assertTotalValue(totalResult, noOfOrders, totalAdditionalVolume, totalAdjCost, totalManualAdjFreight, totalManualAdjFx,
                totalOriginalDN, totalOriginalMargin, totalOriginalMarginPercentage, totalNewDN, totalNewMargin,totalManualAdjCost);
    }

    @Test
    public void testGetAdjustmentByFilter_marginPercentage() throws ParseException {
        resetFilters();

        String marginPercentage = "<20% Margin";
        filters.setMarginPercentage(marginPercentage);

        Map<String, Object> result = adjustmentService.getAdjustmentByFilter(filters, calculatorModel);
        Assertions.assertNotNull(result.get("total"));
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertNotNull(result.get("listAdjustment"));

        AdjustmentPayLoad totalResult = ((List<AdjustmentPayLoad>) result.get("total")).get(0);
        List<AdjustmentPayLoad> resultList = (List<AdjustmentPayLoad>) result.get("listAdjustment");

        long noOfOrders = 0;
        int totalAdditionalVolume = 0;
        double totalAdjCost = 0.0, totalManualAdjFreight = 0.0, totalManualAdjFx = 0.0, totalManualAdjCost = 0.0;
        double totalOriginalDN = 0.0, totalOriginalMargin = 0.0, totalOriginalMarginPercentage = 0.0;
        double totalNewDN = 0.0, totalNewMargin = 0.0;

        Assertions.assertFalse(resultList.isEmpty());
        for(AdjustmentPayLoad adj : resultList) {
            Assertions.assertTrue(adj.getOriginalMarginPercentage() <= 0.2 || Double.isNaN(adj.getOriginalMarginPercentage()));

            noOfOrders += adj.getNoOfOrder();
            totalAdditionalVolume += adj.getAdditionalVolume();
            totalAdjCost += adj.getManualAdjCost();
            totalManualAdjFreight += adj.getManualAdjFreight();
            totalManualAdjFx += adj.getManualAdjFX();
            totalManualAdjCost += adj.getTotalManualAdjCost();
            totalOriginalDN += adj.getOriginalDN();
            totalOriginalMargin += adj.getOriginalMargin();
            totalOriginalMarginPercentage += adj.getOriginalMarginPercentage();
            totalNewDN += adj.getNewDN();
            totalNewMargin += adj.getNewMargin();
        }
        totalManualAdjFreight = totalManualAdjFreight / resultList.size();
        totalManualAdjFx = totalManualAdjFx / resultList.size();

        assertTotalValue(totalResult, noOfOrders, totalAdditionalVolume, totalAdjCost, totalManualAdjFreight, totalManualAdjFx,
                totalOriginalDN, totalOriginalMargin, totalOriginalMarginPercentage, totalNewDN, totalNewMargin,totalManualAdjCost);
    }
}
