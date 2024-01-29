package com.hysteryale.repository;

import com.hysteryale.model.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, String> {

    @Query("SELECT r.regionName FROM Region r ")
    List<String> findAllRegion();

    @Query("SELECT r FROM Region r WHERE r.regionName = ?1")
    Region getRegionByName(String strRegion);

    @Query("SELECT r FROM Region r WHERE r.regionShortName = ?1")
    Region getRegionByShortName(String shortName);
}
