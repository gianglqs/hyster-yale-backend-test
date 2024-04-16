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
import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IMMarginAnalystData {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MarginAnalystSeq")
    private int id;
    private String plant;

    @Column(name = "model_code")
    private String modelCode;

    private String clazz;

    @Column(name = "option_code")
    private String optionCode;

    private String description;

    @Column(name = "list_price")
    private double listPrice;
    @Column(name = "margin_aop")
    private double margin_aop;

    @Column(name = "month_year")
    private LocalDate monthYear; // we only needs to care month and year, so the day is always 1

    private String currency;
    private double manufacturingCost;
    private String dealer;  // equivalent with billTo in Part

    @Column(name = "dealer_net")
    private double dealerNet;

    private String fileUUID;
    private String orderNumber;
    private boolean isSPED;

    private int type;
    private String series;
    private String quoteNumber;
    private String region;

    public IMMarginAnalystData(String plant, String modelCode, String optionCode, String description, double listPrice, String currency, double dealerNet, String series) {
        this.plant = plant;
        this.modelCode = modelCode;
        this.optionCode = optionCode;
        this.description = description;
        this.listPrice = listPrice;
        this.currency = currency;
        this.dealerNet = dealerNet;
        this.series = series;
    }
}
