package com.hysteryale.repository;

import com.hysteryale.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CurrencyRepository extends JpaRepository<Currency, Integer> {
    @Query("SELECT c FROM Currency c WHERE c.currencyName = ?1 OR c.currency = ?1")
    Optional<Currency> getCurrenciesByName(String currencyName);

    @Query("SELECT c FROM Currency c WHERE  c.currency = ?1")
    Optional<Currency> findById(String currencyName);

    @Query("SELECT DISTINCT (e.from.currency) FROM ExchangeRate e " +
            "WHERE e.date = (SELECT MAX(e.date) FROM ExchangeRate e) " +
            "ORDER BY e.from.currency")
    List<String> getExistingCurrencies();

}
