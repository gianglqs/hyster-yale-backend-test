/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TrendData {
    private int month;
    private double marginPercentage;
    private double cost;
    private String monthYear;

    public TrendData (int month, double marginPercentage, double cost) {
        this.month = month;
        this.marginPercentage = marginPercentage;
        this.cost = cost;
    }
}
