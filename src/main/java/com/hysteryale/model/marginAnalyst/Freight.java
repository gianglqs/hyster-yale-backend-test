/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.model.marginAnalyst;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Freight {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "freight_sequence")
    private int id;

    @Column(name = "meta_series")
    private String metaSeries;
    private double freight;

    @Column(name = "month_year")
    private LocalDate monthYear;

    public Freight (String metaSeries, double freight, LocalDate monthYear) {
        this.metaSeries = metaSeries;
        this.freight = freight;
        this.monthYear = monthYear;
    }
}
