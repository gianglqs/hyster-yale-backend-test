/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.service;

import com.hysteryale.model.volumeDiscountAnalysis.VolumeDiscount;
import com.hysteryale.model.volumeDiscountAnalysis.VolumeDiscountRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Slf4j
public class VolumeDiscountServiceTest {
    @Resource
    VolumeDiscountService volumeDiscountService;


    @Test
    public void testCalculateVolumeDiscount() {
        VolumeDiscountRequest request = new VolumeDiscountRequest();

        int highlightedIndex = 0;
        double maxStandardMargin = 0;
        List<VolumeDiscount> volumeDiscountList = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            int volume = 1 + i * request.getLever();

            double discountAdded = request.getDiscountPercentage();
            if(i == 0) discountAdded = 0.0;

            double totalDiscount = discountAdded * (volume - 1);

            double pricePerUnit = request.getPricePerUnit() * (1 - totalDiscount);
            double totalPrice = pricePerUnit * volume;

            double costOfGoodSold = request.getCostOfGoodSold() * volume;

            double standardMargin = totalPrice - costOfGoodSold;
            double standardMarginPercentage = standardMargin / totalPrice;
            double averageStandardMargin = standardMargin / volume;

            int expectedUnitSales = request.getExpectedUnitSales();
            double revenue = pricePerUnit * expectedUnitSales;
            double standardMarginOnUnitSales = averageStandardMargin * expectedUnitSales;
            double adjustedStandardMargin = standardMarginOnUnitSales - request.getOCOS();
            double adjustedStandardMarginPercentage = adjustedStandardMargin / revenue;

            VolumeDiscount volumeDiscount = new VolumeDiscount(
                    i, volume, discountAdded * request.getLever(), totalDiscount, pricePerUnit, totalPrice,
                    costOfGoodSold, standardMargin, standardMarginPercentage, averageStandardMargin,
                    expectedUnitSales, revenue, standardMarginOnUnitSales,
                    request.getOCOS(), adjustedStandardMargin, adjustedStandardMarginPercentage, false
            );
            volumeDiscountList.add(volumeDiscount);

            if(i == 0) {
                maxStandardMargin = standardMargin;
            }
            else if(standardMargin > maxStandardMargin) {
                highlightedIndex = i;
                maxStandardMargin = standardMargin;
            }
        }
        volumeDiscountList.get(highlightedIndex).setHighlight(true);

        List<VolumeDiscount> result = volumeDiscountService.calculateVolumeDiscount(request);
        assertVolumeDiscount(result, volumeDiscountList);
    }

    void assertVolumeDiscount(List<VolumeDiscount> resultList, List<VolumeDiscount> expectedList) {
        Assertions.assertEquals(expectedList.size(), resultList.size());
        for (int i = 0; i < 10; i++) {
            VolumeDiscount expected = expectedList.get(i);
            VolumeDiscount result = resultList.get(i);

            Assertions.assertEquals(expected.getVolume(), result.getVolume());
            Assertions.assertEquals(expected.getDiscountAdded(), result.getDiscountAdded());
            Assertions.assertEquals(expected.getTotalDiscount(), result.getTotalDiscount());
            Assertions.assertEquals(expected.getPricePerUnit(), result.getPricePerUnit());
            Assertions.assertEquals(expected.getTotalPrice(), result.getTotalPrice());
            Assertions.assertEquals(expected.getCostOfGoodSold(), result.getCostOfGoodSold());
            Assertions.assertEquals(expected.getStandardMargin(), result.getStandardMargin());
            Assertions.assertEquals(expected.getStandardMarginPercentage(), result.getStandardMarginPercentage());
            Assertions.assertEquals(expected.getAverageStandardMargin(), result.getAverageStandardMargin());
            Assertions.assertEquals(expected.getExpectedUnitSales(), result.getExpectedUnitSales());
            Assertions.assertEquals(expected.getRevenue(), result.getRevenue());
            Assertions.assertEquals(expected.getStandardMarginOnUnitSales(), result.getStandardMarginOnUnitSales());
            Assertions.assertEquals(expected.getAdjustedStandardMargin(), result.getAdjustedStandardMargin());
            Assertions.assertEquals(expected.getAdjustedStandardMarginPercentage(), result.getAdjustedStandardMarginPercentage());
            Assertions.assertEquals(expected.getOCOS(), result.getOCOS());
            Assertions.assertEquals(expected.isHighlight(), result.isHighlight());
        }
    }
}
