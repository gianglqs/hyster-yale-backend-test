package com.hysteryale.model;

import ch.qos.logback.classic.db.names.ColumnName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.units.qual.C;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "interest_rate")
public class InterestRate {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private int id;

    @Column(name="bank_name")
    private String bankName;

    @Column(name="country")
    private String country;

    @Column(name="current_rate")
    double currentRate;

    @Column(name="previous_rate")
    double previousRate;

    @Temporal(TemporalType.DATE)
    @Column(name="update_date")
    Date updateDate;

}
