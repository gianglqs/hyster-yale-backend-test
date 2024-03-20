package com.hysteryale.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Country {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "country_name")
    private String countryName;
    @ManyToOne
    private Region region;

    private String code;

    public Country(String countryName, Region region){
        this.countryName = countryName;
        this.region = region;
    }

    public Country(String regionName){
        this.region = new Region(regionName);
    }
}
