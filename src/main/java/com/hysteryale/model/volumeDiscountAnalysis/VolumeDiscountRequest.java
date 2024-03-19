package com.hysteryale.model.volumeDiscountAnalysis;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VolumeDiscountRequest {
    private double pricePerUnit;
    private double costOfGoodSold;
    private double discountPercentage;
    private int lever;

    private int expectedUnitSales;
    private double OCOS;
}
