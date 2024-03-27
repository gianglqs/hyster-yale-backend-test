package com.hysteryale.repository;

import com.hysteryale.model.Dealer;
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

    @Query("SELECT d FROM Dealer d WHERE LOWER(d.name) LIKE CONCAT('%', LOWER(:dealerName), '%')")
    List<Dealer> getDealerListingByFilter(@Param(value = "dealerName") String dealerName, Pageable pageable);
}
