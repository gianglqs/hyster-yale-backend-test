/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.service;

import com.hysteryale.model.Country;
import com.hysteryale.model.Region;
import com.hysteryale.repository.CountryRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@SpringBootTest
public class CountryServiceTest {
    @Resource
    CountryService countryService;
    @Resource
    CountryRepository countryRepository;

    @Test
    public void testGetCountryByName() {
        String countryName = "New Country";

        countryRepository.save(new Country(countryName, new Region(1, "Asia", "A")));
        Optional<Country> dbCountry = countryService.getCountryByName(countryName);

        Assertions.assertTrue(dbCountry.isPresent());
    }

    @Test
    public void testGetCountryByName_notFound() {
        Optional<Country> dbCountry = countryService.getCountryByName("17263hjsdfsjdhfbv");
        Assertions.assertTrue(dbCountry.isEmpty());
    }

    @Test
    public void testAddCountry() {
        String countryName = "New Country 1234";

        countryService.addCountry(new Country(countryName, new Region(1, "Asia", "A")));
        Optional<Country> dbCountry = countryRepository.getCountryByName(countryName);

        Assertions.assertTrue(dbCountry.isPresent());
        Assertions.assertEquals(countryName, dbCountry.get().getCountryName());
    }

    @Test
    public void testGetListCountryNameByRegion() {
        List<String> countryList = countryService.getListCountryNameByRegion("Asia");
        Assertions.assertNotNull(countryList);
    }
}
