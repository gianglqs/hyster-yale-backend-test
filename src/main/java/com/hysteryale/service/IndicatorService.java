package com.hysteryale.service;

import com.hysteryale.model.competitor.CompetitorColor;
import com.hysteryale.model.competitor.CompetitorPricing;
import com.hysteryale.model.competitor.ForeCastValue;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.model.filters.SwotFilters;
import com.hysteryale.repository.CompetitorColorRepository;
import com.hysteryale.repository.CompetitorPricingRepository;
import com.hysteryale.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@SuppressWarnings("unchecked")
public class IndicatorService extends BasedService {
    @Resource
    CompetitorPricingRepository competitorPricingRepository;
    @Resource
    CompetitorColorRepository competitorColorRepository;
    @Resource
    FileUploadService fileUploadService;
    @Resource
    ImportService importService;


    public Map<String, Object> getCompetitorPriceForTableByFilter(FilterModel filterModel) throws ParseException {
        logInfo(filterModel.toString());
        Map<String, Object> result = new HashMap<>();

        Map<String, Object> filterMap = ConvertDataFilterUtil.loadDataFilterIntoMap(filterModel);
        List<CompetitorPricing> competitorPricingList = competitorPricingRepository.findCompetitorByFilterForTable(
                (List<String>) filterMap.get("regionFilter"), (List<String>) filterMap.get("plantFilter"), (List<String>) filterMap.get("metaSeriesFilter"),
                (List<String>) filterMap.get("classFilter"), (List<String>) filterMap.get("modelFilter"), (Boolean) filterMap.get("ChineseBrandFilter"),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((String) ((List) filterMap.get("marginPercentageFilter")).get(0)),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((Double) ((List) filterMap.get("marginPercentageFilter")).get(1)), (Pageable) filterMap.get("pageable"));
        result.put("listCompetitor", competitorPricingList);

        //get total Recode
        int totalCompetitor = competitorPricingRepository.getCountAll(
                (List<String>) filterMap.get("regionFilter"), (List<String>) filterMap.get("plantFilter"), (List<String>) filterMap.get("metaSeriesFilter"),
                (List<String>) filterMap.get("classFilter"), (List<String>) filterMap.get("modelFilter"), (Boolean) filterMap.get("ChineseBrandFilter"),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((String) ((List) filterMap.get("marginPercentageFilter")).get(0)),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((Double) ((List) filterMap.get("marginPercentageFilter")).get(1)));
        result.put("totalItems", totalCompetitor);

        // get Total
        List<CompetitorPricing> getTotal = competitorPricingRepository.getTotal(
                (List<String>) filterMap.get("regionFilter"), (List<String>) filterMap.get("plantFilter"), (List<String>) filterMap.get("metaSeriesFilter"),
                (List<String>) filterMap.get("classFilter"), (List<String>) filterMap.get("modelFilter"), (Boolean) filterMap.get("ChineseBrandFilter"),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((String) ((List) filterMap.get("marginPercentageFilter")).get(0)),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((Double) ((List) filterMap.get("marginPercentageFilter")).get(1)));
        result.put("total", getTotal);

        // get latest updated time
        Optional<LocalDateTime> latestUpdatedTimeOptional = competitorPricingRepository.getLatestUpdatedTime();
        String latestUpdatedTime = null;
        if (latestUpdatedTimeOptional.isPresent()) {
            latestUpdatedTime = DateUtils.convertLocalDateTimeToString(latestUpdatedTimeOptional.get());
        }

        result.put("latestUpdatedTime", latestUpdatedTime);
        result.put("serverTimeZone", TimeZone.getDefault().getID());

        return result;
    }


    public List<CompetitorPricing> getCompetitorPricingAfterFilterAndGroupByRegion(FilterModel filterModel) throws ParseException {
        logInfo(filterModel.toString());
        Map<String, Object> filterMap = ConvertDataFilterUtil.loadDataFilterIntoMap(filterModel);
        return competitorPricingRepository.findCompetitorByFilterForLineChartRegion(
                (List<String>) filterMap.get("regionFilter"), (List<String>) filterMap.get("plantFilter"), (List<String>) filterMap.get("metaSeriesFilter"),
                (List<String>) filterMap.get("classFilter"), (List<String>) filterMap.get("modelFilter"), (Boolean) filterMap.get("ChineseBrandFilter"),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((String) ((List) filterMap.get("marginPercentageFilter")).get(0)),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((Double) ((List) filterMap.get("marginPercentageFilter")).get(1)));
    }


