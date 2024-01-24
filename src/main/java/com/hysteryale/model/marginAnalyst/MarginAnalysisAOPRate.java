package com.hysteryale.model.marginAnalyst;

import com.hysteryale.model.Currency;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MarginAnalysisAOPRate {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "marginAnalysisAOPRateSeq")
    private int id;
    private double aopRate;
    private double costUplift;
    private double addWarranty;
    private double surcharge;
    private double duty;
    private double freight;
    private String plant;

    @ManyToOne(fetch = FetchType.EAGER)
    private Currency currency;

    private LocalDate monthYear;

    private String durationUnit;
}
