/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.model;

import com.hysteryale.model.embedId.GDPId;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name="gdp_country")
public class GDPCountry {

    @EmbeddedId
    private GDPId GDPId;

    @Column(name = "gdp")
    private double GDP;

    @Column(name = "per_capita")
    private double perCapita;

    @Column(name = "growth")
    private double growth;

    @Column(name = "share_of_world")
    private double shareOfWorld;
}
