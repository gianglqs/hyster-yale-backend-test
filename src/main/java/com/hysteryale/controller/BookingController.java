/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.controller;

import com.hysteryale.exception.InvalidFileFormatException;
import com.hysteryale.exception.InvalidFileNameException;
import com.hysteryale.model.enums.FrequencyImport;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.model.importFailure.ImportFailure;
import com.hysteryale.repository.upload.FileUploadRepository;
import com.hysteryale.response.ResponseObject;
import com.hysteryale.service.BookingService;
import com.hysteryale.service.FileUploadService;
import com.hysteryale.service.ImportTrackingService;
import com.hysteryale.utils.EnvironmentUtils;
import com.hysteryale.utils.FileUtils;
import com.hysteryale.utils.LocaleUtils;
import com.hysteryale.model.enums.ModelTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class BookingController {

    @Resource
    BookingService bookingService;

    @Resource
    FileUploadService fileUploadService;

    @Resource
    FileUploadRepository fileUploadRepository;

    @Resource
    ImportTrackingService importTrackingService;

    @Resource
    LocaleUtils localeUtils;

    /**
     * Get BookingOrders based on filters and pagination
     *
     * @param filters from FE
     * @param pageNo  current page
     * @param perPage number of items per page
     */

    @PostMapping(path = "/bookingOrders", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getDataBooking(@RequestBody FilterModel filters,
                                              @RequestParam(defaultValue = "1") int pageNo,
                                              @RequestParam(defaultValue = "100") int perPage) throws java.text.ParseException {
        filters.setPageNo(pageNo);
        filters.setPerPage(perPage);

        return bookingService.getBookingByFilter(filters);

    }

    @PostMapping(path = "/importNewBooking", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Object> importNewDataBooking(@RequestParam("file") MultipartFile file, @RequestHeader("locale") String locale, Authentication authentication) throws Exception {
        String baseFolder = EnvironmentUtils.getEnvironmentValue("public-folder");
        String baseFolderUploaded = EnvironmentUtils.getEnvironmentValue("upload_files.base-folder");
        String targetFolder = EnvironmentUtils.getEnvironmentValue("upload_files.booked");

        String modelType = ModelTypeEnum.BOOKING.getValue();
        if (FileUtils.checkFileNameValid(file, "cost_data")) {
            modelType = ModelTypeEnum.COST_DATA.getValue();
        }
        //save file on disk
        String excelFileExtension = FileUtils.EXCEL_FILE_EXTENSION;
        String savedFileName = fileUploadService.saveFileUploaded(file, authentication, targetFolder, excelFileExtension, modelType);
        String filePath = baseFolder + baseFolderUploaded + targetFolder + savedFileName;

        String fileUUID = fileUploadRepository.getFileUUIDByFileName(savedFileName);

        if (!FileUtils.isExcelFile(filePath)) {
            throw new InvalidFileFormatException(file.getOriginalFilename(), fileUUID);
        }

        List<ImportFailure> importFailures = null;

        if (FileUtils.checkFileNameValid(file, "booked") || FileUtils.checkFileNameValid(file, "booking")) {
            importFailures = bookingService.importNewBookingFileByFile(filePath, fileUUID);
            fileUploadService.handleUpdatedSuccessfully(savedFileName);
        } else if (FileUtils.checkFileNameValid(file, "cost_data")) {
            importFailures = bookingService.importCostDataNew(filePath, fileUUID);
            fileUploadService.handleUpdatedSuccessfully(savedFileName);
        } else {
            throw new InvalidFileNameException(file.getOriginalFilename(), fileUUID);
        }
        // update ImportTracking
        importTrackingService.updateImport(fileUUID, file.getOriginalFilename(), FrequencyImport.MONTHLY);
        String message = localeUtils.getMessageImportComplete(importFailures, modelType, locale);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject(message, fileUUID));

    }


}
