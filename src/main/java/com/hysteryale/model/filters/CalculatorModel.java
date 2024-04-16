/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.model.filters;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CalculatorModel {
    private double costAdjPercentage;
    private double dnAdjPercentage;
    private double freightAdj;
    private double fxAdj;

}
