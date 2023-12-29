package com.hysteryale.model.marginAnalyst;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Calendar;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "target_margin")
public class TargetMargin {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "target_margin_sequence")
    private int id;
    private String region;
    private String metaSeries;

    @Temporal(TemporalType.DATE)
    private Calendar monthYear;
    private double stdMarginPercentage;

    public TargetMargin (String region, String metaSeries, Calendar monthYear, double stdMarginPercentage) {
        this.region = region;
        this.metaSeries = metaSeries;
        this.monthYear = monthYear;
        this.stdMarginPercentage = stdMarginPercentage;
    }
}
