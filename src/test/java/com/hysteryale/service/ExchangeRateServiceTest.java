package com.hysteryale.service;

import com.hysteryale.model.Currency;
import com.hysteryale.model.ExchangeRate;
import com.hysteryale.repository.ExchangeRateRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Calendar;

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
        Calendar monthYear = Calendar.getInstance();
        monthYear.set(2050, Calendar.DECEMBER, 21);
        exchangeRateRepository.save(new ExchangeRate(new Currency(from), new Currency(to), 1.01, monthYear));

        ExchangeRate dbExchangeRate = exchangeRateService.getExchangeRate(from, to, monthYear);
        Assertions.assertEquals(from, dbExchangeRate.getFrom().getCurrency());
        Assertions.assertEquals(to, dbExchangeRate.getTo().getCurrency());
        Assertions.assertEquals(monthYear.get(Calendar.MONTH), dbExchangeRate.getDate().get(Calendar.MONTH));
        Assertions.assertEquals(monthYear.get(Calendar.YEAR), dbExchangeRate.getDate().get(Calendar.YEAR));
    }

    @Test
    public void testGetExchangeRate_notFound() {
        ExchangeRate dbExchangeRate = exchangeRateService.getExchangeRate("abc", "def", Calendar.getInstance());
        Assertions.assertNull(dbExchangeRate);
    }

    @Test
    public void testGetNearestExchangeRate() {
        String from = "VND";
        String to = "USD";
        Calendar monthYear = Calendar.getInstance();
        exchangeRateRepository.save(new ExchangeRate(new Currency(from), new Currency(to), 1.01, monthYear));

        ExchangeRate dbExchangeRate = exchangeRateService.getNearestExchangeRate(from, to);
        Assertions.assertEquals(from, dbExchangeRate.getFrom().getCurrency());
        Assertions.assertEquals(to, dbExchangeRate.getTo().getCurrency());
        Assertions.assertEquals(monthYear.get(Calendar.MONTH), dbExchangeRate.getDate().get(Calendar.MONTH));
        Assertions.assertEquals(monthYear.get(Calendar.YEAR), dbExchangeRate.getDate().get(Calendar.YEAR));
    }

    @Test
    public void testGetNearestExchangeRate_notFound() {
        ExchangeRate dbExchangeRate = exchangeRateService.getNearestExchangeRate("abc", "def");
        Assertions.assertNull(dbExchangeRate);
    }

}