    public List<CompetitorPricing> getCompetitorPricingAfterFilterAndGroupByPlant(FilterModel filterModel) throws ParseException {
        logInfo(filterModel.toString());
        Map<String, Object> filterMap = ConvertDataFilterUtil.loadDataFilterIntoMap(filterModel);
        return competitorPricingRepository.findCompetitorByFilterForLineChartPlant(
                (List<String>) filterMap.get("regionFilter"), (List<String>) filterMap.get("plantFilter"), (List<String>) filterMap.get("metaSeriesFilter"),
                (List<String>) filterMap.get("classFilter"), (List<String>) filterMap.get("modelFilter"), (Boolean) filterMap.get("ChineseBrandFilter"),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((String) ((List) filterMap.get("marginPercentageFilter")).get(0)),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((Double) ((List) filterMap.get("marginPercentageFilter")).get(1)));
    }

    public List<CompetitorPricing> getCompetitiveLandscape(SwotFilters filters) {

        String regions = filters.getRegions();
        List<String> countryNames = filters.getCountries();
        List<String> competitorClass = filters.getClasses();
        List<String> category = filters.getCategories();
        List<String> series = filters.getSeries();

        if (countryNames.isEmpty())
            countryNames = null;
        if (competitorClass.isEmpty())
            competitorClass = null;
        if (category.isEmpty())
            category = null;
        if (series.isEmpty())
            series = null;
        return competitorPricingRepository.getDataForBubbleChart(Collections.singletonList(regions), countryNames, competitorClass, category, series);
    }

    /**
     * Get CompetitorColor by competitorName
     *
     * @return competitor color if existed, else randomly generate new one.
     */
    public CompetitorColor getCompetitorColor(String groupName) {
        Optional<CompetitorColor> optional = competitorColorRepository.getCompetitorColor(groupName.strip());
        if (optional.isEmpty()) {
            Random random = new Random();
            int nextColorCode = random.nextInt(256 * 256 * 256);
            String colorCode = String.format("#%06x", nextColorCode);
            return competitorColorRepository.save(new CompetitorColor(groupName, colorCode));
        } else
            return optional.get();
    }

    public CompetitorColor getCompetitorById(int id) {
        Optional<CompetitorColor> optional = competitorColorRepository.findById(id);
        if (optional.isPresent())
            return optional.get();
        else
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Competitor Color not found");
    }

    public Page<CompetitorColor> searchCompetitorColor(String search, int pageNo, int perPage) {
        Pageable pageable = PageRequest.of(pageNo - 1, perPage, Sort.by("groupName").ascending());
        return competitorColorRepository.searchCompetitorColor(search, pageable);
    }

    @Transactional
    public void updateCompetitorColor(CompetitorColor modifyColor) {
        Optional<CompetitorColor> optional = competitorColorRepository.findById(modifyColor.getId());
        if (optional.isPresent()) {
            CompetitorColor dbCompetitorColor = optional.get();

            dbCompetitorColor.setGroupName(modifyColor.getGroupName());
            dbCompetitorColor.setColorCode(modifyColor.getColorCode());
        } else throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Competitor Color not found");
    }

    /**
     * Save new Forecast file after deleting current Forecast file
     */
    public void uploadForecastFile(MultipartFile multipartFile, Authentication authentication) throws Exception {
        String baseFolder = EnvironmentUtils.getEnvironmentValue("upload_files.base-folder");
        String forecastFolder = EnvironmentUtils.getEnvironmentValue("import-files.forecast-pricing");


        // delete current Forecast file in folder
        List<String> fileNames = FileUtils.getAllFilesInFolder(baseFolder + "/" + forecastFolder);
        for (String fileName : fileNames) {
            File forecastFile = new File(baseFolder + "/" + forecastFolder + "/" + fileName);
            forecastFile.delete();
        }

        // save new Forecast file into disk
        Date uploadedTime = new Date();
        String strUploadedTime = (new SimpleDateFormat("ddMMyyyyHHmmss").format(uploadedTime));
        String encodedFileName = FileUtils.encoding(Objects.requireNonNull(multipartFile.getOriginalFilename())) + "_" + strUploadedTime + ".xlsx";

        File file = new File(baseFolder + forecastFolder + "/" + encodedFileName);
        if (file.createNewFile()) {
            log.info("File " + encodedFileName + " created");
            multipartFile.transferTo(file);

            fileUploadService.saveFileUpload(baseFolder + forecastFolder + "/" + encodedFileName, authentication);
        } else {
            log.info("Can not create new file: " + encodedFileName);
            throw new Exception("Can not create new file: " + encodedFileName);
        }
    }


