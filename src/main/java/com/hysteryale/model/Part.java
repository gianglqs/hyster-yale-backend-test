/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "part")
public class Part {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "partSequence")
    private int id;

    @Column(name = "quote_id")
    private String quoteId;

    private String description;
    private int quantity;
    private String image;

    @Column(name = "model_code")
    private String modelCode;

    @Column(name = "order_number")
    private String orderNumber;

    private String series;
    @ManyToOne
    private Clazz clazz;
    private String region;

    @Column(name = "part_number")
    private String partNumber;

    @Column(name = "list_price")
    private double listPrice;

    private double discount;

    @Column(name = "discount_percentage")
    private double discountPercentage;

    @Column(name = "bill_to")
    private String billTo;

    @Column(name = "net_price_each")
    private double netPriceEach;

    @Column(name = "discount_to_customer_percentage")
    private double discountToCustomerPercentage;

    @Column(name = "customer_price")
    private double customerPrice;

    @Column(name = "extended_customer_price")
    private double extendedCustomerPrice;

    @Column(name = "option_type")
    private String optionType;

    @Column(name = "order_booked_date")
    private Date orderBookedDate;

    @Column(name = "order_request_date")
    private Date orderRequestDate;

    @Column(name = "recorded_time")
    private LocalDate recordedTime;
    @ManyToOne(fetch = FetchType.EAGER)
    private Currency currency;
    private boolean isSPED;


    public Part(String quoteId, int quantity, String orderNumber, String modelCode, String series, String partNumber, double listPrice, double discount, double discountPercentage, String billTo, double netPriceEach, double customerPrice, double extendedCustomerPrice, Currency currency, Clazz clazz, String region, boolean isSPED) {
        this.quoteId = quoteId;
        this.quantity = quantity;
        this.modelCode = modelCode;
        this.series = series;
        this.partNumber = partNumber;
        this.listPrice = listPrice;
        this.discount = discount;
        this.discountPercentage = discountPercentage;
        this.billTo = billTo;
        this.netPriceEach = netPriceEach;
        this.customerPrice = customerPrice;
        this.extendedCustomerPrice = extendedCustomerPrice;
        this.currency = currency;
        this.orderNumber = orderNumber;
        this.clazz = clazz;
        this.region = region;
        this.isSPED = isSPED;
    }

    public Part(String partNumber, String image, Currency currency, double listPrice, String description) {
        this.description = description;
        this.image = image;
        this.partNumber = partNumber;
        this.currency = currency;
        this.listPrice = listPrice;
    }
}
