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
public class Warranty {
    @Id
    @GeneratedValue(strategy =  GenerationType.SEQUENCE, generator = "warranty_sequence")
    private int id;
    private String clazz;

    @Temporal(TemporalType.DATE)
    private Calendar monthYear;
    private double warranty;

    public Warranty (String clazz, Calendar monthYear, double warranty) {
        this.clazz = clazz;
        this.monthYear = monthYear;
        this.warranty = warranty;
    }
}
