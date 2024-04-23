/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.model.payLoad;

import com.hysteryale.model.GDPCountry;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BubbleChartGDPCountryPageLoad {
    private GDPCountry gdpCountry;
    private String color;
    private String rank;

}
