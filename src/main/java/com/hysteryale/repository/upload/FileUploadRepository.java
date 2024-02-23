package com.hysteryale.repository.upload;

import com.hysteryale.model.upload.FileUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface FileUploadRepository extends JpaRepository<FileUpload, Integer> {
    @Query("SELECT m.fileName FROM FileUpload m WHERE m.uuid = ?1")
    String getFileNameByUUID(String uuid);

    @Query(value = "SELECT m.uploaded_time FROM file_upload m where m.success is true AND m.model_type = :modelType ORDER BY m.uploaded_time DESC LIMIT 1", nativeQuery = true)
    Optional<LocalDateTime> getLatestUpdatedTimeByModelType(String modelType);

    @Query("SELECT f FROM FileUpload f WHERE f.fileName = :fileName")
    Optional<FileUpload> getFileUploadByFileName(String fileName);


}
