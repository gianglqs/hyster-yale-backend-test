/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.repository.upload;

import com.hysteryale.model.upload.FileUpload;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FileUploadRepository extends JpaRepository<FileUpload, Integer> {
    @Query("SELECT m.fileName FROM FileUpload m WHERE m.uuid = ?1")
    String getFileNameByUUID(String uuid);

    @Query(value = "SELECT m.uploaded_time FROM file_upload m left join model_type t on m.model_type = t.id where m.success is true AND t.type = :modelType ORDER BY m.uploaded_time DESC LIMIT 1", nativeQuery = true)
    Optional<LocalDateTime> getLatestUpdatedTimeByModelType(String modelType);

    @Query("SELECT f FROM FileUpload f WHERE f.fileName = :fileName")
    Optional<FileUpload> getFileUploadByFileName(String fileName);


    @Query("SELECT f FROM FileUpload f WHERE " +
            " f.success is not null " +
            " AND (:filter IS NULL OR lower(convert_from(decode(split_part(f.fileName, '_', 1), 'base64'),'UTF8')) like lower(concat('%', :filter, '%')))" +
            " ORDER BY f.uploadedTime DESC ")
    List<FileUpload> getFileUploadByFilter(@Param("filter") String filter, Pageable pageable);


    @Query("SELECT count(f) FROM FileUpload f WHERE " +
            " f.success is not null " +
            " AND (:filter IS NULL OR lower(convert_from(decode(split_part(f.fileName, '_', 1), 'base64'),'UTF8')) like lower(concat('%', :filter, '%')))")
    int countAll(String filter);


    @Query("SELECT f.uuid FROM FileUpload f WHERE f.fileName = :fileName")
    String getUUIDByName(String fileName);

    @Query("SELECT f.uuid FROM FileUpload f WHERE f.fileName = :savedFileName")
    String getFileUUIDByFileName(String savedFileName);

    @Query("SELECT f FROM FileUpload f WHERE f.uuid = :fileUUID")
    Optional<FileUpload> getFileUploadByUUID(String fileUUID);
}
