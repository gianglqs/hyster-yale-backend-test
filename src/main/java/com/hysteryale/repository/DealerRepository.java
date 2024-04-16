/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.repository;

import com.hysteryale.model.dealer.Dealer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DealerRepository extends JpaRepository<Dealer, Integer> {

    @Query("SELECT DISTINCT name FROM Dealer ")
    List<String> getAllDealerName();

    @Query("SELECT d FROM Dealer d WHERE d.name = :name")
    Optional<Dealer> findByName(String name);

    @Query("SELECT d FROM Dealer d WHERE LOWER(d.name) LIKE CONCAT('%', LOWER(:dealerName), '%') ORDER BY d.name ASC")
    Page<Dealer> getDealerListingByFilter(@Param(value = "dealerName") String dealerName, Pageable pageable);

    @Query(value = "SELECT * FROM Dealer d " +
            "WHERE LOWER(d.name) LIKE CONCAT('%', LOWER(:dealerName), '%') " +
            "ORDER BY similarity(LOWER(d.name), LOWER(:dealerName)) LIMIT 1", nativeQuery = true)
    Optional<Dealer> getDealerByDealerName(@Param(value = "dealerName") String dealerName);

    @Query("SELECT d FROM Dealer d ORDER BY d.name")
    List<Dealer> getAllDealers();

}
