package com.hysteryale.repositories;

import com.hysteryale.model.Currency;
import com.hysteryale.repository.CurrencyRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import javax.annotation.Resource;
import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@DataJpaTest
public class CurrencyRepositoryTest {

    @Resource
    CurrencyRepository currencyRepository;

    @Test
    void getCurrenciesByName(){
        Optional<Currency> getExistCurrencyByName = currencyRepository.getCurrenciesByName("USD");
        Optional<Currency> getNotExistCurrencyByName = currencyRepository.getCurrenciesByName("SSS");

        Assertions.assertTrue(getExistCurrencyByName.isPresent());
        Assertions.assertFalse(getNotExistCurrencyByName.isPresent());
    }
}
