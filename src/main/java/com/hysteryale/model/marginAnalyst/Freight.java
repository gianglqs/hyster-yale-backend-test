package com.hysteryale.model.marginAnalyst;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Calendar;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Freight {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "freight_sequence")
    private int id;
    private String metaSeries;
    private double freight;

    @Temporal(TemporalType.DATE)
    private Calendar monthYear;

    public Freight (String metaSeries, double freight, Calendar monthYear) {
        this.metaSeries = metaSeries;
        this.freight = freight;
        this.monthYear = monthYear;
    }
}
