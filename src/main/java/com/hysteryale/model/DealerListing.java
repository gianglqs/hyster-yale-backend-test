package com.hysteryale.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "dealerListing")

public class DealerListing extends  BaseModel{

    @Id
    @Column(name="bill_to_code")
    private String billtoCode;

    @Column(name = "mkg_group")
    private String mkgGroup;

    @Column(name = "dealer_divison")
    private String dealerDivison;

    @Column(name = "dealer_name")
    private String dealerName;

    @Column(name = "territory_manager")
    private String territoryManager;

    @Column(name = "area_businesss_director")
    private String areaBusinesssDirector;

    @Column(name = "big_truck_manager")
    private String bigTruckManager;

    @Column(name = "aftermarket_manager")
    private String aftermarketManager;

    @Column(name = "aftermarket_technical_service_manager")
    private String AftermarketTechnicalServiceManager;

    public DealerListing( String dealerDivison, String dealerName, String territoryManager, String areaBusinesssDirector, String bigTruckManager, String aftermarketManager, String aftermarketTechnicalServiceManager) {
        this.dealerDivison = dealerDivison;
        this.dealerName = dealerName;
        this.territoryManager = territoryManager;
        this.areaBusinesssDirector = areaBusinesssDirector;
        this.bigTruckManager = bigTruckManager;
        this.aftermarketManager = aftermarketManager;
        AftermarketTechnicalServiceManager = aftermarketTechnicalServiceManager;
    }
}
