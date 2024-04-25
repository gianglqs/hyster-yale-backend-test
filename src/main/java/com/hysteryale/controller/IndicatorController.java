/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.controller;

import com.hysteryale.exception.CompetitorException.CompetitorColorNotFoundException;
import com.hysteryale.exception.InvalidFileFormatException;
import com.hysteryale.model.Region;
import com.hysteryale.model.competitor.CompetitorColor;
import com.hysteryale.model.competitor.CompetitorPricing;
import com.hysteryale.model.enums.FrequencyImport;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.model.filters.SwotFilters;
import com.hysteryale.repository.upload.FileUploadRepository;
import com.hysteryale.service.FileUploadService;
import com.hysteryale.service.ImportService;
import com.hysteryale.service.ImportTrackingService;
import com.hysteryale.service.IndicatorService;
import com.hysteryale.utils.CheckRequiredColumnUtils;
import com.hysteryale.utils.EnvironmentUtils;
import com.hysteryale.utils.FileUtils;
import com.hysteryale.model.enums.ModelTypeEnum;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class IndicatorController {
    @Resource
    IndicatorService indicatorService;
    @Resource
    FileUploadService fileUploadService;

    @Resource
    FileUploadRepository fileUploadRepository;

    @Resource
    ImportTrackingService importTrackingService;

    @Resource
    ImportService importService;

    @PostMapping("/getCompetitorData")
    public Map<String, Object> getCompetitorData(@RequestBody FilterModel filters,
                                                 @RequestParam(defaultValue = "0") int pageNo,
                                                 @RequestParam(defaultValue = "100") int perPage) throws ParseException {
        filters.setPageNo(pageNo);
        filters.setPerPage(perPage);

        return indicatorService.getCompetitorPriceForTableByFilter(filters);
    }

    @PostMapping(value = "/chart/getDataForCompetitorBubbleChart", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getCompetitorPricing(@RequestBody SwotFilters filters) {

        List<CompetitorPricing> competitorPricingList =
                indicatorService.getCompetitiveLandscape(filters);
        return Map.of(
                "competitiveLandscape", competitorPricingList
        );
    }


    @PostMapping("/chart/getDataForRegionLineChart")
    public Map<String, List<CompetitorPricing>> getDataForLineChartRegion(@RequestBody FilterModel filters) throws ParseException {
        Map<String, List<CompetitorPricing>> result = new HashMap<>();
        List<CompetitorPricing> listCompetitorPricingGroupByRegion = indicatorService.getCompetitorPricingAfterFilterAndGroupByRegion(filters);
        result.put("lineChartRegion", listCompetitorPricingGroupByRegion);
        return result;
    }

    @PostMapping("/chart/getDataForPlantLineChart")
    public Map<String, List<CompetitorPricing>> getDataForLineChartPlant(@RequestBody FilterModel filters) throws ParseException {
        Map<String, List<CompetitorPricing>> result = new HashMap<>();
        List<CompetitorPricing> listCompetitorPricingGroupByRegion = indicatorService.getCompetitorPricingAfterFilterAndGroupByPlant(filters);
        result.put("lineChartPlant", listCompetitorPricingGroupByRegion);
        return result;
    }

    @GetMapping("/competitorColors")
    public Map<String, Object> getCompetitorColor(@RequestParam(required = false) String search,
                                                  @RequestParam(defaultValue = "100") int perPage,
                                                  @RequestParam(defaultValue = "1") int pageNo) {
        Page<CompetitorColor> competitorColors = indicatorService.searchCompetitorColor(search, pageNo, perPage);

        return Map.of(
                "competitorColors", competitorColors.getContent(),
                "page", pageNo,
                "perPage", perPage,
                "totalPages", competitorColors.getTotalPages(),
                "totalItems", competitorColors.getTotalElements()
        );
    }

    @GetMapping("/competitorColors/getDetails")
    public Map<String, Object> getCompetitorColorDetails(@RequestParam("id") int id) throws CompetitorColorNotFoundException {
        return Map.of("competitorColorDetail", indicatorService.getCompetitorById(id));
    }

    @PutMapping("/competitorColors")
    public void updateCompetitorColor(@RequestBody CompetitorColor competitorColor) throws CompetitorColorNotFoundException {
        indicatorService.updateCompetitorColor(competitorColor);
    }

    @PostMapping("/importIndicatorsFile")
    public void importIndicatorsFile(@RequestBody MultipartFile file, Authentication authentication) throws Exception {

        String baseFolder = EnvironmentUtils.getEnvironmentValue("public-folder");
        String baseFolderUploaded = EnvironmentUtils.getEnvironmentValue("upload_files.base-folder");
        String targetFolder = EnvironmentUtils.getEnvironmentValue("upload_files.competitor");
        String excelFileExtension = FileUtils.EXCEL_FILE_EXTENSION;
        String savedFileName = fileUploadService.saveFileUploaded(file, authentication, targetFolder, excelFileExtension, ModelTypeEnum.COMPETITOR.getValue());
        String pathFile = baseFolder + baseFolderUploaded + targetFolder + savedFileName;
        String fileUUID = fileUploadRepository.getFileUUIDByFileName(savedFileName);

        if (!FileUtils.isExcelFile(pathFile)) {
            fileUploadService.handleUpdatedFailure(fileUUID, "Uploaded file is not an Excel file");
            throw new InvalidFileFormatException("Uploaded file is not an Excel file " + savedFileName, fileUUID);
        }

        try {
            indicatorService.importIndicatorsFromFile(pathFile, fileUUID);
            fileUploadService.handleUpdatedSuccessfully(savedFileName);
            // update ImportTracking
            importTrackingService.updateImport(fileUUID, file.getOriginalFilename(), FrequencyImport.AD_HOC_IMPORT);
        } catch (Exception e) {
            fileUploadService.handleUpdatedFailure(fileUUID, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/uploadForecastFile")
    public void uploadForecastFile(@RequestBody MultipartFile file, Authentication authentication) throws Exception {
        String baseFolder = EnvironmentUtils.getEnvironmentValue("public-folder");
        String baseFolderUploaded = EnvironmentUtils.getEnvironmentValue("upload_files.base-folder");
        String targetFolder = EnvironmentUtils.getEnvironmentValue("upload_files.forecast_pricing");
        String excelFileExtension = FileUtils.EXCEL_FILE_EXTENSION;
        String savedFileName = fileUploadService.saveFileUploaded(file, authentication, targetFolder, excelFileExtension, ModelTypeEnum.FORECAST_PRICING.getValue());
        String pathFile = baseFolder + baseFolderUploaded + targetFolder + savedFileName;
        String fileUUID = fileUploadRepository.getFileUUIDByFileName(savedFileName);

        if (!FileUtils.isXLSXFile(pathFile)) {
            fileUploadService.handleUpdatedFailure(fileUUID, "Uploaded file is not an Excel file");
            throw new InvalidFileFormatException("Uploaded file is not an Excel file " + savedFileName, fileUUID);
        }
        InputStream is = new FileInputStream(pathFile);
        XSSFWorkbook workbook = new XSSFWorkbook(is);
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        List<String> titleColumnCurrent = new ArrayList<>();


        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            Region region = importService.getRegionBySheetName(sheet.getSheetName());
            if (region == null) continue;
            Row headerRow = sheet.getRow(1);
            for (int j = 0; j < CheckRequiredColumnUtils.FORECAST_REQUIRED_COLUMN.size(); j++) {
                Cell cell = headerRow.getCell(j);
                if (cell == null)
                    continue;
                if (cell.getCellType() == CellType.STRING)
                    titleColumnCurrent.add(cell.getStringCellValue());
                else if (cell.getCellType() == CellType.NUMERIC)
                    titleColumnCurrent.add(String.valueOf(cell.getNumericCellValue()));
                else if (cell.getCellType() == CellType.FORMULA) {
                    CellValue cellValue = evaluator.evaluate(cell);
                    if (cellValue.getCellType() == CellType.NUMERIC)
                        titleColumnCurrent.add(String.valueOf(cellValue.getNumberValue()));
                    else
                        titleColumnCurrent.add(cellValue.getStringValue());
                }
            }
        }
        CheckRequiredColumnUtils.checkRequiredColumn(titleColumnCurrent, CheckRequiredColumnUtils.FORECAST_REQUIRED_COLUMN, fileUUID);
        // update ImportTracking
        importTrackingService.updateImport(fileUUID, file.getOriginalFilename(), FrequencyImport.AD_HOC_IMPORT);
    }
}
