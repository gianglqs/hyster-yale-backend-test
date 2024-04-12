package com.hysteryale.controller;

import com.hysteryale.exception.InvalidFileFormatException;
import com.hysteryale.model.enums.FrequencyImport;
import com.hysteryale.model.marginAnalyst.CalculatedMargin;
import com.hysteryale.model_h2.IMMarginAnalystData;
import com.hysteryale.repository.upload.FileUploadRepository;
import com.hysteryale.service.FileUploadService;
import com.hysteryale.service.ImportTrackingService;
import com.hysteryale.service.PartService;
import com.hysteryale.service.marginAnalyst.IMMarginAnalystDataService;
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
    IMMarginAnalystDataService IMMarginAnalystDataService;
    @Resource
    FileUploadService fileUploadService;
    @Resource
    MarginAnalystMacroService marginAnalystMacroService;
    @Resource
    PartService partService;

    @Resource
    FileUploadRepository fileUploadRepository;



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

        if (!IMMarginAnalystDataService.isFileCalculated(fileUUID, currency, region))
            IMMarginAnalystDataService.calculateMarginAnalysisData(fileUUID, currency, region);

        List<IMMarginAnalystData> imMarginAnalystDataList = IMMarginAnalystDataService.getIMMarginAnalystData(modelCode, currency, fileUUID, orderNumber, type, series, region);

        double targetMargin = 0.0;
        if (!imMarginAnalystDataList.isEmpty() && series != null)
            targetMargin = marginAnalystMacroService.getLatestTargetMarginValue(region, series.substring(1));

        assert series != null;
        return Map.of(
                "MarginAnalystData", imMarginAnalystDataList,
                "MarginAnalystSummary", IMMarginAnalystDataService.calculateMarginAnalysisSummary(fileUUID, type, modelCode, series, orderNumber, currency, region),
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
        Map<String, Object> marginFilters = IMMarginAnalystDataService.populateMarginFilters(filePath, fileName, uuid);
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
}