package com.hysteryale.model;

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
@Table(name = "booking")
public class Booking {
    @Id
    @Column(name = "order_no")
    private String orderNo;

    private LocalDate date;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "currency")
    private Currency currency;

    @Column(name = "order_type")
    private String orderType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "region")
    private Region region;

    @Column(name = "ctry_code")
    private String ctryCode;

    @Column(name = "dealerpo")
    private String dealerPO;

    @JoinColumn(name = "dealer")
    @ManyToOne(fetch = FetchType.EAGER)
    private Dealer dealer;

    private String comment;
    private String series;

    @Column(name = "bill_to")
    private String billTo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product")
    private Product product;

    @Column(name = "truck_class")
    private String truckClass;

    //properties that we need to calculate based on raw data
    private long quantity = 1;

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

    public Booking(String region, String plant, String clazz, String series, String model, long quantity, double totalCost, double dealerNet, double dealerNetAfterSurcharge, double marginAfterSurcharge) {

        Product p = new Product(plant, clazz, model);
        this.region = new Region(region);
        this.product = p;
        this.series = series;
        this.quantity = quantity;
        this.totalCost = totalCost;
        this.dealerNet = dealerNet;
        this.dealerNetAfterSurcharge = dealerNetAfterSurcharge;
        this.marginAfterSurcharge = marginAfterSurcharge;
    }

    public Booking(String region, Product product, Currency currency, double totalCost, double dealerNetAfterSurcharge, double marginAfterSurcharge, long quantity) {
        this.currency = currency;
        this.region = new Region(region);
        this.product = product;
        this.quantity = quantity;
        this.totalCost = totalCost;
        this.dealerNetAfterSurcharge = dealerNetAfterSurcharge;
        this.marginAfterSurcharge = marginAfterSurcharge;
    }

    public Booking(String id, Currency currency, long quantity, double dealerNet, double dealerNetAfterSurcharge, double totalCost, double marginAfterSurcharge, double marginPercentageAfterSurcharge) {
        this.orderNo = id;
        this.currency = currency;
        this.dealerNet = dealerNet;
        this.quantity = quantity;
        this.dealerNetAfterSurcharge = dealerNetAfterSurcharge;
        this.totalCost = totalCost;
        this.marginAfterSurcharge = marginAfterSurcharge;
        this.marginPercentageAfterSurcharge = marginPercentageAfterSurcharge;

    }

    public Booking(double dealerNetAfterSurcharge, double totalCost, double marginAfterSurcharge, long quantity) {
        this.dealerNetAfterSurcharge = dealerNetAfterSurcharge;
        this.totalCost = totalCost;
        this.marginAfterSurcharge = marginAfterSurcharge;
        this.quantity = quantity;
    }

    public Booking(long quantity, double totalCost, double dealerNet, double dealerNetAfterSurcharge, double marginAfterSurcharge, double marginPercentageAfterSurcharge) {
        this.quantity = quantity;
        this.totalCost = totalCost;
        this.dealerNet = dealerNet;
        this.dealerNetAfterSurcharge = dealerNetAfterSurcharge;
        this.marginAfterSurcharge = marginAfterSurcharge;
        this.marginPercentageAfterSurcharge = marginPercentageAfterSurcharge;
    }

    public Booking(String order_no, double sum_total_cost, double sum_dealer_net, double sum_dealer_net_after_sur_charge, double sum_margin_after_sur_charge, double sum_margin_percentage_after_surcharge, long sum_quantity) {
        this.orderNo = order_no;
        this.totalCost = sum_total_cost;
        this.dealerNet = sum_dealer_net;
        this.dealerNetAfterSurcharge = sum_dealer_net_after_sur_charge;
        this.marginAfterSurcharge = sum_margin_after_sur_charge;
        this.marginPercentageAfterSurcharge = sum_margin_percentage_after_surcharge;
        this.quantity = sum_quantity;
    }

    public Booking(String orderNo, Currency currency, double dealerNet, double dealerNetAfterSurcharge, double totalCost) {
        this.orderNo = orderNo;
        this.currency = currency;
        this.dealerNet = dealerNet;
        this.dealerNetAfterSurcharge = dealerNetAfterSurcharge;
        this.totalCost = totalCost;
    }

    public Booking(String orderNo, double dealerNetAfterSurcharge, double marginAfterSurcharge){
        this.orderNo = orderNo;
        this.dealerNetAfterSurcharge = dealerNetAfterSurcharge;
        this.marginAfterSurcharge = marginAfterSurcharge;
    }

}
