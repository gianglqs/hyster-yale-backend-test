package com.hysteryale.service;

import com.hysteryale.exception.MissingColumnException;
import com.hysteryale.model.AOPMargin;
import com.hysteryale.model.Region;
import com.hysteryale.repository.AOPMarginRepository;
import com.hysteryale.repository.RegionRepository;
import com.hysteryale.repository.upload.FileUploadRepository;
import com.hysteryale.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class AOPMarginService extends BasedService {
    @Resource
    AOPMarginRepository aopMarginRepository;
    private final HashMap<String, Integer> AOP_MARGIN_COLUMNS = new HashMap<>();

    @Resource
    private FileUploadService fileUploadService;

    @Resource
    private RegionService regionService;

    @Resource
    private RegionRepository regionRepository;

    @Resource
    FileUploadRepository fileUploadRepository;

    public void getAOPMarginColumns(Row row) {
        for (int i = 0; i < 20; i++) {
            String columnName = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
            AOP_MARGIN_COLUMNS.put(columnName, i);
        }
        log.info("AOP Margin Columns: " + AOP_MARGIN_COLUMNS);
    }

    public AOPMargin mapExcelToAOPMargin(Row row) throws IllegalAccessException, MissingColumnException {
        AOPMargin aopMargin = new AOPMargin();


        String valueCellRegion = row.getCell(AOP_MARGIN_COLUMNS.get("Region")).getStringCellValue();
        Region region = regionService.getRegionByName(valueCellRegion);
        if (region != null) {
            aopMargin.setRegion(region);
        } else {
            logError("Not found Region: " + valueCellRegion);
            return null;
        }


        aopMargin.setPlant(row.getCell(AOP_MARGIN_COLUMNS.get("Plant")).getStringCellValue());


        String metaSeries = null;
        Cell metaSeriesCell = row.getCell(AOP_MARGIN_COLUMNS.get("Series"));
        if (metaSeriesCell.getCellType() == CellType.NUMERIC) {
            aopMargin.setMetaSeries(String.valueOf((int) metaSeriesCell.getNumericCellValue()));
        } else {
            aopMargin.setMetaSeries(row.getCell(AOP_MARGIN_COLUMNS.get("Series")).getStringCellValue());
        }


        aopMargin.setMarginSTD(row.getCell(AOP_MARGIN_COLUMNS.get("Margin % STD")).getNumericCellValue());


        aopMargin.setDnUSD(row.getCell(AOP_MARGIN_COLUMNS.get("AOP DN USD")).getNumericCellValue());


        if (row.getCell(AOP_MARGIN_COLUMNS.get("Description")) != null)
            aopMargin.setDescription(row.getCell(AOP_MARGIN_COLUMNS.get("Description")).getStringCellValue());


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

    public void importAOPMargin(MultipartFile file, Authentication authentication) throws Exception {
        // b1 take out the first 10 rows
        // b2 check rows are header, conditions: cellType only STRING or BLANK
        // b3 Map column name with column index
        // b4 use regex to get required column and compare ->  throws Exception if missing column
        // b5 read body

        String baseFolder = EnvironmentUtils.getEnvironmentValue("public-folder");
        String baseFolderUploaded = EnvironmentUtils.getEnvironmentValue("upload_files.base-folder");
        String targetFolder = EnvironmentUtils.getEnvironmentValue("upload_files.product");
        String fileNameEncoded = fileUploadService.saveFileUploaded(file, authentication, targetFolder, FileUtils.EXCEL_FILE_EXTENSION, ModelUtil.AOP_MARGIN);
        String fileUUID = fileUploadRepository.getFileUUIDByFileName(fileNameEncoded);
        String filePath = baseFolder + baseFolderUploaded + targetFolder + fileNameEncoded;
        if (!FileUtils.isExcelFile(filePath)) {
            fileUploadService.handleUpdatedFailure(fileUUID, "Uploaded file is not an Excel file");
            throw new Exception("Imported file is not Excel");
        }


        try {
            String fileName = file.getOriginalFilename();
            int year = DateUtils.extractYear(fileName);
            InputStream is = new FileInputStream(filePath);
            importAOPMarginFromGUM(is, year, fileUUID);

        } catch (Exception e) {
            fileUploadService.handleUpdatedFailure(fileUUID, e.getMessage());
            throw e;
        }

        fileUploadService.handleUpdatedSuccessfully(fileNameEncoded);
    }

    private void importAOPMarginFromGUM(InputStream is, int year, String fileUUID) throws IOException, MissingColumnException {
        XSSFWorkbook workbook = new XSSFWorkbook(is);

        List<AOPMargin> aopMarginListInDB = aopMarginRepository.findAll();
        for (Sheet sheet : workbook) {
            if (sheet.getSheetName().toLowerCase().contains("aop") &&
                    sheet.getSheetName().toLowerCase().contains("dn") &&
                    sheet.getSheetName().toLowerCase().contains("margin") &&
                    sheet.getSheetName().toLowerCase().contains("%") &&
                    sheet.getSheetName().toLowerCase().contains(String.valueOf(year))) {

                List<AOPMargin> newAOPMarginList = new ArrayList<>();
                int startIndexBodyRow = getStartIndexBodyRow(sheet);

                List<String> listCurrentColumn = new ArrayList<>(AOP_MARGIN_COLUMNS.keySet());
                // check required columns
                CheckRequiredColumnUtils.checkRequiredColumn(listCurrentColumn, CheckRequiredColumnUtils.AOP_MARGIN_REQUIRED_COLUMN, fileUUID);

                //check column 'STD Margin %'
                boolean hasMarginSTD = false;
                for (String columnName : listCurrentColumn) {
                    if (columnName.toLowerCase().contains("std")
                            && columnName.toLowerCase().contains("margin")
                            && columnName.toLowerCase().contains("%")) {
                        hasMarginSTD = true;
                        break;
                    }
                }
                if (!hasMarginSTD)
                    throw new MissingColumnException("Margin STD %", fileUUID);


                for (Row row : sheet) {
                    if (row.getRowNum() >= startIndexBodyRow) {
                        AOPMargin aopMargin = mapExcelGUMToAOPMargin(row);
                        if (aopMargin == null)
                            continue;

                        aopMargin.setYear(year);
                        newAOPMarginList.add(aopMargin);
                    }
                }

                // save or update
                saveOrUpdateAOPMargin(aopMarginListInDB, newAOPMarginList);
            }
        }
    }

    private void saveOrUpdateAOPMargin(List<AOPMargin> aopMarginListInDB, List<AOPMargin> newAOPMarginList) {
        List<AOPMargin> newAOPMarginNotInDB = new ArrayList<>();
        for (AOPMargin newAOPMargin : newAOPMarginList) {
            for (AOPMargin aopMarginInDB : aopMarginListInDB) {
                if (newAOPMargin.equals(aopMarginInDB)) {
                    //update
                    aopMarginInDB.setMarginSTD(aopMarginInDB.getMarginSTD());
                    continue;
                }
                newAOPMarginNotInDB.add(newAOPMargin);
            }
        }
        aopMarginRepository.saveAllAndFlush(aopMarginListInDB);
        aopMarginRepository.saveAllAndFlush(newAOPMarginNotInDB);
    }

    private AOPMargin mapExcelGUMToAOPMargin(Row row) {
        AOPMargin aopMargin = new AOPMargin();

        // region
        String regionName = row.getCell(AOP_MARGIN_COLUMNS.get("Region")).getStringCellValue();
        Region region = regionRepository.getRegionByName(regionName);
        if (region == null) {
            log.error("Could not find Region with region name: " + regionName);
            return null;
        }
        aopMargin.setRegion(region);

        //series
        Cell seriesCell = row.getCell(AOP_MARGIN_COLUMNS.get("Region"));
        if (seriesCell.getCellType() != CellType.STRING) {
            log.error("Series is not STRING at row " + row.getRowNum());
            return null;
        }
        aopMargin.setMetaSeries(seriesCell.getStringCellValue());

        //series
        Cell plantCell = row.getCell(AOP_MARGIN_COLUMNS.get("Plant"));
        if (plantCell.getCellType() != CellType.STRING) {
            log.error("Series is not STRING at row " + row.getRowNum());
            return null;
        }
        aopMargin.setPlant(plantCell.getStringCellValue());

        // margin STD
        for (String columnName : AOP_MARGIN_COLUMNS.keySet()) {
            if (columnName.toLowerCase().contains("margin") && columnName.contains("%") && columnName.toLowerCase().contains("std")) {
                aopMargin.setMarginSTD(row.getCell(AOP_MARGIN_COLUMNS.get(columnName)).getNumericCellValue());
                break;
            }
        }

        return aopMargin;
    }

    private int getStartIndexBodyRow(Sheet sheet) {
        int totalHeaderRow = 0;
        loopRow:
        for (Row row : sheet) {
            for (Cell cell : row) {
                if (cell.getCellType() == CellType.NUMERIC)
                    continue loopRow;
            }
            totalHeaderRow++;
            getAOPMarginColumns(row);
        }
        return totalHeaderRow;
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
