/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.model;

import com.hysteryale.model.dealer.Dealer;
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
@Table(name = "shipment")
public class Shipment extends BaseModel{
    @Id
    @Column(name = "order_no")
    private String orderNo;

    @JoinColumn(name = "dealer")
    @ManyToOne(fetch = FetchType.EAGER)
    private Dealer dealer;

    private LocalDate date;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "currency")
    private Currency currency;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product")
    private Product product;

    private String series;

    @Column(name = "serial_number")
    private String serialNumber;

    private long quantity;

    private double netRevenue;

    @Column(name = "total_cost")
    private double totalCost;

    @Column(name = "dealer_net")
    private double dealerNet;

    @Column(name = "dealer_net_after_surcharge")
    private double dealerNetAfterSurcharge;

    @Column(name = "margin_after_surcharge")
    private double marginAfterSurcharge;

    @Column(name = "margin_percentage_after_surcharge")
    private double marginPercentageAfterSurcharge;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "aopmargin")
    private AOPMargin AOPMargin;

    @Column(name = "booking_margin_percentage_after_surcharge")
    private Double bookingMarginPercentageAfterSurcharge;

    @Column(name = "booking_margin_after_surcharge")
    private Double bookingMarginAfterSurcharge;

    @Column(name = "booking_dealer_net_after_surcharge")
    private Double bookingDealerNetAfterSurcharge;

    @Column(name = "quote_number")
    private String quoteNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "country")
    private Country country;

    public Shipment(String id, Currency currency, long quantity, double dealerNet, double dealerNetAfterSurcharge, double totalCost, double netRevenue, double marginAfterSurcharge, double marginPercentageAfterSurcharge, Double bookingMargin) {
        this.orderNo = id;
        this.currency = currency;
        this.dealerNet = dealerNet;
        this.quantity = quantity;
        this.dealerNetAfterSurcharge = dealerNetAfterSurcharge;
        this.totalCost = totalCost;
        this.marginAfterSurcharge = marginAfterSurcharge;
        this.marginPercentageAfterSurcharge = marginPercentageAfterSurcharge;
        this.netRevenue = netRevenue;
        this.bookingMarginPercentageAfterSurcharge = bookingMargin;
    }

    public Shipment(String orderNo, Currency currency, double dealerNet, double dealerNetAfterSurcharge, double totalCost, double netRevenue, Double bookingDealerNetAfterSurcharge, Double bookingMarginAfterSurcharge) {
        this.orderNo = orderNo;
        this.currency = currency;
        this.netRevenue = netRevenue;
        this.totalCost = totalCost;
        this.dealerNet = dealerNet;
        this.dealerNetAfterSurcharge = dealerNetAfterSurcharge;
        this.bookingDealerNetAfterSurcharge = bookingDealerNetAfterSurcharge;
        this.bookingMarginAfterSurcharge = bookingMarginAfterSurcharge;
    }
}
