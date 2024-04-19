package com.hysteryale.repository;

import com.hysteryale.model.Booking;
import com.hysteryale.model.InterestRate;
import com.hysteryale.model.competitor.CompetitorPricing;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InterestRateRepository extends JpaRepository<InterestRate, Integer> {


    @Query("SELECT c FROM InterestRate c WHERE c.country = ?1" )
    Optional<InterestRate> getInterestRateByCountry(String country);


    @Query("SELECT c FROM InterestRate c WHERE c.bankName LIKE CONCAT (?1,'%')")
    List<InterestRate> getInterestRateByBankName(String bankName);

    @Query("SELECT c FROM InterestRate c WHERE " +
            "c.bankName LIKE CONCAT('%', :search, '%')")
    List<InterestRate> selectAllForInterestRate(@Param("search") String bankName);
//            @Param("region") List<String> regions


}


