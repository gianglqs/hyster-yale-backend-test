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
    private int year;
    private String plant;
    @Column(name = "meta_series")
    private String metaSeries;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "region")
    private Region region;

}
