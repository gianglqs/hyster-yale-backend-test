package com.hysteryale.controller;

import com.hysteryale.exception.InvalidFileFormatException;
import com.hysteryale.model.enums.FrequencyImport;
import com.hysteryale.model.enums.ModelTypeEnum;
import com.hysteryale.model.importFailure.ImportFailure;
import com.hysteryale.repository.upload.FileUploadRepository;
import com.hysteryale.response.ResponseObject;
import com.hysteryale.service.FileUploadService;
import com.hysteryale.service.ImportTrackingService;
import com.hysteryale.service.ResidualValueService;
import com.hysteryale.utils.*;
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
@RequestMapping("residualValue")
public class ResidualValueController {
    @Resource
    FileUploadService fileUploadService;

    @Resource
    FileUploadRepository fileUploadRepository;

    @Resource
    ResidualValueService residualValueService;
    @Resource
    LocaleUtils localeUtils;
    @Resource
    ImportTrackingService importTrackingService;

    @PostMapping(path = "/importData", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ResponseObject> importData(@RequestParam("file") MultipartFile file, @RequestHeader("locale") String locale, Authentication authentication) throws Exception {
        String baseFolder = EnvironmentUtils.getEnvironmentValue("public-folder");
        String baseFolderUploaded = EnvironmentUtils.getEnvironmentValue("upload_files.base-folder");
        String targetFolder = EnvironmentUtils.getEnvironmentValue("upload_files.residual_value");
        String modelType = ModelTypeEnum.RESIDUAL_VALUE.getValue();

        //save file on disk
        String excelFileExtension = FileUtils.EXCEL_FILE_EXTENSION;
        String savedFileName = fileUploadService.saveFileUploaded(file, authentication, targetFolder, excelFileExtension, modelType);
        String filePath = baseFolder + baseFolderUploaded + targetFolder + savedFileName;

        String fileUUID = fileUploadRepository.getFileUUIDByFileName(savedFileName);

        if (!FileUtils.isExcelFile(filePath)) {
            throw new InvalidFileFormatException(file.getOriginalFilename(), fileUUID);
        }

        int year = DateUtils.extractYear(file.getOriginalFilename(), fileUUID);
        System.out.println(year);
        List<ImportFailure> importFailures = residualValueService.importResidualValue(filePath, fileUUID, year);
        String message = localeUtils.getMessageImportComplete(importFailures, modelType, locale);
        fileUploadService.handleUpdatedSuccessfully(savedFileName);
        // update ImportTracking
        importTrackingService.updateImport(fileUUID, file.getOriginalFilename(), FrequencyImport.ANNUAL);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject(message, fileUUID));
    }

    @GetMapping("/getResidualValueData")
    public Map<String, Object> getResidualValueDataByFilter(@RequestParam String modelCode){
        return residualValueService.getDataByFilter(modelCode);
    }

}
