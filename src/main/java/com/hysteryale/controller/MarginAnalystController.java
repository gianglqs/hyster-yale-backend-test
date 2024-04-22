/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.controller;

import com.hysteryale.exception.InvalidFileFormatException;
import com.hysteryale.exception.UserException.EmailNotFoundException;
import com.hysteryale.model.User;
import com.hysteryale.model.marginAnalyst.CalculatedMargin;
import com.hysteryale.model_h2.MarginData;
import com.hysteryale.model_h2.MarginDataId;
import com.hysteryale.model_h2.MarginSummaryId;
import com.hysteryale.repository.upload.FileUploadRepository;
import com.hysteryale.service.FileUploadService;
import com.hysteryale.service.PartService;
import com.hysteryale.service.UserService;
import com.hysteryale.service.marginAnalyst.MarginDataService;
import com.hysteryale.service.marginAnalyst.MarginAnalystMacroService;
import com.hysteryale.utils.EnvironmentUtils;
import com.hysteryale.utils.FileUtils;
import com.hysteryale.model.enums.ModelTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class MarginAnalystController {

    @Resource
    MarginDataService marginDataService;
    @Resource
    FileUploadService fileUploadService;
    @Resource
    MarginAnalystMacroService marginAnalystMacroService;
    @Resource
    PartService partService;
    @Resource
    FileUploadRepository fileUploadRepository;
    @Resource
    UserService userService;

    /**
     * Calculate MarginAnalystData and MarginAnalystSummary based on user's uploaded file
     */
    @PostMapping(path = "/estimateMarginAnalystData", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> estimateMarginAnalystData(@RequestBody CalculatedMargin calculatedMargin, Authentication authentication) throws Exception {
        // Get the User requesting to estimate Margin Analysis Data
        User user = userService.getUserByEmail(authentication.getName());

        MarginData marginData = calculatedMargin.getMarginData();
        String region = calculatedMargin.getRegion();

        MarginDataId id = marginData.getId();
        String currency = id.getCurrency();
        String orderNumber = marginData.getOrderNumber();
        String fileUUID = marginData.getFileUUID();
        Integer type = id.getType();
        String modelCode = id.getModelCode();
        String series = marginData.getSeries();

        if (type == 0)
            type = null;
        if (modelCode.isEmpty())
            modelCode = null;
        if (orderNumber.isEmpty())
            orderNumber = null;

        if (!marginDataService.isFileCalculated(fileUUID, currency, region))
            marginDataService.calculateMarginAnalysisData(fileUUID, currency, region, user.getId());

        List<MarginData> marginDataList = marginDataService.getIMMarginAnalystData(modelCode, currency, fileUUID, orderNumber, type, series, region);

        double targetMargin = 0.0;
        if (!marginDataList.isEmpty() && series != null)
            targetMargin = marginAnalystMacroService.getLatestTargetMarginValue(region, series.substring(1));

        assert series != null;
        return Map.of(
                "MarginAnalystData", marginDataList,
                "MarginAnalystSummary", marginDataService.calculateMarginAnalysisSummary(fileUUID, type, modelCode, series, orderNumber, currency, region, user.getId(), marginDataList),
                "TargetMargin", targetMargin
        );
    }

    /**
     * Check the plant of model code included in file
     */
    @PostMapping(path = "/marginData/readNOVOFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> readNOVOFile(@RequestBody MultipartFile file, Authentication authentication) throws Exception {
        String baseFolder = EnvironmentUtils.getEnvironmentValue("public-folder");
        String baseFolderUploaded = EnvironmentUtils.getEnvironmentValue("upload_files.base-folder");
        String targetFolder = EnvironmentUtils.getEnvironmentValue("upload_files.novo");
        String excelFileExtension = FileUtils.EXCEL_FILE_EXTENSION;
        String fileName = fileUploadService.saveFileUploaded(file, authentication, targetFolder, excelFileExtension, ModelTypeEnum.NOVO.getValue());
        String filePath = baseFolder + baseFolderUploaded + targetFolder + fileName;
        String fileUUID = fileUploadRepository.getFileUUIDByFileName(fileName);

        // Verify the Excel file
        if (!FileUtils.isExcelFile(filePath))
            throw new InvalidFileFormatException(file.getOriginalFilename(), fileUUID);

        String uuid = fileUploadRepository.getUUIDByName(fileName);
        Map<String, Object> marginFilters = marginDataService.populateMarginFilters(filePath, uuid);
        fileUploadService.handleUpdatedSuccessfully(fileName);

        return Map.of(
                "marginFilters", marginFilters,
                "fileUUID", uuid
        );
    }

    @PostMapping(path = "/importMacroFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public void importMacroFile(@RequestBody MultipartFile file, Authentication authentication) throws Exception {
        String baseFolder = EnvironmentUtils.getEnvironmentValue("public-folder");
        String baseFolderUploaded = EnvironmentUtils.getEnvironmentValue("upload_files.base-folder");
        String targetFolder = EnvironmentUtils.getEnvironmentValue("upload_files.macro");
        String excelFileExtension = FileUtils.EXCEL_FILE_EXTENSION;
        String fileName = fileUploadService.saveFileUploaded(file, authentication, targetFolder, excelFileExtension, ModelTypeEnum.MACRO.getValue());
        String filePath = baseFolder + baseFolderUploaded + targetFolder + fileName;
        String fileUUID = fileUploadRepository.getFileUUIDByFileName(fileName);

        // Verify the Excel file
        if (!FileUtils.isExcelFile(filePath))
            throw new InvalidFileFormatException(file.getOriginalFilename(), fileUUID);

        marginAnalystMacroService.importMarginAnalystMacroFromFile(file.getOriginalFilename(), filePath, fileUUID);
        fileUploadService.handleUpdatedSuccessfully(fileName);

    }

    @PostMapping(path = "/importPowerBiFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public void importPowerBiFile(@RequestBody MultipartFile file, Authentication authentication) throws Exception {
        String baseFolder = EnvironmentUtils.getEnvironmentValue("public-folder");
        String baseFolderUploaded = EnvironmentUtils.getEnvironmentValue("upload_files.base-folder");
        String targetFolder = EnvironmentUtils.getEnvironmentValue("upload_files.part");
        String excelFileExtension = FileUtils.EXCEL_FILE_EXTENSION;
        String savedFileName = fileUploadService.saveFileUploaded(file, authentication, targetFolder, excelFileExtension, ModelTypeEnum.PART.getValue());
        String filePath = baseFolder + baseFolderUploaded + targetFolder + savedFileName;
        String fileUUID = fileUploadRepository.getFileUUIDByFileName(savedFileName);

        // Verify the Excel file
        if (!FileUtils.isExcelFile(filePath))
            throw new InvalidFileFormatException(file.getOriginalFilename(), fileUUID);

        partService.importPartFromFile(file.getOriginalFilename(), filePath, fileUUID);
        fileUploadService.handleUpdatedSuccessfully(savedFileName);

    }

    @PostMapping(path = "/list-history-margin")
    public Map<String, Object> ListHistoryMarginSummary(Authentication authentication) throws EmailNotFoundException {
        User user = userService.getUserByEmail(authentication.getName());
        return Map.of(
                "historicalMargin", marginDataService.listHistoryMarginSummary(user.getId())
        );
    }

    @PostMapping(path = "/view-history-margin", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> viewHistoryMargin(@RequestBody MarginSummaryId id, Authentication authentication) throws EmailNotFoundException {
        User user = userService.getUserByEmail(authentication.getName());
        id.setUserId(user.getId());
        log.info("{}", id.getType());
        log.info("{}", id.getSeries());
        return Map.of(
                "margin", marginDataService.viewHistoryMarginSummary(id)
        );
    }
}