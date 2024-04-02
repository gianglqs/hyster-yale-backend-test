package com.hysteryale.model.payLoad;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PriceVolSensitivityPayLoad {

    private int id;
    private String segment;
    private String series;
    private long volume;
    private double revenue;
    private double COGS;
    private double margin;
    private double marginPercent;

    private double discountPercent;

    private double newDN;
    private int unitVolumeOffset;
    private long newVolume;
    private double newRevenue;
    private double newCOGS;
    private double newMargin;
    private double newMarginPercent;
}
