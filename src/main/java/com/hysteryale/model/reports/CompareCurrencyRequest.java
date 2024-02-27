package com.hysteryale.model.reports;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CompareCurrencyRequest {
    private String currentCurrency;
    private List<String> comparisonCurrencies;
    private boolean fromRealTime;
}
