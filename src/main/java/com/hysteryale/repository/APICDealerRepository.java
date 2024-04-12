/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.repository;

import com.hysteryale.model.APICDealer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface APICDealerRepository extends JpaRepository<APICDealer, String> {
    @Query("SELECT DISTINCT a.dealerDivison FROM APICDealer a ORDER BY a.dealerDivison")
    List<String> getDealerNames();
}
