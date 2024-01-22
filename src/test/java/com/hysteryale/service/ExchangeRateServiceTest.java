package com.hysteryale.service;

import com.hysteryale.model.Currency;
import com.hysteryale.model.ExchangeRate;
import com.hysteryale.repository.ExchangeRateRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.Month;

@SpringBootTest
public class ExchangeRateServiceTest {
    @Resource
    ExchangeRateService exchangeRateService;
    @Resource
    ExchangeRateRepository exchangeRateRepository;

    @Test
    public void testGetExchangeRate() {
        String from = "VND";
        String to = "USD";
        LocalDate monthYear = LocalDate.of(2050, Month.DECEMBER, 21);
        exchangeRateRepository.save(new ExchangeRate(new Currency(from), new Currency(to), 1.01, monthYear));

        ExchangeRate dbExchangeRate = exchangeRateService.getExchangeRate(from, to, monthYear);
        Assertions.assertEquals(from, dbExchangeRate.getFrom().getCurrency());
        Assertions.assertEquals(to, dbExchangeRate.getTo().getCurrency());
        Assertions.assertEquals(monthYear.getMonth(), dbExchangeRate.getDate().getMonth());
        Assertions.assertEquals(monthYear.getYear(), dbExchangeRate.getDate().getYear());
    }

    @Test
    public void testGetExchangeRate_notFound() {
        ExchangeRate dbExchangeRate = exchangeRateService.getExchangeRate("abc", "def", LocalDate.now());
        Assertions.assertNull(dbExchangeRate);
    }

    @Test
    public void testGetNearestExchangeRate() {
        String from = "VND";
        String to = "USD";
        LocalDate monthYear = LocalDate.now();
        exchangeRateRepository.save(new ExchangeRate(new Currency(from), new Currency(to), 1.01, monthYear));

        ExchangeRate dbExchangeRate = exchangeRateService.getNearestExchangeRate(from, to);
        Assertions.assertEquals(from, dbExchangeRate.getFrom().getCurrency());
        Assertions.assertEquals(to, dbExchangeRate.getTo().getCurrency());
        Assertions.assertEquals(monthYear.getMonth(), dbExchangeRate.getDate().getMonth());
        Assertions.assertEquals(monthYear.getYear(), dbExchangeRate.getDate().getYear());
    }

    @Test
    public void testGetNearestExchangeRate_notFound() {
        ExchangeRate dbExchangeRate = exchangeRateService.getNearestExchangeRate("abc", "def");
        Assertions.assertNull(dbExchangeRate);
    }

}
