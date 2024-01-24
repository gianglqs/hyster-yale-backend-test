package com.hysteryale.repository.marginAnalyst;

import com.hysteryale.model.marginAnalyst.TargetMargin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface TargetMarginRepository extends JpaRepository<TargetMargin, Integer> {

    @Query(value = "SELECT t.id, t.metaseries, t.monthyear, t.region, t.stdmarginpercentage " +
            "FROM target_margin t " +
            "WHERE t.region = :region " +
            "AND t.metaseries = :meta_series " +
            "AND t.monthyear = :month_year " +
            "LIMIT 1", nativeQuery = true)
    Optional<TargetMargin> getTargetMargin(@Param("region") String region, @Param("meta_series") String metaSeries, @Param("month_year") LocalDate monthYear);

}
