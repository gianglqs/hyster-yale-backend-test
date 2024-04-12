/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.model;

import lombok.*;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Currency {
    @Id
    private String currency;
    @Column(name = "currency_name")
    private String currencyName;

    public Currency(String currency){
        this.currency =currency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Currency currency1 = (Currency) o;
        return Objects.equals(currency, currency1.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currency);
    }
}
