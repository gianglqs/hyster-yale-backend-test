/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.model_h2;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IMMarginAnalystSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "marginAnalystSummarySequence")
    private int id;

    private String modelCode;

    private String durationUnit;
    private String currency;

    private double totalManufacturingCost;
    private double costUplift;
    private double addWarranty;
    private double surcharge;
    private double duty;
    private double freight;
    private boolean liIonIncluded;
    private double totalCost;

    private double totalListPrice;
    private double blendedDiscountPercentage;
    private double dealerNet;
    private double margin;

    private double marginAopRate;
    private double fullCostAopRate;
    private double marginPercentAopRate;

    private double marginMonthlyRate;
    private double fullMonthlyRate;
    private double marginPercentMonthlyRate;

    private double manufacturingCostUSD;
    private double warrantyCost;
    private double surchargeCost;
    private double dutyCost;
    private double totalCostWithoutFreight;
    private double totalCostWithFreight;

    private double manufacturingCostAop;
    private double manufacturingCostMonthly;

    private String fileUUID;
    private String orderNumber;
    private String plant;
    private int type;

    public IMMarginAnalystSummary(String modelCode, String currency, double totalManufacturingCost, double costUplift, double addWarranty, double surcharge, double duty, double freight, boolean liIonIncluded, double totalCost, double totalListPrice, double blendedDiscountPercentage, double dealerNet, double margin, double marginAopRate, double manufacturingCostUSD, double warrantyCost, double surchargeCost, double dutyCost, double totalCostWithoutFreight, double totalCostWithFreight, String fileUUID, String plant) {
        this.modelCode = modelCode;
        this.currency = currency;
        this.totalManufacturingCost = totalManufacturingCost;
        this.costUplift = costUplift;
        this.addWarranty = addWarranty;
        this.surcharge = surcharge;
        this.duty = duty;
        this.freight = freight;
        this.liIonIncluded = liIonIncluded;
        this.totalCost = totalCost;
        this.totalListPrice = totalListPrice;
        this.blendedDiscountPercentage = blendedDiscountPercentage;
        this.dealerNet = dealerNet;
        this.margin = margin;
        this.marginAopRate = marginAopRate;
        this.manufacturingCostUSD = manufacturingCostUSD;
        this.warrantyCost = warrantyCost;
        this.surchargeCost = surchargeCost;
        this.dutyCost = dutyCost;
        this.totalCostWithoutFreight = totalCostWithoutFreight;
        this.totalCostWithFreight = totalCostWithFreight;
        this.fileUUID = fileUUID;
        this.plant = plant;
    }
}
