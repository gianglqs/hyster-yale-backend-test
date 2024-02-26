package com.hysteryale.service;

import com.hysteryale.exception.MissingColumnException;
import com.hysteryale.model.AOPMargin;
import com.hysteryale.model.Region;
import com.hysteryale.repository.AOPMarginRepository;
import com.hysteryale.utils.EnvironmentUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class AOPMarginService extends BasedService {
    @Resource
    AOPMarginRepository aopMarginRepository;
    private final HashMap<String, Integer> AOP_MARGIN_COLUMNS = new HashMap<>();

    @Resource
    private RegionService regionService;

    public void getAOPMarginColumns(Row row) {
        for (int i = 0; i < 11; i++) {
            String columnName = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
            AOP_MARGIN_COLUMNS.put(columnName, i);
        }
        log.info("AOP Margin Columns: " + AOP_MARGIN_COLUMNS);
    }

    public AOPMargin mapExcelToAOPMargin(Row row) throws IllegalAccessException, MissingColumnException {
        AOPMargin aopMargin = new AOPMargin();

        if (AOP_MARGIN_COLUMNS.get("Region") != null) {
            String valueCellRegion = row.getCell(AOP_MARGIN_COLUMNS.get("Region")).getStringCellValue();
            Region region = regionService.getRegionByName(valueCellRegion);
            if (region != null) {
                aopMargin.setRegion(region);
            } else {
                logError("Not found Region: " + valueCellRegion);
                return null;
            }
        } else {
            throw new MissingColumnException("Missing column 'Region'!");
        }

        if (AOP_MARGIN_COLUMNS.get("Plant") != null) {
            aopMargin.setPlant(row.getCell(AOP_MARGIN_COLUMNS.get("Plant")).getStringCellValue());
        } else {
            throw new MissingColumnException("Missing column 'Plant'!");
        }

        if (AOP_MARGIN_COLUMNS.get("Series") != null) {
            String metaSeries = null;
            Cell metaSeriesCell = row.getCell(AOP_MARGIN_COLUMNS.get("Series"));
            if (metaSeriesCell.getCellType() == CellType.NUMERIC) {
                aopMargin.setMetaSeries(String.valueOf((int) metaSeriesCell.getNumericCellValue()));
            } else {
                aopMargin.setMetaSeries(row.getCell(AOP_MARGIN_COLUMNS.get("Series")).getStringCellValue());
            }
        } else {
            throw new MissingColumnException("Missing column 'Series'!");
        }

        if (AOP_MARGIN_COLUMNS.get("Margin % STD") != null) {
            aopMargin.setMarginSTD(row.getCell(AOP_MARGIN_COLUMNS.get("Margin % STD")).getNumericCellValue());
        } else {
            throw new MissingColumnException("Missing column 'Margin % STD'!");
        }

        if (AOP_MARGIN_COLUMNS.get("AOP DN USD") != null) {
            aopMargin.setDnUSD(row.getCell(AOP_MARGIN_COLUMNS.get("AOP DN USD")).getNumericCellValue());
        } else {
            throw new MissingColumnException("Missing column 'AOP DN USD'!");
        }

        if (AOP_MARGIN_COLUMNS.get("Description") != null) {
            if (row.getCell(AOP_MARGIN_COLUMNS.get("Description")) != null)
                aopMargin.setDescription(row.getCell(AOP_MARGIN_COLUMNS.get("Description")).getStringCellValue());
        } else {
            throw new MissingColumnException("Missing column 'Description'!");
        }

        return aopMargin;
    }

    public void importAOPMargin() throws IOException, IllegalAccessException, MissingColumnException {
        // Initialize folderPath and fileName
        String fileName = "2023 AOP DN and Margin%.xlsx";
        String baseFolder = EnvironmentUtils.getEnvironmentValue("import-files.base-folder");
        String folderPath = baseFolder + EnvironmentUtils.getEnvironmentValue("import-files.aopmargin");
        String pathFile = folderPath + "/" + fileName;
        //check file has been imported ?
        if (isImported(pathFile)) {
            logWarning("file '" + fileName + "' has been imported");
            return;
        }

        int year = getYearFromFileName(fileName);

        InputStream is = new FileInputStream(pathFile);

        XSSFWorkbook workbook = new XSSFWorkbook(is);


        Sheet aopMarginSheet = workbook.getSheetAt(0);

        for (Row row : aopMarginSheet) {
            if (row.getRowNum() == 2)
                getAOPMarginColumns(row);
            else if (row.getRowNum() > 2 && !row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().isEmpty()) {
                AOPMargin aopMargin = mapExcelToAOPMargin(row);
                if (aopMargin == null)
                    continue;

                aopMargin.setYear(year);

                Optional<AOPMargin> optionalAOPMargin = aopMarginRepository.findByRegionSeriesPlantAndYear(
                        aopMargin.getRegion(), aopMargin.getMetaSeries(), aopMargin.getPlant(), year);
                if (optionalAOPMargin.isEmpty()) {
                    aopMarginRepository.save(aopMargin);
                }
            }
        }
        updateStateImportFile(pathFile);
    }

    private int getYearFromFileName(String fileName) throws InvalidPropertiesFormatException {
        String yearRegex = ("\\b\\d{4}\\b");
        Matcher m = Pattern.compile(yearRegex).matcher(fileName);
        boolean matchFound = m.find();
        if (matchFound) {
            return Integer.parseInt(m.group());
        } else {
            throw new InvalidPropertiesFormatException("Could not extract Year from file name");
        }
    }

    public AOPMargin getAOPMargin(Region region, String series, String plant, LocalDate date) {
        int year = date.getYear();
        Optional<AOPMargin> aopMarginOptional = aopMarginRepository.findByRegionSeriesPlantAndYear(region, series.substring(1), plant, year);
        return aopMarginOptional.orElse(null);
    }

    public AOPMargin getAOPMargin(List<AOPMargin> aopMargins, Region region, String series, String plant, LocalDate date) {
        int year = date.getYear();
        for (AOPMargin aopMargin : aopMargins) {
            if (aopMargin.getPlant().equals(plant) && aopMargin.getMetaSeries().equals(series.substring(1))
                    && aopMargin.getYear() == year && aopMargin.getRegion().getRegionShortName().equals(region.getRegionShortName())) {
                return aopMargin;
            }
        }
        log.error("Not found AOPMargin with region: " + region.getRegionShortName() + ", metaSeries: " + series.substring(1) + ", plant: " + plant + ", year: " + year);
        return null;
    }

}
