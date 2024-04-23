/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.repository;

import com.hysteryale.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CountryRepository extends JpaRepository<Country, Integer> {
    @Query("SELECT c FROM Country c WHERE c.countryName = ?1")
    Optional<Country> getCountryByName(String countryName);

    @Query("SELECT c.countryName FROM Country c WHERE c.region.regionName = ?1")
    List<String> getCountryNameByRegion(String region);

    @Query("SELECT c.countryName FROM Country c WHERE c.countryName != '' AND c.countryName IS NOT NULL")
    List<String> getAllCountryNames();

    @Query("SELECT c FROM Country c WHERE c.hasDealer = true")
    List<Country> findCountryHasDealer();
}
