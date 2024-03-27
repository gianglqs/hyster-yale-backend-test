package com.hysteryale.controller;

import com.hysteryale.exception.InvalidFileFormatException;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.model.importFailure.ImportFailure;
import com.hysteryale.repository.upload.FileUploadRepository;
import com.hysteryale.response.ResponseObject;
import com.hysteryale.service.FileUploadService;
import com.hysteryale.service.ImportService;
import com.hysteryale.service.ShipmentService;
import com.hysteryale.utils.EnvironmentUtils;
import com.hysteryale.utils.FileUtils;
import com.hysteryale.utils.LocaleUtils;
import com.hysteryale.utils.ModelUtil;
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
import java.text.ParseException;
import java.util.List;
import java.util.Map;

@RestController
public class ShipmentController {

    @Resource
    ShipmentService shipmentService;

    @Resource
    ImportService importService;
    @Resource
    FileUploadService fileUploadService;

    @Resource
    FileUploadRepository fileUploadRepository;

    @Resource
    LocaleUtils localeUtils;

    @PostMapping("/getShipmentData")
    public Map<String, Object> getDataFinancialShipment(@RequestBody FilterModel filters,
                                                        @RequestParam(defaultValue = "1") int pageNo,
                                                        @RequestParam(defaultValue = "100") int perPage) throws ParseException {
        filters.setPageNo(pageNo);
        filters.setPerPage(perPage);


        return shipmentService.getShipmentByFilter(filters);

    }

    @PostMapping(path = "/importNewShipment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ResponseObject> importNewDataShipment(@RequestBody MultipartFile file, @RequestHeader("locale") String locale, Authentication authentication) throws Exception {
        String baseFolder = EnvironmentUtils.getEnvironmentValue("public-folder");
        String baseFolderUploaded = EnvironmentUtils.getEnvironmentValue("upload_files.base-folder");
        String targetFolder = EnvironmentUtils.getEnvironmentValue("upload_files.shipment");
        String excelFileExtension = FileUtils.EXCEL_FILE_EXTENSION;
        String savedFileName = fileUploadService.saveFileUploaded(file, authentication, targetFolder, excelFileExtension, ModelUtil.SHIPMENT);
        String pathFile = baseFolder + baseFolderUploaded + targetFolder + savedFileName;
        String fileUUID = fileUploadRepository.getFileUUIDByFileName(savedFileName);

        if (!FileUtils.isExcelFile(pathFile)) {
            throw new InvalidFileFormatException(file.getOriginalFilename(), fileUUID);
        }

        InputStream inputStream = new FileInputStream(pathFile);
        List<ImportFailure> importFailures = importService.importShipmentFileOneByOne(inputStream, fileUUID);
        String message = localeUtils.getMessageImportComplete(importFailures, ModelUtil.SHIPMENT, locale);
        fileUploadService.handleUpdatedSuccessfully(savedFileName);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject(message, fileUUID));


    }
}
