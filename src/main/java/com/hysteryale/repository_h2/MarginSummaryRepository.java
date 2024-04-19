/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.repository_h2;

import com.hysteryale.model_h2.MarginSummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarginSummaryRepository extends JpaRepository<MarginSummary, Integer> {

}
