package com.hysteryale.controller;

import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.response.ResponseObject;
import com.hysteryale.service.DealerService;
import com.hysteryale.service.FileUploadService;
import com.hysteryale.utils.EnvironmentUtils;
import com.hysteryale.utils.FileUtils;
import com.hysteryale.utils.ModelUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@RestController
@Slf4j
public class DearlerController {

    @Resource
    DealerService dealerService;

    @Resource
    FileUploadService fileUploadService;

    private FilterModel filters;
    @PostMapping(path = "/importNewDealer", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ResponseObject> importNewDataDealer(@RequestParam("file") MultipartFile file, Authentication authentication) throws Exception {
        String baseFolder = EnvironmentUtils.getEnvironmentValue("public-folder");
        String baseFolderUploaded = EnvironmentUtils.getEnvironmentValue("upload_files.base-folder");
        String targetFolder = EnvironmentUtils.getEnvironmentValue("upload_files.dealer");
        //save file on disk
        String excelFileExtension = FileUtils.EXCEL_FILE_EXTENSION;


        String fileName = fileUploadService.saveFileUploaded(file, authentication, targetFolder, excelFileExtension, ModelUtil.DEALER);
        String filePath = baseFolder + baseFolderUploaded + targetFolder + fileName;

        if (!FileUtils.isExcelFile(filePath)) {
            fileUploadService.handleUpdatedFailure(fileName, "Uploaded file is not an Excel file");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseObject("File is not EXCEL",null));

        }

        try {
            dealerService.importNewDealerFileByFile(filePath);
            fileUploadService.handleUpdatedSuccessfully(fileName);
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("Import successfully", null));
        } catch (Exception e) {
            fileUploadService.handleUpdatedFailure(fileName, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseObject(e.getMessage(), null));
        }
    }
}
