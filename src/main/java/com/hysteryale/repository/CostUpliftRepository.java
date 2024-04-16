/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.repository;

import com.hysteryale.model.CostUplift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

public interface CostUpliftRepository extends JpaRepository<CostUplift, Integer> {
    @Query("SELECT c FROM CostUplift c WHERE c.plant = ?1 AND c.date = ?2")
    Optional<CostUplift> getCostUpliftByPlantAndDate(String plant, LocalDate date);
}
