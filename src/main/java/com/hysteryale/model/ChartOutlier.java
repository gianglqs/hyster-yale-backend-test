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
public class ChartOutlier {
    private String region;
    private double dealerNet;
    private double marginPercentageAfterSurcharge;
    private String modelCode;
}
