package com.hysteryale.repository;

import com.hysteryale.model.ResidualValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ResidualValueRepository extends JpaRepository<ResidualValue, Integer> {

    @Query("SELECT r from ResidualValue r WHERE r.id.product.modelCode = :modelCode")
    List<ResidualValue> getResidualValueByModelAndHours(String modelCode);
}
