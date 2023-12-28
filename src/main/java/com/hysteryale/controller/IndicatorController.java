package com.hysteryale.controller;

import com.hysteryale.model.competitor.CompetitorColor;
import com.hysteryale.model.competitor.CompetitorPricing;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.model.filters.SwotFilters;
import com.hysteryale.service.FileUploadService;
import com.hysteryale.service.IndicatorService;
import com.hysteryale.utils.EnvironmentUtils;
import com.hysteryale.utils.FileUtils;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class IndicatorController {
    @Resource
    IndicatorService indicatorService;
    @Resource
    FileUploadService fileUploadService;


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
    public Map<String, Object> getCompetitorColorDetails(@RequestParam("id") int id) {
        return Map.of("competitorColorDetail", indicatorService.getCompetitorById(id));
    }

    @PutMapping("/competitorColors")
    public void updateCompetitorColor(@RequestBody CompetitorColor competitorColor) {
        indicatorService.updateCompetitorColor(competitorColor);
    }

    @PostMapping("/importIndicatorsFile")
    public void importIndicatorsFile(@RequestBody MultipartFile file, Authentication authentication) throws Exception {
        String filePath = fileUploadService.saveFileUploadToDisk(file);

        // Verify the Excel file
        if (FileUtils.isExcelFile(filePath)) {
            indicatorService.importIndicatorsFromFile(filePath);

        } else {
            fileUploadService.deleteFileInDisk(filePath);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded file is not an Excel file");
        }
    }

    @PostMapping("/uploadForecastFile")
    public void uploadForecastFile(@RequestBody MultipartFile file, Authentication authentication) throws Exception {
        indicatorService.uploadForecastFile(file, authentication);
    }
}