    /**
     * Check the existence of Competitor Pricing in DB then update value
     */
    private CompetitorPricing checkExistAndUpdateCompetitorPricing(CompetitorPricing competitorPricing) {
        Optional<CompetitorPricing> dbCompetitorPricing = competitorPricingRepository.getCompetitorPricing(
                competitorPricing.getCountry().getCountryName(),
                competitorPricing.getClazz().getClazzName(),
                competitorPricing.getCategory(),
                competitorPricing.getSeries(),
                competitorPricing.getCompetitorName(),
                competitorPricing.getModel()
        );
        dbCompetitorPricing.ifPresent(pricing -> competitorPricing.setId(pricing.getId()));
        return competitorPricing;
    }

    public HashMap<String, Integer> getCompetitorColumnName(Row row) {
        HashMap<String, Integer> competitorColumnName = new HashMap<>();
        for (Cell cell : row)
            competitorColumnName.put(cell.getStringCellValue(), cell.getColumnIndex());

        return competitorColumnName;
    }


    /**
     * Checking existed Competitor Pricing and update new data from imported file
     */
    public void importIndicatorsFromFile(String filePath) throws Exception {
        InputStream is = new FileInputStream(filePath);
        XSSFWorkbook workbook = new XSSFWorkbook(is);

        HashMap<String, Integer> competitorColumnName = new HashMap<>();
        List<CompetitorPricing> competitorPricingList = new ArrayList<>();
        List<ForeCastValue> forecastValueList = importService.loadForecastForCompetitorPricingFromFile();
        if (forecastValueList == null) {
            throw new Exception("Missing Forecast Dynamic Pricing Excel file");
        }

        Sheet sheet = workbook.getSheetAt(0);
        List<String> titleColumnCurrent = new ArrayList<>();
        Row headerRow = sheet.getRow(0);
        for (int j = 0; j < CheckRequiredColumnUtils.COMPETITOR_REQUIRED_COLUMN.size(); j++) {
            Cell cell = headerRow.getCell(j);
            if(cell==null)
                continue;
            if (cell.getCellType() == CellType.STRING)
                titleColumnCurrent.add(cell.getStringCellValue());
            else
                titleColumnCurrent.add(String.valueOf(cell.getNumericCellValue()));

        }
        //Check format file competitor
        CheckRequiredColumnUtils.checkRequiredColumn(titleColumnCurrent, CheckRequiredColumnUtils.COMPETITOR_REQUIRED_COLUMN);

        for (Row row : sheet) {
            if (row.getRowNum() == 0) {
                competitorColumnName = getCompetitorColumnName(row);
            } else if (!row.getCell(competitorColumnName.get("Table Title"), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().isEmpty()) {
                List<CompetitorPricing> competitorPricings = importService.mapExcelDataIntoCompetitorObject(row, competitorColumnName);
                for (CompetitorPricing competitorPricing : competitorPricings) {
                    // if it has series -> assign ForeCastValue
                    if (!competitorPricing.getSeries().isEmpty()) {
                        String strRegion = competitorPricing.getCountry().getRegion().getRegionName();
                        String metaSeries = competitorPricing.getSeries().substring(1); // extract metaSeries from series

                        int currentYear = LocalDate.now().getYear();

                        ForeCastValue actualForeCast = importService.findForeCastValue(forecastValueList, strRegion, metaSeries, currentYear - 1);
                        ForeCastValue AOPFForeCast = importService.findForeCastValue(forecastValueList, strRegion, metaSeries, currentYear);
                        ForeCastValue LRFFForeCast = importService.findForeCastValue(forecastValueList, strRegion, metaSeries, currentYear + 1);

                        competitorPricing.setActual(actualForeCast == null ? 0 : actualForeCast.getQuantity());
                        competitorPricing.setAOPF(AOPFForeCast == null ? 0 : AOPFForeCast.getQuantity());
                        competitorPricing.setLRFF(LRFFForeCast == null ? 0 : LRFFForeCast.getQuantity());
                        competitorPricing.setPlant(LRFFForeCast == null ? "" : LRFFForeCast.getPlant());

                    }
                    competitorPricingList.add(checkExistAndUpdateCompetitorPricing(competitorPricing));
                }
            }
        }


        competitorPricingRepository.saveAll(competitorPricingList);
        importService.assigningCompetitorValues();
    }


}
