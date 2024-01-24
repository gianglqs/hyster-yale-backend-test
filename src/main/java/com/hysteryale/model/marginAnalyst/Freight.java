package com.hysteryale.model.marginAnalyst;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
    private String metaSeries;
    private double freight;

    private LocalDate monthYear;

    public Freight (String metaSeries, double freight, LocalDate monthYear) {
        this.metaSeries = metaSeries;
        this.freight = freight;
        this.monthYear = monthYear;
    }
}
