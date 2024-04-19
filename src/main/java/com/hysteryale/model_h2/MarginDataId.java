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
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class MarginDataId implements Serializable {
    @Column(name = "quote_number")
    private String quoteNumber;
    private Integer type;

    @Column(name = "model_code")
    private String modelCode;

    @Column(name = "part_number")
    private String partNumber;
    private String currency;
    private int userId;

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        MarginDataId castedObject = (MarginDataId) o;

        return
                quoteNumber.equals(castedObject.getQuoteNumber()) &&
                type.equals(castedObject.getType()) &&
                modelCode.equals(castedObject.getModelCode()) &&
                partNumber.equals(castedObject.getPartNumber()) &&
                currency.equals(castedObject.getCurrency()) &&
                userId == castedObject.getUserId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(quoteNumber, type, modelCode, partNumber, currency, userId);
    }
}
