package com.hysteryale.service;

import com.hysteryale.model.AOPMargin;
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
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AOPMarginService extends BasedService {
    @Resource
    AOPMarginRepository aopMarginRepository;
    private final HashMap<String, Integer> AOP_MARGIN_COLUMNS = new HashMap<>();

    public void getAOPMarginColumns(Row row) {
        for (int i = 0; i < 11; i++) {
            String columnName = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
            AOP_MARGIN_COLUMNS.put(columnName, i);
        }
        log.info("AOP Margin Columns: " + AOP_MARGIN_COLUMNS);
    }

    public AOPMargin mapExcelToAOPMargin(Row row) throws IllegalAccessException {
        AOPMargin aopMargin = new AOPMargin();
        Class<? extends AOPMargin> aopMarginClass = aopMargin.getClass();
        Field[] fields = aopMarginClass.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);

            String fieldName = field.getName();
            switch (fieldName) {
                case "regionSeriesPlant":
                    String regionSeriesPlant = row.getCell(AOP_MARGIN_COLUMNS.get("Region & M series")).getStringCellValue();
                    regionSeriesPlant += row.getCell(AOP_MARGIN_COLUMNS.get("Plant")).getStringCellValue();
                    field.set(aopMargin, regionSeriesPlant);
                    break;
                case "description":
                    field.set(aopMargin, row.getCell(AOP_MARGIN_COLUMNS.get("Description"), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue());
                    break;
                case "dnUSD":
                    field.set(aopMargin, row.getCell(AOP_MARGIN_COLUMNS.get("AOP DN USD")).getNumericCellValue());
                    break;
                case "marginSTD":
                    field.set(aopMargin, row.getCell(AOP_MARGIN_COLUMNS.get("Margin % STD")).getNumericCellValue());
                    break;
                case "plant":
                    field.set(aopMargin, row.getCell(AOP_MARGIN_COLUMNS.get("Plant")).getStringCellValue());
                    break;
                case "region":
                    String valueCellRegion = row.getCell(AOP_MARGIN_COLUMNS.get("Region")).getStringCellValue();
                    logInfo("Region  "+valueCellRegion.split(" ")[0]);
                    field.set(aopMargin, valueCellRegion.split(" ")[0]);

                    break;
                case "series":
                    Cell cell = row.getCell(AOP_MARGIN_COLUMNS.get("Series"));
                    if (cell.getCellType() == CellType.STRING) {
                        field.set(aopMargin, cell.getStringCellValue());
                    } else if (cell.getCellType() == CellType.NUMERIC) {
                        field.set(aopMargin, String.valueOf((int) cell.getNumericCellValue()));
                    }
                    break;
            }
        }

        return aopMargin;
    }

//    public void importAOPMargin() throws IOException, IllegalAccessException {
//        // Initialize folderPath and fileName
//        String fileName = "2023 AOP DN and Margin%.xlsx";
//        String baseFolder = EnvironmentUtils.getEnvironmentValue("import-files.base-folder");
//        String folderPath = baseFolder + EnvironmentUtils.getEnvironmentValue("import-files.aopmargin");
//        String pathFile = folderPath + "/" + fileName;
//        //check file has been imported ?
//        if (isImported(pathFile)) {
//            logWarning("file '" + fileName + "' has been imported");
//            return;
//        }
//
//        InputStream is = new FileInputStream(pathFile);
//
//        XSSFWorkbook workbook = new XSSFWorkbook(is);
//
//
//        Sheet aopMarginSheet = workbook.getSheetAt(0);
//
//        for (Row row : aopMarginSheet) {
//            if (row.getRowNum() == 2)
//                getAOPMarginColumns(row);
//            else if (row.getRowNum() > 2 && !row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().isEmpty()) {
//                AOPMargin aopMargin = mapExcelToAOPMargin(row);
//                //TODO need to get year from file name, not hardcode as I did below
//                aopMargin.setYear(2023);
//                Optional<AOPMargin> optionalAOPMargin = aopMarginRepository.findByRegionSeriesPlant(aopMargin.getRegionSeriesPlant());
//                if (!optionalAOPMargin.isPresent()) {
//                    aopMarginRepository.save(aopMargin);
//                }
//            }
//        }
//        updateStateImportFile(pathFile);
//    }

    public Double getAOPMargin(String series, String region, String plant) {
        List<AOPMargin> aopMarginList = aopMarginRepository.findByRegionPlantSeries(series.substring(1),region, plant);
        return aopMarginList.isEmpty() ? null : aopMarginList.get(0).getMarginSTD();
    }

}
