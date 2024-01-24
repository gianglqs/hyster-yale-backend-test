package com.hysteryale.model.marginAnalyst;

import com.hysteryale.model.Currency;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "margin_analyst_summary")
public class MarginAnalystSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "marginAnalystSummarySequence")
    private int id;

    @Column(name = "model_code")
    private String modelCode;

    @Column(name = "month_year")
    private LocalDate monthYear;

    @ManyToOne(fetch = FetchType.EAGER)
    private Currency currency;

    @Column(name = "manufacturing_costrmb")
    private double manufacturingCostRMB;

    @Column(name = "cost_uplift")
    private double costUplift;

    @Column(name = "add_warranty")
    private double addWarranty;
    private double duty;
    private double freight;

    @Column(name = "li_ion_included")
    private boolean liIonIncluded;

    @Column(name = "total_costrmb")
    private double totalCostRMB;

    @Column(name = "total_list_price")
    private double totalListPrice;

    @Column(name = "blended_discount_percentage")
    private double blendedDiscountPercentage;

    @Column(name = "dealer_net")
    private double dealerNet;
    private double margin;

    @Column(name = "margin_aop_rate")
    private double marginAopRate;

    @Column(name = "full_cost_aop_rate")
    private double fullCostAopRate;

    @Column(name = "margin_percentage_aop_rate")
    private double marginPercentAopRate;

    @Column(name = "margin_monthly_rate")
    private double marginMonthlyRate;

    @Column(name = "full_monthly_rate")
    private double fullMonthlyRate;

    @Column(name = "margin_percentage_monthly_rate")
    private double marginPercentMonthlyRate;

    @Column(name = "manufacturing_cost")
    private double manufacturingCost;
    private double warranty;
    private double surcharge;

    @Column(name = "total_cost")
    private double totalCost;

    @Column(name = "manufacturing_cost_aop")
    private double manufacturingCostAop;

    @Column(name = "manufacturing_cost_monthly")
    private double manufacturingCostMonthly;
}
