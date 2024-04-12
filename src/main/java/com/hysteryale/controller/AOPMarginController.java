/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.controller;

import com.hysteryale.exception.InvalidFileFormatException;
import com.hysteryale.model.enums.FrequencyImport;
import com.hysteryale.model.enums.ModelTypeEnum;
import com.hysteryale.model.importFailure.ImportFailure;
import com.hysteryale.repository.importFailure.ImportFailureRepository;
import com.hysteryale.repository.upload.FileUploadRepository;
import com.hysteryale.response.ResponseObject;
import com.hysteryale.service.AOPMarginService;
import com.hysteryale.service.FileUploadService;
import com.hysteryale.service.ImportTrackingService;
import com.hysteryale.utils.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

@RestController()
@RequestMapping("aopmargin")
public class AOPMarginController {

    @Resource
    private AOPMarginService aopMarginService;

    @Resource
    private FileUploadService fileUploadService;

    @Resource
    private FileUploadRepository fileUploadRepository;

    @Resource
    private ImportFailureRepository importFailureRepository;

    @Resource
    LocaleUtils localeUtils;

    @Resource
    ImportTrackingService importTrackingService;

    @PostMapping(path = "/importData", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ResponseObject> importAOPMargin(@RequestParam("file") MultipartFile file, @RequestHeader("locale") String locale, Authentication authentication) throws Exception {

        String baseFolder = EnvironmentUtils.getEnvironmentValue("public-folder");
        String baseFolderUploaded = EnvironmentUtils.getEnvironmentValue("upload_files.base-folder");
        String targetFolder = EnvironmentUtils.getEnvironmentValue("upload_files.product");
        String fileNameEncoded = fileUploadService.saveFileUploaded(file, authentication, targetFolder, FileUtils.EXCEL_FILE_EXTENSION, ModelTypeEnum.AOP_MARGIN.getValue());
        String fileUUID = fileUploadRepository.getFileUUIDByFileName(fileNameEncoded);
        String filePath = baseFolder + baseFolderUploaded + targetFolder + fileNameEncoded;

        if (!FileUtils.isExcelFile(filePath)) {
            throw new InvalidFileFormatException(file.getOriginalFilename(), fileUUID);
        }


        String fileName = file.getOriginalFilename();
        int year = DateUtils.extractYear(fileName, fileUUID);
        InputStream is = new FileInputStream(filePath);
        List<ImportFailure> importFailures = aopMarginService.importAOPMarginFromGUM(is, year, fileUUID);

        importFailureRepository.saveAll(importFailures);

        // update ImportTracking
        importTrackingService.updateImport(fileUUID, file.getOriginalFilename(), FrequencyImport.ANNUAL);
        String message = localeUtils.getMessageImportComplete(importFailures, ModelTypeEnum.AOP_MARGIN.getValue(),  locale);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject(message, fileUUID));
    }
}
