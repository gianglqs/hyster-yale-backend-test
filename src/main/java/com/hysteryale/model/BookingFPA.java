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
@Table(name = "booking_fpa")
public class BookingFPA extends BaseModel {

    @Id
    @Column(name = "order_no")
    private String orderNo;

    @Column(name = "total_cost")
    private double totalCost;

    @Column(name = "dealer_net")
    private double dealerNet;

    private double margin;

    @Column(name = "margin_percentage")
    private double marginPercentage;

    private long quantity = 1;

    private String type;

    private LocalDate date;

}
