package com.hysteryale.repository;

import com.hysteryale.model.AOPMargin;
import com.hysteryale.model.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface AOPMarginRepository extends JpaRepository<AOPMargin, String> {

    @Query("SELECT DISTINCT a FROM AOPMargin a WHERE a.region = :region AND a.plant = :plant AND a.metaSeries = :metaSeries AND a.year = :year")
    Optional<AOPMargin> findByRegionSeriesPlantAndYear(Region region, String metaSeries, String plant, int year);


}
