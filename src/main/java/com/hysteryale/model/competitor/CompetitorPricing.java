package com.hysteryale.model.competitor;

import com.hysteryale.model.BaseModel;
import com.hysteryale.model.Clazz;
import com.hysteryale.model.Country;
import com.hysteryale.model.Region;
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
@Table(name = "competitor_pricing")
public class CompetitorPricing extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @ManyToOne
    private Country country;
    private String plant;

    @Column(name = "competitor_name")
    private String competitorName;
    @ManyToOne
    private Clazz clazz;
    private String category;
    private String series;

    @Column(name = "average_dn")
    private Double averageDN;

    @Column(name = "chinese_brand")
    private Boolean chineseBrand;
    private String model;
    private long actual;
    private long AOPF;
    private long LRFF;

    @Column(name = "hyg_lead_time")
    private Double HYGLeadTime;

    @Column(name = "competitor_lead_time")
    private Double competitorLeadTime;

    @Column(name = "competitor_pricing")
    private Double competitorPricing;

    @Column(name = "dealer_premium_percentage")
    private Double dealerPremiumPercentage;

    @Column(name = "dealer_street_pricing")
    private Double dealerStreetPricing;

    @Column(name = "dealer_handling_cost")
    private Double dealerHandlingCost;

    @Column(name = "dealer_pricing_premium_percentage")
    private Double dealerPricingPremiumPercentage;

    @Column(name = "dealer_pricing_premium")
    private Double dealerPricingPremium;


    // variance % (competitor - (Dealer street + premium))
    @Column(name = "variance_percentage")
    private Double variancePercentage;

    @Column(name = "dealer_net")
    private double dealerNet;

    @Column(name = "market_share")
    private double marketShare;

    @ManyToOne()
    private CompetitorColor color;


    public CompetitorPricing(String region, long actual, long AOPF, long LRFF) {
        this.country = new Country("", new Region(region));
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
