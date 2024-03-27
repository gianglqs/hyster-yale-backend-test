package com.hysteryale.service;

import com.hysteryale.exception.ExchangeRatesException;
import com.hysteryale.model.Currency;
import com.hysteryale.repository.CurrencyRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Resource;

@SpringBootTest
public class CurrencyServiceTest {
    @Resource
    CurrencyService currencyService;

    @Resource
    CurrencyRepository currencyRepository;

    @Test
    public void testGetCurrencyByName() throws ExchangeRatesException {
        String currencyName = "abc";
        currencyRepository.save(new Currency("abc", "ABC"));

        Currency dbCurrency = currencyService.getCurrenciesByName(currencyName);
        Assertions.assertEquals(currencyName, dbCurrency.getCurrency());
    }

    @Test
    public void testGetCurrencyByName_notFound() {
        String currencyName = "asdfnbbf";
        ResponseStatusException exception =
                Assertions.assertThrows(ResponseStatusException.class, () -> currencyService.getCurrenciesByName(currencyName));

        Assertions.assertEquals(404, exception.getStatus().value());
        Assertions.assertEquals("No currencies found with " + currencyName, exception.getReason());
    }

    @Test
    public void testGetCurrencies() {
        String currencyName = "abc";
        currencyRepository.save(new Currency("abc", "ABC"));

        Currency dbCurrency = currencyService.getCurrencies(currencyName);
        Assertions.assertEquals(currencyName, dbCurrency.getCurrency());
    }

    @Test
    public void testGetCurrencies_notFound() {
        String currencyName = "asdfnbbf";
        Currency dbCurrency = currencyService.getCurrencies(currencyName);
        Assertions.assertNull(dbCurrency);
    }
}
