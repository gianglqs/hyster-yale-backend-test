package com.hysteryale.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExchangeRatesException extends Exception {
    private String unsupportedCurrency;
    public ExchangeRatesException(String name) {
        super(name);
    }

    public ExchangeRatesException(String name, String unsupportedCurrency) {
        super(name);
        this.unsupportedCurrency = unsupportedCurrency;
    }

}
