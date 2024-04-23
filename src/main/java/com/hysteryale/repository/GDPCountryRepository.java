/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.repository;

import com.hysteryale.model.GDPCountry;
import com.hysteryale.model.embedId.GDPId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.domain.Pageable;
import java.util.List;

public interface GDPCountryRepository extends JpaRepository<GDPCountry, Integer> {

    @Query("SELECT g FROM GDPCountry g WHERE g.GDPId.years = :year ORDER BY g.GDPId.country.countryName ")
    List<GDPCountry> findByYear(int year, Pageable pageable);

    @Query("SELECT DISTINCT(g.GDPId.years) FROM GDPCountry g ORDER BY g.GDPId.years DESC")
    List<Integer> getAllYear();

    @Query("SELECT g FROM GDPCountry g WHERE g.GDPId.years = :year ORDER BY g.GDP DESC")
    List<GDPCountry> findByYearAndSort(int year);

    @Query("SELECT COUNT(g) FROM GDPCountry g WHERE g.GDPId.years = :year")
    long countByYear(int year);

}
