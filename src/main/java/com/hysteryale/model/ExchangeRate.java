package com.hysteryale.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "exchange_rate")
public class ExchangeRate {
    @Id
    @SequenceGenerator(name = "exchangeRateSeq")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "exchangeRateSeq")
    private int id;
    @ManyToOne(fetch = FetchType.EAGER)
    private Currency from;
    @ManyToOne(fetch = FetchType.EAGER)
    private Currency to;
    private Double rate;
    private LocalDate date;

    public ExchangeRate (Currency fromCurrency, Currency toCurrency, Double rate, LocalDate date) {
        this.from = fromCurrency;
        this.to = toCurrency;
        this.rate = rate;
        this.date = date;
    }

    public ExchangeRate(Currency from, Currency to) {
        this.from = from;
        this.to = to;
    }
}
