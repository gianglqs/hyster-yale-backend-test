package com.hysteryale.controller;

import com.hysteryale.model.marginAnalyst.CalculatedMargin;
import com.hysteryale.model_h2.IMMarginAnalystData;
import com.hysteryale.service.FileUploadService;
import com.hysteryale.service.PartService;
import com.hysteryale.service.marginAnalyst.IMMarginAnalystDataService;
import com.hysteryale.service.marginAnalyst.MarginAnalystMacroService;
import com.hysteryale.utils.EnvironmentUtils;
import com.hysteryale.utils.FileUtils;
import com.hysteryale.utils.ModelUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class MarginAnalystController {

    @Resource
    IMMarginAnalystDataService IMMarginAnalystDataService;
    @Resource
    FileUploadService fileUploadService;
    @Resource
    MarginAnalystMacroService marginAnalystMacroService;
    @Resource
    PartService partService;

    /**
     * Calculate MarginAnalystData and MarginAnalystSummary based on user's uploaded file
     */
    @PostMapping(path = "/estimateMarginAnalystData", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> estimateMarginAnalystData(@RequestBody CalculatedMargin calculatedMargin) throws Exception {

        IMMarginAnalystData marginData = calculatedMargin.getMarginData();
        String region = calculatedMargin.getRegion();

        String currency = marginData.getCurrency();
        String orderNumber = marginData.getOrderNumber();
        String fileUUID = marginData.getFileUUID();
        Integer type = marginData.getType();
        String modelCode = marginData.getModelCode();
        String series = marginData.getSeries();

        if (type == 0)
            type = null;
        if (modelCode.isEmpty())
            modelCode = null;
        if (orderNumber.isEmpty())
            orderNumber = null;

        if (!IMMarginAnalystDataService.isFileCalculated(fileUUID, currency))
            IMMarginAnalystDataService.calculateMarginAnalysisData(fileUUID, currency);

        List<IMMarginAnalystData> imMarginAnalystDataList = IMMarginAnalystDataService.getIMMarginAnalystData(modelCode, currency, fileUUID, orderNumber, type, series);

        double targetMargin = 0.0;
        if (!imMarginAnalystDataList.isEmpty() && series != null)
            targetMargin = marginAnalystMacroService.getLatestTargetMarginValue(region, series.substring(1));

        assert series != null;
        return Map.of(
                "MarginAnalystData", imMarginAnalystDataList,
                "MarginAnalystSummary", IMMarginAnalystDataService.calculateMarginAnalysisSummary(fileUUID, type, modelCode, series, orderNumber, currency),
                "TargetMargin", targetMargin
        );
    }

    /**
     * Check the plant of model code included in file
     */
    @PostMapping(path = "/marginData/readNOVOFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> readNOVOFile(@RequestBody MultipartFile file, Authentication authentication) throws Exception {
        String filePath = fileUploadService.saveFileUploadToDisk(file);

        // Verify the Excel file
        if (FileUtils.isExcelFile(filePath)) {
            String fileUUID = fileUploadService.saveFileUpload(filePath, authentication);
            Map<String, Object> marginFilters = IMMarginAnalystDataService.populateMarginFilters(fileUUID);

            return Map.of(
                    "marginFilters", marginFilters,
                    "fileUUID", fileUUID
            );
        } else {
            fileUploadService.deleteFileInDisk(filePath);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded file is not an Excel file");
        }
    }

    @PostMapping(path = "/importMacroFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public void importMacroFile(@RequestBody MultipartFile file, Authentication authentication) throws Exception {


        String baseFolder = EnvironmentUtils.getEnvironmentValue("public-folder");
        String baseFolderUploaded = EnvironmentUtils.getEnvironmentValue("upload_files.base-folder");
        String targetFolder = EnvironmentUtils.getEnvironmentValue("upload_files.macro");
        String excelFileExtension = FileUtils.EXCEL_FILE_EXTENSION;
        String fileName = fileUploadService.saveFileUploaded(file, authentication, targetFolder, excelFileExtension, ModelUtil.MACRO);
        String filePath = baseFolder + baseFolderUploaded + targetFolder + fileName;


        // Verify the Excel file
        if (!FileUtils.isExcelFile(filePath)) {
            fileUploadService.handleUpdatedFailure(fileName, "Uploaded file is not an Excel file");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded file is not an Excel file");
        }

        try {
            marginAnalystMacroService.importMarginAnalystMacroFromFile(file.getOriginalFilename(), filePath);

            fileUploadService.handleUpdatedSuccessfully(fileName);
        } catch (Exception e) {
            fileUploadService.handleUpdatedFailure(fileName, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }


    }

    @PostMapping(path = "/importPowerBiFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public void importPowerBiFile(@RequestBody MultipartFile file, Authentication authentication) throws Exception {

        String baseFolder = EnvironmentUtils.getEnvironmentValue("public-folder");
        String baseFolderUploaded = EnvironmentUtils.getEnvironmentValue("upload_files.base-folder");
        String targetFolder = EnvironmentUtils.getEnvironmentValue("upload_files.shipment");
        String excelFileExtension = FileUtils.EXCEL_FILE_EXTENSION;
        String fileName = fileUploadService.saveFileUploaded(file, authentication, targetFolder, excelFileExtension, ModelUtil.PART);
        String filePath = baseFolder + baseFolderUploaded + targetFolder + fileName;

        // Verify the Excel file
        if (!FileUtils.isExcelFile(filePath)) {
            fileUploadService.handleUpdatedFailure(fileName, "Uploaded file is not an Excel file");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded file is not an Excel file");
        }

        try {
            partService.importPartFromFile(file.getOriginalFilename(), filePath);
            fileUploadService.handleUpdatedSuccessfully(fileName);
        } catch (Exception e) {
            fileUploadService.handleUpdatedFailure(fileName, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }


    }
}

