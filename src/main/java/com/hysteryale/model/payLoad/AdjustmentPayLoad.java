/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.model.payLoad;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdjustmentPayLoad {
    private long id;
    private String region;
    private String plant;
    private String clazz;
    private String metaSeries;
    private String model;
    private long noOfOrder;
    private double manualAdjCost;
    private double manualAdjFreight;
    private double manualAdjFX;
    private double totalManualAdjCost;
    private double originalDN;
    private double originalMargin;
    private double originalMarginPercentage;
    private double newDN;
    private double newMargin;
    private double newMarginPercentage;
    private int additionalVolume;

    public AdjustmentPayLoad(String region, String plant, String clazz, String metaSeries, String model, int noOfOrder, double manualAdjCost, double manualAdjFreight, double manualAdjFX, double totalManualAdjCost, double originalDN, double originalMargin, double originalMarginPercentage, double newDN, double newMargin, double newMarginPercentage) {
        this.region = region;
        this.plant = plant;
        this.clazz = clazz;
        this.metaSeries = metaSeries;
        this.model = model;
        this.noOfOrder = noOfOrder;
        this.manualAdjCost = manualAdjCost;
        this.manualAdjFreight = manualAdjFreight;
        this.manualAdjFX = manualAdjFX;
        this.totalManualAdjCost = totalManualAdjCost;
        this.originalDN = originalDN;
        this.originalMargin = originalMargin;
        this.originalMarginPercentage = originalMarginPercentage;
        this.newDN = newDN;
        this.newMargin = newMargin;
        this.newMarginPercentage = newMarginPercentage;
    }


}
