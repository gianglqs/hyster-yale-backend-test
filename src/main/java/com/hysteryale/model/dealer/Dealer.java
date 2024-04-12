/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.model.dealer;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Dealer {
    @Id
    @SequenceGenerator(name = "dealer_seq", sequenceName = "dealer_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dealer_seq")
    private int id;
    private String name;
    private String description;

    public Dealer(String name) {
        this.name = name;
    }

    public boolean equals(String name) {
        return this.name.equals(name);
    }

    @Column(name="bill_to_code")
    private String billtoCode;

    @Column(name = "mkg_group")
    private String mkgGroup;

    @Column(name = "dealer_divison")
    private String dealerDivison;

//    @Column(name = "dealer_name")
//    private String dealerName;

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
}
