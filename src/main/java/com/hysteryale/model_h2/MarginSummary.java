/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.model_h2;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MarginSummary {
    @EmbeddedId
    private MarginSummaryId id;

    @Column(name = "total_manufacturing_cost")
    private double totalManufacturingCost;

    @Column(name = "cost_uplift")
    private double costUplift;

    @Column(name = "add_warranty_percentage")
    private double addWarranty;
    private double surcharge;
    private double duty;
    private double freight;

    @Column(name = "li_ion_included")
    private boolean liIonIncluded;

    @Column(name = "total_cost")
    private double totalCost;

    @Column(name = "full_cost_aop_rate")
    private double fullCostAOPRate;

    @Column(name = "total_list_price")
    private double totalListPrice;

    @Column(name = "blended_discount_percentage")
    private double blendedDiscountPercentage;

    @Column(name = "dealer_net")
    private double dealerNet;
    private double margin;

    @Column(name = "margin_aop_rate")
    private double marginAOPRate;

    @Column(name = "margin_percentage_aop_rate")
    private double marginPercentageAOPRate;

    @Column(name = "manufacturing_cost_usd")
    private double manufacturingCostUSD;

    @Column(name = "warranty_cost")
    private double warrantyCost;

    @Column(name = "surcharge_cost")
    private double surchargeCost;

    @Column(name = "duty_cost")
    private double dutyCost;

    @Column(name = "total_cost_without_freight")
    private double totalCostWithoutFreight;

    @Column(name = "total_cost_with_freight")
    private double totalCostWithFreight;

    private String fileUUID;
    private String orderNumber;
    private String plant;

    public MarginSummary(MarginSummaryId id, double totalManufacturingCost, double costUplift, double addWarranty, double surcharge, double duty, double freight, boolean liIonIncluded, double totalCost, double totalListPrice, double blendedDiscountPercentage, double dealerNet, double margin, double marginAOPRate, double manufacturingCostUSD, double warrantyCost, double surchargeCost, double dutyCost, double totalCostWithoutFreight, double totalCostWithFreight, String fileUUID, String plant) {
        this.id = id;
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
        this.marginAOPRate = marginAOPRate;
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
