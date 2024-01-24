package com.hysteryale.model.marginAnalyst;

import com.hysteryale.model.Currency;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "margin_analyst_data")
public class MarginAnalystData {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MarginAnalystSeq")
    private int id;
    private String plant;

    @Column(name = "model_code")
    private String modelCode;

    @Column(name = "price_list_region")
    private String priceListRegion;
    private String class_;

    @Column(name = "option_code")
    private String optionCode;
    private String std_opt;
    private String description;

    @Column(name = "list_price")
    private double listPrice;
    private double margin_aop;

    private LocalDate monthYear; // we only needs to care month and year, so the day is always 1

    @ManyToOne(fetch = FetchType.EAGER)
    private Currency currency;
    private double costRMB;
    private String dealer;  // equivalent with billTo in Part

    @Column(name = "dealer_net")
    private double dealerNet;
}
