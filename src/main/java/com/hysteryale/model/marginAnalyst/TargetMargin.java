package com.hysteryale.model.marginAnalyst;

import com.hysteryale.model.Region;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

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

    @ManyToOne
    private Region region;

    @Column(name = "meta_series")
    private String metaSeries;

    @Column(name = "month_year")
    private LocalDate monthYear;

    @Column(name = "std_margin_percentage")
    private double stdMarginPercentage;

    public TargetMargin (Region region, String metaSeries, LocalDate monthYear, double stdMarginPercentage) {
        this.region = region;
        this.metaSeries = metaSeries;
        this.monthYear = monthYear;
        this.stdMarginPercentage = stdMarginPercentage;
    }
}
