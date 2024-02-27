package com.hysteryale.repository.upload;

import com.hysteryale.model.upload.UpdateHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UpdateHistoryRepository extends JpaRepository<UpdateHistory,Integer > {

    @Query(value = "SELECT m.time FROM update_history m where m.model_type = :modelType ORDER BY m.time DESC LIMIT 1", nativeQuery = true)
    Optional<LocalDateTime> getLatestUpdatedTimeByModelType(String modelType);

}
