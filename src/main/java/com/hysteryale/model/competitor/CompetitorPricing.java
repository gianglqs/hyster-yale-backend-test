package com.hysteryale.model.competitor;

import com.hysteryale.model.Country;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompetitorPricing {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @ManyToOne
    private Country country;
    private String region;
    private String plant;
    private String competitorName;
    private String clazz;
    private String category;
    private String series;
    private Double averageDN;
    private Boolean chineseBrand;
    private String model;
    private long actual;
    private long AOPF;
    private long LRFF;
    private Double HYGLeadTime;
    private Double competitorLeadTime;
    private Double competitorPricing;
    private Double dealerPremiumPercentage;
    private Double dealerStreetPricing;
    private Double dealerHandlingCost;
    private Double dealerPricingPremiumPercentage;
    private Double dealerPricingPremium;


    // variance % (competitor - (Dealer street + premium))
    private Double variancePercentage;
    private boolean isChineseBrand;
    private double dealerNet;
    private double marketShare;

    @ManyToOne()
    private CompetitorColor color;


    public CompetitorPricing(String region, long actual, long AOPF, long LRFF) {
        this.region = region;
        this.actual = actual;
        this.AOPF = AOPF;
        this.LRFF = LRFF;
    }
    public CompetitorPricing( long actual, long AOPF, long LRFF,String plant) {
        this.plant = plant;
        this.actual = actual;
        this.AOPF = AOPF;
        this.LRFF = LRFF;
    }

    public CompetitorPricing(String rowName, long actual, long aopf, long lrff, double dealerHandlingCost, double competitorPricing, double dealerStreetPricing,
                              double averageDN, double variancePercentage ){

        this.competitorName = rowName;
        this.actual =actual;
        this.AOPF = aopf;
        this.LRFF = lrff;
        this.dealerHandlingCost = dealerHandlingCost;
        this.competitorPricing = competitorPricing;
        this.dealerStreetPricing = dealerStreetPricing;
        this.averageDN = averageDN;
        this.variancePercentage = variancePercentage;
    }

    public CompetitorPricing (String competitorName, double competitorLeadTime, double competitorPricing, double marketShare, CompetitorColor competitorColor) {
        this.competitorName = competitorName;
        this.competitorLeadTime = competitorLeadTime;
        this.competitorPricing = competitorPricing;
        this.marketShare = marketShare;
        this.color = competitorColor;
    }

    public CompetitorPricing (double competitorLeadTime, double competitorPricing, double marketShare, CompetitorColor competitorColor) {
        this.competitorLeadTime = competitorLeadTime;
        this.competitorPricing = competitorPricing;
        this.marketShare = marketShare;
        this.color = competitorColor;
    }
}
