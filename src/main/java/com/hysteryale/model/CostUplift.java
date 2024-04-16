/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.model;

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
@Table(name = "cost_uplift")
public class CostUplift {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "costUpliftSeq")
    private int id;
    private String plant;

    @Column(name = "cost_uplift")
    private double costUplift;
    private LocalDate date;

}
