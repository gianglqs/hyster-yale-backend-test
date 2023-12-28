package com.hysteryale.repository.marginAnalyst;

import com.hysteryale.model.marginAnalyst.Freight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Calendar;
import java.util.Optional;

public interface FreightRepository extends JpaRepository<Freight, Integer> {

    @Query("SELECT f FROM Freight f WHERE f.metaSeries = ?1 AND f.monthYear = ?2")
    Optional<Freight> getFreight(String metaSeries, Calendar monthYear);

}
