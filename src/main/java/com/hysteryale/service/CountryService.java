/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.service;

import com.hysteryale.model.Country;
import com.hysteryale.repository.CountryRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Service
public class CountryService {
    @Resource
    CountryRepository countryRepository;


    public Optional<Country> getCountryByName(String countryName) {
        return countryRepository.getCountryByName(countryName);
    }

    public Country addCountry(Country country) {
        return countryRepository.save(country);
    }

    public List<String> getListCountryNameByRegion(String region) {
        return countryRepository.getCountryNameByRegion(region);
    }

    public Country findByCountryCode(List<Country> countries, String ctryCode) {
        for (Country country : countries) {
            if (country.getCode()!= null && country.getCode().equals(ctryCode))
                return country;
        }
        return null;
    }
}
