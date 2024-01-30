package com.hysteryale.repository;

import com.hysteryale.model.AOPMargin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface AOPMarginRepository extends JpaRepository<AOPMargin, String> {
    @Query("SELECT DISTINCT aopMargin FROM AOPMargin aopMargin WHERE aopMargin.year= :year")
    Set<AOPMargin> findByYear(@Param("year") int year);

    @Query("SELECT DISTINCT a FROM AOPMargin a WHERE a.series = :series")
    List<AOPMargin> findByMetaSeries(String series);

//    @Query("SELECT DISTINCT a FROM AOPMargin a WHERE a.regionSeriesPlant = :regionSeriesPlant")
//    Optional<AOPMargin> findByRegionSeriesPlant(String regionSeriesPlant);

    @Query("SELECT DISTINCT a FROM AOPMargin a WHERE a.region = :region AND a.plant = :plant AND a.series = :series")
    List<AOPMargin> findByRegionPlantSeries(String series, String region, String plant);

}
