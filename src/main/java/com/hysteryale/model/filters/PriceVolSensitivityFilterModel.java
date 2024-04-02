package com.hysteryale.model.filters;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PriceVolSensitivityFilterModel {
    private FilterModel dataFilter;
    private double discountPercent;
    private boolean withMarginVolumeRecovery;
}