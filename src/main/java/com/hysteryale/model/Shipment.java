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
@Table(name = "shipment")
public class Shipment {
    @Id
    @Column(name = "order_no")
    private String orderNo;

    @OneToOne
    @MapsId
    @JoinColumn(name = "order_no")
    private Booking booking;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "region")
    private Region region;

    @Column(name = "dealer_name")
    private String dealerName;

    private LocalDate date;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "currency")
    private Currency currency;

    @Column(name = "ctry_code")
    private String ctryCode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_dimension")
    private ProductDimension productDimension;

    private String series;

    @Column(name = "serial_number")
    private String serialNumber;

    private long quantity;

    private double netRevenue;

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

    @Column(name = "booking_margin_percentage_after_sur_charge")
    private double bookingMarginPercentageAfterSurCharge;

    @Column(name = "aopmargin_percentage")
    private double AOPMarginPercentage;

    public Shipment(String id, long quantity, double dealerNet, double dealerNetAfterSurCharge, double totalCost, double netRevenue, double marginAfterSurCharge, double marginPercentageAfterSurCharge) {
        this.orderNo = id;
        this.dealerNet = dealerNet;
        this.quantity = quantity;
        this.dealerNetAfterSurCharge = dealerNetAfterSurCharge;
        this.totalCost = totalCost;
        this.marginAfterSurCharge = marginAfterSurCharge;
        this.marginPercentageAfterSurCharge = marginPercentageAfterSurCharge;
        this.netRevenue = netRevenue;
    }

}
