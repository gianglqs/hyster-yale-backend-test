package com.hysteryale.repository.marginAnalyst;

import com.hysteryale.model.marginAnalyst.Warranty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

public interface WarrantyRepository  extends JpaRepository<Warranty, Integer> {
    @Query("SELECT w FROM Warranty w WHERE w.clazz = ?1 AND w.monthYear = ?2")
    Optional<Warranty> getWarranty(String clazz, LocalDate monthYear);

    @Query("SELECT w FROM Warranty w " +
            "WHERE w.clazz = ?1 " +
            "AND w.monthYear = (SELECT MAX (w2.monthYear) FROM Warranty w2)")
    Optional<Warranty> getLatestWarranty(String clazz);

}
