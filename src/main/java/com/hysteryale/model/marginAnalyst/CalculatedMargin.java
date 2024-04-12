/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.model.marginAnalyst;

import com.hysteryale.model_h2.IMMarginAnalystData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CalculatedMargin {
    private IMMarginAnalystData marginData;
    private String region;
}
