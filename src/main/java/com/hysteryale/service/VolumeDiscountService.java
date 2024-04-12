/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.service;

import com.hysteryale.model.volumeDiscountAnalysis.VolumeDiscount;
import com.hysteryale.model.volumeDiscountAnalysis.VolumeDiscountRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class VolumeDiscountService {
    public List<VolumeDiscount> calculateVolumeDiscount(VolumeDiscountRequest request) {
        List<VolumeDiscount> volumeDiscountList = new ArrayList<>();

        int highlightedIndex = 0;
        double maxStandardMargin = 0;

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
        return volumeDiscountList;
    }

}
