package com.hysteryale.repository.marginAnalyst;

import com.hysteryale.model.marginAnalyst.TargetMargin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface TargetMarginRepository extends JpaRepository<TargetMargin, Integer> {

    @Query(value = "SELECT t.id, t.meta_series, t.month_year, t.region_id, t.std_margin_percentage " +
            "FROM target_margin t join region r on t.region_id = r.id " +
            "WHERE r.region_name = :region " +
            "AND t.meta_series = :meta_series " +
            "AND t.month_year = :month_year " +
            "LIMIT 1", nativeQuery = true)
    Optional<TargetMargin> getTargetMargin(@Param("region") String region, @Param("meta_series") String metaSeries, @Param("month_year") LocalDate monthYear);

}
