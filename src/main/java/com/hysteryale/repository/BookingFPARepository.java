/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.repository;

import com.hysteryale.model.BookingFPA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingFPARepository extends JpaRepository<BookingFPA, String> {


    @Query("SELECT b FROM BookingFPA b WHERE "+
            " (:orderNos) IS NULL OR b.orderNo IN (:orderNos)")
    List<BookingFPA> findByListOrderNo(List<String> orderNos);

    @Query(value = "SELECT m.latest_modified_at FROM booking_fpa m WHERE m.latest_modified_at is not null ORDER BY m.latest_modified_at DESC LIMIT 1", nativeQuery = true)
    Optional<LocalDateTime> getLatestUpdatedTime();
}
