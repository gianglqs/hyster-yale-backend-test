package com.hysteryale.model.marginAnalyst;

import com.hysteryale.model.Clazz;
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
public class Warranty {
    @Id
    @GeneratedValue(strategy =  GenerationType.SEQUENCE, generator = "warranty_sequence")
    private int id;

    @ManyToOne
    private Clazz clazz;

    @Column(name = "month_year")
    private LocalDate monthYear;
    private double warranty;

    public Warranty (Clazz clazz, LocalDate monthYear, double warranty) {
        this.clazz = clazz;
        this.monthYear = monthYear;
        this.warranty = warranty;
    }
}
