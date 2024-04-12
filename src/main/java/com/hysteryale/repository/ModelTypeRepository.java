/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.repository;

import com.hysteryale.model.ModelType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ModelTypeRepository extends JpaRepository<ModelType, Integer> {

    Optional<ModelType> findByType(String type);

    @Query("SELECT m.type FROM ModelType m ")
    List<String> getAllType();


}
