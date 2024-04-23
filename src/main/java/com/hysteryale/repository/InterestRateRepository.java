package com.hysteryale.repository;

import com.hysteryale.model.Booking;
import com.hysteryale.model.InterestRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InterestRateRepository extends JpaRepository<InterestRate, Integer> {


    @Query("SELECT c FROM InterestRate c WHERE c.country = ?1" )
    Optional<InterestRate> getInterestRateByCountry(String country);


    @Query("SELECT c FROM InterestRate c WHERE c.bankName LIKE CONCAT (?1,'%')")
    List<InterestRate> getInterestRateByBankName(String bankName);

    @Query("SELECT DISTINCT bankName FROM InterestRate ")
    List<String> getAllBankName();

    @Query(value = "select ir.id, ir.bank_name, ir.country, ir.current_rate, ir. previous_rate, ir.update_date, c.region_id, r.region_name, c.code  from interest_rate ir, country c , region r \n" +
            "\twhere ir.bank_name like concat('%',:bankName,'%') and ir.country =c.country_name  and c.region_id =r.id", nativeQuery = true)
    List<Object[]> selectAllForInterestRate(@Param("bankName") String bankName);


    @Query(value = "select ir.id, ir.bank_name, ir.country, ir.current_rate, ir. previous_rate, ir.update_date, c.region_id, r.region_name, c.code  from interest_rate ir, country c , region r \n" +
            "\twhere ir.bank_name like concat('%',:bankName,'%') and ir.country =c.country_name  and c.region_id =r.id and r.region_name IN (:regions)", nativeQuery = true)
    List<Object[]> selectAllForInterestRateByFilter(@Param("bankName") String bankName,@Param("regions") List<String> regions);

}


