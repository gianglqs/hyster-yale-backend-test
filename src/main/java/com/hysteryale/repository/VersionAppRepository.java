/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.repository;


import com.hysteryale.model.versionTag.VersionApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface VersionAppRepository extends JpaRepository<VersionApp, Integer> {


    @Query(value = "SELECT * FROM version_app v WHERE v.type = :type ORDER BY v.id DESC LIMIT 1", nativeQuery = true)
    Optional<VersionApp> getLatestVersion(String type);
}
