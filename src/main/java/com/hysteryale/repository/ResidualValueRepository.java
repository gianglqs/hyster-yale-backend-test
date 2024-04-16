/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.repository;

import com.hysteryale.model.ResidualValue;
import com.hysteryale.model.embedId.ResidualValueId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResidualValueRepository extends JpaRepository<ResidualValue, ResidualValueId> {

    @Query("SELECT r from ResidualValue r WHERE r.id.product.modelCode = :modelCode")
    List<ResidualValue> getResidualValueByModelCode(String modelCode);
}
