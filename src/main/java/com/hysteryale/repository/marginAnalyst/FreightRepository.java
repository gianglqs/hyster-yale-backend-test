/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.repository.marginAnalyst;

import com.hysteryale.model.marginAnalyst.Freight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

public interface FreightRepository extends JpaRepository<Freight, Integer> {

    @Query("SELECT f FROM Freight f WHERE f.metaSeries = ?1 AND f.monthYear = ?2")
    Optional<Freight> getFreight(String metaSeries, LocalDate monthYear);

    @Query("SELECT f FROM Freight f " +
            "WHERE f.metaSeries = ?1 " +
            "AND f.monthYear = (SELECT MAX(f2.monthYear) FROM Freight f2)")
    Optional<Freight> getLatestFreight(String metaSeries);

}
