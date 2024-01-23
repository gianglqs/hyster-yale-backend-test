package com.hysteryale.repository;

import com.hysteryale.model.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Integer> {
    @Query("SELECT e FROM ExchangeRate e WHERE e.from.currency = ?1 AND e.to.currency = ?2 AND e.date = ?3")
    Optional<ExchangeRate> getExchangeRateByFromToCurrencyAndDate(String fromId, String toId, LocalDate date);

    @Query(value = "SELECT * FROM Exchange_Rate WHERE from_currency = ?1 AND to_currency = ?2 ORDER BY date DESC LIMIT 1", nativeQuery = true)
    Optional<ExchangeRate> getNearestExchangeRateByFromToCurrency(String fromId, String toId);
}
