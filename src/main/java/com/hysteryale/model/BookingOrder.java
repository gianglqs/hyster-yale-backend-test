package com.hysteryale.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private String model;

    @Column(name = "truck_class")
    private String truckClass;

    //properties that we need to calculate based on raw data
    private long  quantity = 1;

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

    public BookingOrder(String region, String plant, String clazz, String series, String model, long quantity, double totalCost,double dealerNet, double dealerNetAfterSurCharge, double marginAfterSurCharge){

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
        this.model = model;
    }

}
