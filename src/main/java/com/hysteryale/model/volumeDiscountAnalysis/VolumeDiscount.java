/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.model.volumeDiscountAnalysis;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VolumeDiscount {
    private int id;
    private int volume;
    private double discountAdded;
    private double totalDiscount;
    private double pricePerUnit;
    private double totalPrice;
    private double costOfGoodSold;
    private double standardMargin;
    private double standardMarginPercentage;
    private double averageStandardMargin;

    private int expectedUnitSales;
    private double revenue;
    private double standardMarginOnUnitSales;
    private double OCOS;
    private double adjustedStandardMargin;
    private double adjustedStandardMarginPercentage;

    private boolean isHighlight;
}
