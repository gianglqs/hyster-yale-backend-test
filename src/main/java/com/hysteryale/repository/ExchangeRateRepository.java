package com.hysteryale.repository;

import com.hysteryale.model.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Integer> {
    @Query("SELECT e FROM ExchangeRate e WHERE e.from.currency = ?1 AND e.to.currency = ?2 AND e.date = ?3")
    Optional<ExchangeRate> getExchangeRateByFromToCurrencyAndDate(String fromId, String toId, LocalDate date);

    @Query(value = "SELECT * FROM Exchange_Rate WHERE from_currency = ?1 AND to_currency = ?2 ORDER BY date DESC LIMIT 1", nativeQuery = true)
    Optional<ExchangeRate> getNearestExchangeRateByFromToCurrency(String fromId, String toId);

    @Query(value = "SELECT * from exchange_rate e " +
            "WHERE e.from_currency = :from_currency " +
            "AND e.to_currency = :to_currency " +
            "AND (cast(:to_date as date) IS NULL OR e.date <= (:to_date)) " +
            "AND (cast(:from_date as date) IS NULL OR e.date >= (:from_date)) " +
            "ORDER BY e.date DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<ExchangeRate> getCurrentExchangeRate(@Param("from_currency") String fromCurrency, @Param("to_currency") String toCurrency,
                                              @Param("from_date") LocalDate fromDate, @Param("to_date") LocalDate toDate, @Param("limit") int limit);

    @Query(value = "SELECT m.latest_modified_at FROM booking m WHERE m.latest_modified_at is not null ORDER BY m.latest_modified_at DESC LIMIT 1", nativeQuery = true)
    Optional<LocalDateTime> getLatestUpdatedTime();

    @Query("SELECT e FROM ExchangeRate e WHERE e.to.currency = 'USD' ")
    List<ExchangeRate> getExchangeRateToUSD();
}
