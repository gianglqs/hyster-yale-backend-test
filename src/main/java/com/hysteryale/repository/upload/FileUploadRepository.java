package com.hysteryale.repository.upload;

import com.hysteryale.model.upload.FileUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface FileUploadRepository extends JpaRepository<FileUpload, Integer> {
    @Query("SELECT m.fileName FROM FileUpload m WHERE m.uuid = ?1")
    String getFileNameByUUID(String uuid);

    @Query("SELECT m.uploadedTime FROM FileUpload m where m.modelType = :model")
    LocalDateTime getLatestUpdatedTimeByModelType(String modelType);
}
