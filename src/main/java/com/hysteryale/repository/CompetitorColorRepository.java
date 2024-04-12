/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.repository;

import com.hysteryale.model.competitor.CompetitorColor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CompetitorColorRepository extends JpaRepository<CompetitorColor, Integer> {

    @Query("SELECT c FROM CompetitorColor c WHERE c.groupName = ?1")
    Optional<CompetitorColor> getCompetitorColor(String groupName);

    @Query("SELECT c FROM CompetitorColor c WHERE c.groupName LIKE CONCAT ('%', ?1, '%')")
    Page<CompetitorColor> searchCompetitorColor(String search, Pageable pageable);

    @Query(value = "SELECT m.latest_modified_at FROM booking m WHERE m.latest_modified_at is not null ORDER BY m.latest_modified_at DESC LIMIT 1", nativeQuery = true)
    Optional<LocalDateTime> getLatestUpdatedTime();
}
