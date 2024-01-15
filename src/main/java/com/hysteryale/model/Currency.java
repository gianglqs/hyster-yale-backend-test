package com.hysteryale.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

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
}
