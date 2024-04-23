/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.model_h2;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class MarginSummaryId implements Serializable {
    @Column(name = "quote_number")
    private String quoteNumber;
    private Integer type;

    @Column(name = "model_code")
    private String modelCode;
    private String series;
    private String currency;

    @Column(name = "user_id")
    private int userId;

    @Column(name = "duration_unit")
    private String durationUnit;
    private String region;

    public MarginSummaryId (String quoteNumber, int type, String modelCode, String series, String currency, String region) {
        this.quoteNumber = quoteNumber;
        this.type = type;
        this.modelCode = modelCode;
        this.series = series;
        this.currency = currency;
        this.region = region;
    }

    public MarginSummaryId (String quoteNumber, int type, String modelCode, String series, String currency, int userId, String region) {
        this.quoteNumber = quoteNumber;
        this.type = type;
        this.modelCode = modelCode;
        this.series = series;
        this.currency = currency;
        this.userId = userId;
        this.region = region;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        MarginSummaryId castedObject = (MarginSummaryId) o;

        return quoteNumber.equals(castedObject.getQuoteNumber()) &&
                type.equals(castedObject.getType()) &&
                modelCode.equals(castedObject.getModelCode()) &&
                series.equals(castedObject.getSeries()) &&
                currency.equals(castedObject.getCurrency()) &&
                userId == castedObject.getUserId() &&
                durationUnit.equals(castedObject.getDurationUnit()) &&
                region.equals(castedObject.getRegion());
    }

    @Override
    public int hashCode() {
        return Objects.hash(quoteNumber, type, modelCode, series, currency, userId, durationUnit, region);
    }
}
