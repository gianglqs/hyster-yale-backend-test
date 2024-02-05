package com.hysteryale.model.reports;

import com.hysteryale.model.ExchangeRate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CompareCurrencyResponse {
    private List<ExchangeRate> exchangeRateList;
    private double differentRate;
    private double differentRatePercentage;
}
