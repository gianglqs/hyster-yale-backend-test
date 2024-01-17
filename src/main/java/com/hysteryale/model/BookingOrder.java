package com.hysteryale.model;

import lombok.*;

import javax.persistence.*;
import java.util.Calendar;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "booking_order")
public class BookingOrder {
    @Id
    @Column(name = "order_no")
    private String orderNo;

    @Temporal(TemporalType.DATE)
    private Calendar date;
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

    @Column(name = "dealer_name")
    private String dealerName;

    private String comment;
    private String series;

    @Column(name = "bill_to")
    private String billTo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_dimension")
    private ProductDimension productDimension;

    @Column(name = "truck_class")
    private String truckClass;

    //properties that we need to calculate based on raw data
    private long quantity = 1;

    @Column(name = "total_cost")
    private double totalCost;

    @Column(name = "dealer_net")
    private double dealerNet;

    @Column(name = "dealer_net_after_sur_charge")
    private double dealerNetAfterSurCharge;

    @Column(name = "margin_after_sur_charge")
    private double marginAfterSurCharge;

    @Column(name = "margin_percentage_after_sur_charge")
    private double marginPercentageAfterSurCharge;

    @Column(name = "aopmargin_percentage")
    private double AOPMarginPercentage;

    public BookingOrder(String region, String plant, String clazz, String series, String model, long quantity, double totalCost, double dealerNet, double dealerNetAfterSurCharge, double marginAfterSurCharge) {

        ProductDimension p = new ProductDimension(plant, clazz, model);
        Region r = new Region(region);
        this.region = r;
        this.productDimension = p;
        this.series = series;
        this.quantity = quantity;
        this.totalCost = totalCost;
        this.dealerNet = dealerNet;
        this.dealerNetAfterSurCharge = dealerNetAfterSurCharge;
        this.marginAfterSurCharge = marginAfterSurCharge;
    }

    public BookingOrder(String region, ProductDimension productDimension, String series, double totalCost, double dealerNetAfterSurCharge, double marginAfterSurCharge, long quantity) {


        Region r = new Region(region);
        this.region = r;
        this.productDimension = productDimension;
        this.series = series;
        this.quantity = quantity;
        this.totalCost = totalCost;
        this.dealerNetAfterSurCharge = dealerNetAfterSurCharge;
        this.marginAfterSurCharge = marginAfterSurCharge;
    }

    public BookingOrder(String id, long quantity, double dealerNet, double dealerNetAfterSurCharge, double totalCost, double marginAfterSurCharge, double marginPercentageAfterSurCharge) {
        this.orderNo = id;
        this.dealerNet = dealerNet;
        this.quantity = quantity;
        this.dealerNetAfterSurCharge = dealerNetAfterSurCharge;
        this.totalCost = totalCost;
        this.marginAfterSurCharge = marginAfterSurCharge;
        this.marginPercentageAfterSurCharge = marginPercentageAfterSurCharge;

    }

    public BookingOrder(double dealerNetAfterSurCharge, double totalCost, double marginAfterSurCharge, long quantity) {
        this.dealerNetAfterSurCharge = dealerNetAfterSurCharge;
        this.totalCost = totalCost;
        this.marginAfterSurCharge = marginAfterSurCharge;
        this.quantity = quantity;
    }

    public BookingOrder(long quantity, double totalCost, double dealerNet, double dealerNetAfterSurCharge, double marginAfterSurCharge, double marginPercentageAfterSurCharge){
        this.quantity = quantity;
        this.totalCost = totalCost;
        this.dealerNet = dealerNet;
        this.dealerNetAfterSurCharge = dealerNetAfterSurCharge;
        this.marginAfterSurCharge = marginAfterSurCharge;
        this.marginPercentageAfterSurCharge = marginPercentageAfterSurCharge;
    }

    public BookingOrder(String order_no, double sum_total_cost, double sum_dealer_net, double sum_dealer_net_after_sur_charge, double sum_margin_after_sur_charge, double sum_margin_percentage_after_surcharge, long sum_quantity){
        this.orderNo = order_no;
        this.totalCost = sum_total_cost;
        this.dealerNet = sum_dealer_net;
        this.dealerNetAfterSurCharge = sum_dealer_net_after_sur_charge;
        this.marginAfterSurCharge = sum_margin_after_sur_charge;
        this.marginPercentageAfterSurCharge = sum_margin_percentage_after_surcharge;
        this.quantity = sum_quantity;
    }
    /**
     * for test Repository in Junit Test
     */
    public BookingOrder(String orderNo, String dealerName, String series){
        this.orderNo = orderNo;
        this.dealerName = dealerName;
        this.series = series;
    }


}
