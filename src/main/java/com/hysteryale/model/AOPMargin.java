package com.hysteryale.model;

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
public class AOPMargin {
    @Id
    @SequenceGenerator(name = "aopmargin_seq", sequenceName = "aopmargin_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "aopmargin_seq")
    private Long id;
    private String description;
    private double dnUSD;
    private double marginSTD;
    @Column(name = "\"year\"")
    private int year;
    private String plant;
    @Column(name = "meta_series")
    private String metaSeries;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "region")
    private Region region;

    public boolean equals(Region region, String metaSeries, String plant, int year) {
        return this.region.equals(region)
                && this.getMetaSeries().equals(metaSeries)
                && this.getPlant().equals(plant)
                && this.year == year;
    }

}
