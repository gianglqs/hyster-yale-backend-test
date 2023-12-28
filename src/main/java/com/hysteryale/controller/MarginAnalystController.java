package com.hysteryale.controller;

import com.hysteryale.model_h2.IMMarginAnalystData;
import com.hysteryale.model_h2.IMMarginAnalystSummary;
import com.hysteryale.service.FileUploadService;
import com.hysteryale.service.PartService;
import com.hysteryale.service.marginAnalyst.IMMarginAnalystDataService;
import com.hysteryale.service.marginAnalyst.MarginAnalystMacroService;
import com.hysteryale.service.marginAnalyst.MarginAnalystService;
import com.hysteryale.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
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
    MarginAnalystService marginAnalystService;
    @Resource
    IMMarginAnalystDataService IMMarginAnalystDataService;
    @Resource
    FileUploadService fileUploadService;
    @Resource
    MarginAnalystMacroService marginAnalystMacroService;
    @Resource
    PartService partService;


    @GetMapping(path = "/marginAnalystData/getDealers")
    public Map<String, List<Map<String, String>>> getDealersInMarginAnalystData() {
        return marginAnalystService.getDealersFromMarginAnalystData();
    }

    /**
     * Calculate MarginAnalystData and MarginAnalystSummary based on user's uploaded file
     */
    @PostMapping(path = "/estimateMarginAnalystData", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> estimateMarginAnalystData(@RequestBody IMMarginAnalystData marginData) throws Exception {

        String currency = marginData.getCurrency();
        String orderNumber = marginData.getOrderNumber();
        String fileUUID = marginData.getFileUUID();
        String plant = marginData.getPlant();
        Integer type = marginData.getType();
        String modelCode = marginData.getModelCode();
        String series = marginData.getSeries();

        if(type == 0)
            type = null;
        if(modelCode.isEmpty())
            modelCode = null;
        if(series.isEmpty())
            series = null;

        List<IMMarginAnalystData> imMarginAnalystDataList = IMMarginAnalystDataService.getIMMarginAnalystData(modelCode, currency, fileUUID, orderNumber, type, series, plant);
        IMMarginAnalystSummary monthlySummary;
        IMMarginAnalystSummary annuallySummary;

        if(plant.equals("HYM") || plant.equals("SN") || plant.equals("Ruyi") || plant.equals("Maximal") || plant.equals("Staxx")) {
            if(imMarginAnalystDataList.isEmpty())
                IMMarginAnalystDataService.calculateNonUSMarginAnalystData(fileUUID, plant, currency);
            monthlySummary = IMMarginAnalystDataService.calculateNonUSMarginAnalystSummary(fileUUID, plant, currency, "monthly", type, series, modelCode);
            annuallySummary = IMMarginAnalystDataService.calculateNonUSMarginAnalystSummary(fileUUID, plant, currency, "annually", type, series, modelCode);
        } else {
            if(imMarginAnalystDataList.isEmpty())
                IMMarginAnalystDataService.calculateUSPlantMarginData(currency, orderNumber, fileUUID);
            monthlySummary = IMMarginAnalystDataService.calculateUSPlantMarginSummary(modelCode, series, currency, "monthly", orderNumber, type, fileUUID);
            annuallySummary = IMMarginAnalystDataService.calculateUSPlantMarginSummary(modelCode, series, currency, "annually", orderNumber, type, fileUUID);
        }

        imMarginAnalystDataList = IMMarginAnalystDataService.getIMMarginAnalystData(modelCode, currency, fileUUID, orderNumber, type, series, plant);

        return Map.of(
                "MarginAnalystData", imMarginAnalystDataList,
                "MarginAnalystSummary", Map.of(
                        "MarginAnalystSummaryMonthly", monthlySummary,
                        "MarginAnalystSummaryAnnually", annuallySummary
                )
        );
    }

    /**
     * Check the plant of model code included in file
     */
    @PostMapping(path = "marginData/checkFilePlant", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> checkPlantOfFile(@RequestBody MultipartFile file, Authentication authentication) throws Exception {
        String filePath = fileUploadService.saveFileUploadToDisk(file);

        // Verify the Excel file
        if (FileUtils.isExcelFile(filePath)) {
            String fileUUID = fileUploadService.saveFileUpload(file, authentication);

            IMMarginAnalystData marginAnalystData = IMMarginAnalystDataService.checkPlantOfFile(fileUUID);

            return Map.of(
                    "marginAnalystData", marginAnalystData,
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

        String filePath = fileUploadService.saveFileUploadToDisk(file);

        // Verify the Excel file
        if (FileUtils.isExcelFile(filePath)) {
            String originalFileName = file.getOriginalFilename();
            String fileUUID = fileUploadService.saveFileUpload(file, authentication);

            log.info("Saved " + fileUUID + " - type: " + file.getContentType());
            marginAnalystMacroService.importMarginAnalystMacroFromFile(originalFileName, filePath);

        } else {
            fileUploadService.deleteFileInDisk(filePath);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded file is not an Excel file");
        }
    }

    @PostMapping(path = "/importPowerBiFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public void importPowerBiFile(@RequestBody MultipartFile file, Authentication authentication) throws Exception {
        String filePath = fileUploadService.saveFileUploadToDisk(file);

        // Verify the Excel file
        if (FileUtils.isExcelFile(filePath)) {
            String originalFileName = file.getOriginalFilename();
            String fileUUID = fileUploadService.saveFileUpload(file, authentication);

            log.info("Saved " + fileUUID + " - type: " + file.getContentType());
            partService.importPartFromFile(originalFileName, filePath);

        } else {
            fileUploadService.deleteFileInDisk(filePath);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded file is not an Excel file");
        }
    }
}

