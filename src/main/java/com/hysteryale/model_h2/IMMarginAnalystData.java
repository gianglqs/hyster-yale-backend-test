package com.hysteryale.model_h2;

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
public class IMMarginAnalystData {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MarginAnalystSeq")
    private int id;
    private String plant;

    @Column(name = "model_code")
    private String modelCode;

    @Column(name = "price_list_region")
    private String priceListRegion;
    private String clazz;

    @Column(name = "option_code")
    private String optionCode;

    @Column(name = "std_otp")
    private String std_opt;
    private String description;

    @Column(name = "list_price")
    private double listPrice;
    @Column(name = "margin_aop")
    private double margin_aop;

    @Temporal(TemporalType.DATE)
    @Column(name = "month_year")
    private Calendar monthYear; // we only needs to care month and year, so the day is always 1

    private String currency;
    private double manufacturingCost;
    private String dealer;  // equivalent with billTo in Part

    @Column(name = "dealer_net")
    private double dealerNet;

    private String fileUUID;
    private String orderNumber;
    private boolean isSPED;

    public IMMarginAnalystData(String plant, String modelCode, String optionCode, String description, double listPrice, Calendar monthYear, String currency, double dealerNet) {
        this.plant = plant;
        this.modelCode = modelCode;
        this.optionCode = optionCode;
        this.description = description;
        this.listPrice = listPrice;
        this.monthYear = monthYear;
        this.currency = currency;
        this.dealerNet = dealerNet;
    }
}
