package com.hysteryale.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class APICDealer {
    @Id
    private int billtoCode;
    private String mkgGroup;
    private String dealerDivison;
    private String dealerName;
    private String territoryManager;
    private String areaBusinesssDirector;
    private String bigTruckManager;
    private String aftermarketManager;
    private String aftermarketTechnicalServiceManager;
}
