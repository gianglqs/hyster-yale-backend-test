package com.hysteryale.model.filters;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdjustmentFilterModel {
    private CalculatorModel dataCalculate;
    private FilterModel dataFilter;
}
