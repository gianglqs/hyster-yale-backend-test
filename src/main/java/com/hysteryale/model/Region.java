package com.hysteryale.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Region {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @Column(name = "region_short_name")
    private String regionShortName;

    @Column(name = "region_name")
    private String regionName;

    public Region(String regionName) {
        this.regionName = regionName;
    }
}
