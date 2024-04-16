/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.model.competitor;

import com.hysteryale.model.Region;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ForeCastValue {
    private Region region;
    private int year;
    private String metaSeries;
    private int quantity;
    private String plant;

}
