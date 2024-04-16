/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.service.impl;

import com.hysteryale.exception.BlankSheetException;
import com.hysteryale.exception.MissingSheetException;
import com.hysteryale.model.Product;
import com.hysteryale.model.ResidualValue;
import com.hysteryale.model.embedId.ResidualValueId;
import com.hysteryale.model.enums.ImportFailureType;
import com.hysteryale.model.enums.ModelTypeEnum;
import com.hysteryale.model.importFailure.ImportFailure;
import com.hysteryale.repository.ProductRepository;
import com.hysteryale.repository.ResidualValueRepository;
import com.hysteryale.repository.importFailure.ImportFailureRepository;
import com.hysteryale.repository.upload.FileUploadRepository;
import com.hysteryale.service.ImportFailureService;
import com.hysteryale.service.ProductService;
import com.hysteryale.service.ResidualValueService;
import com.hysteryale.utils.*;
import lombok.Getter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ResidualValueServiceImp implements ResidualValueService {

    @Resource
    private ProductRepository productRepository;

    @Getter
    private final Map<String, Integer> RESIDUAL_COLUMN = new HashMap<>();

    @Resource
    private ProductService productService;

    @Resource
    private ImportFailureService importFailureService;

    @Resource
    private ResidualValueRepository residualValueRepository;

    @Resource
    private ImportFailureRepository importFailureRepository;

    @Resource
    LocaleUtils localeUtils;

    @Resource
    FileUploadRepository fileUploadRepository;

    @Override
    public List<ImportFailure> importResidualValue(String filePath, String fileUUID, int year) throws IOException, MissingSheetException, BlankSheetException {

        InputStream is = new FileInputStream(filePath);
        XSSFWorkbook workbook = new XSSFWorkbook(is);

        String sheetName = CheckRequiredColumnUtils.RESIDUAL_VALUE_REQUIRED_SHEET;
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null)
            throw new MissingSheetException(sheetName, fileUUID);

        if (sheet.getLastRowNum() <= 0)
            throw new BlankSheetException(sheetName, fileUUID);

        List<Product> products = productRepository.findAll();

        Map<String, Integer> indexRangeOfNeedDataMap = getIndexRangeOfNeedData(sheet);

        mapHeaderColumn(sheet, indexRangeOfNeedDataMap.get("headerRowNum"));

        List<ResidualValue> residualValues = new ArrayList<>();
        List<ImportFailure> importFailures = new ArrayList<>();
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

        List<CellRangeAddress> listModelTypeRangeCell = getListModelTypeCellRangeAddress(
                sheet,
                indexRangeOfNeedDataMap.get("firstRow"),
                indexRangeOfNeedDataMap.get("lastRow"),
                indexRangeOfNeedDataMap.get("lastColumnHeaderPercent"));

        for (CellRangeAddress cellAddress : listModelTypeRangeCell) {
            int firstRow = cellAddress.getFirstRow();
            int firstColumn = cellAddress.getFirstColumn();
            Row row = sheet.getRow(firstRow);
            String modelType = row.getCell(firstColumn).getStringCellValue();

            for (int i = cellAddress.getFirstRow(); i <= cellAddress.getLastRow(); i++) {
                mapDataExcelIntoResidualValue(sheet.getRow(i), products, importFailures, evaluator, residualValues, modelType, year);
            }
        }
        residualValueRepository.saveAll(residualValues);

        importFailureService.setFileUUIDForListImportFailure(importFailures, fileUUID);
        importFailureRepository.saveAll(importFailures);
        productRepository.saveAll(products);
        localeUtils.logStatusImportComplete(importFailures, ModelTypeEnum.RESIDUAL_VALUE.getValue());
        return importFailures;
    }


    @Override
    public Map<String, Object> getDataByFilter(String modelCode) {
        Map<String, Object> result = new HashMap<>();

        List<ResidualValue> residualValueList = residualValueRepository.getResidualValueByModelCode(modelCode);

        // remove duplicate modelCode
        Set<ResidualValue> residualValueSet = new HashSet<>(residualValueList);
        result.put("listResidualValue", residualValueSet);

        // last update time
        Optional<LocalDateTime> lastUpdateTimeOptional = fileUploadRepository.getLatestUpdatedTimeByModelType(ModelTypeEnum.RESIDUAL_VALUE.getValue());
        String latestUpdatedTime = null;
        if (lastUpdateTimeOptional.isPresent()) {
            latestUpdatedTime = DateUtils.convertLocalDateTimeToString(lastUpdateTimeOptional.get());
        }
        result.put("latestUpdatedTime", latestUpdatedTime);
        result.put("serverTimeZone", TimeZone.getDefault().getID());
        return result;
    }

    public void mapDataExcelIntoResidualValue(Row row, List<Product> prepareProducts, List<ImportFailure> importFailures, FormulaEvaluator evaluator, List<ResidualValue> residualValues, String modelType, int year) {

        String hysterModel = row.getCell(RESIDUAL_COLUMN.get("Hyster Model")).getStringCellValue();
        String yaleModel = row.getCell(RESIDUAL_COLUMN.get("Yale Model")).getStringCellValue();

        List<Product> hysterModelProducts = productService.getProductByModelCode(prepareProducts, hysterModel);
        List<Product> yaleModelProducts = productService.getProductByModelCode(prepareProducts, yaleModel);

        if (hysterModelProducts.isEmpty()) {
            importFailureService.addIntoListImportFailure(importFailures, String.valueOf(row.getRowNum() + 1), "not-find-product-with-modelCode", hysterModel, ImportFailureType.ERROR);
        } else {
            residualValues.addAll(mapResidualWithProduct(hysterModelProducts, row, evaluator, importFailures, modelType, year));
        }

        if (yaleModelProducts.isEmpty()) {
            importFailureService.addIntoListImportFailure(importFailures, String.valueOf(row.getRowNum() + 1), "not-find-product-with-modelCode", hysterModel, ImportFailureType.ERROR);
        } else {
            residualValues.addAll(mapResidualWithProduct(yaleModelProducts, row, evaluator, importFailures, modelType, year));
        }
    }

    public List<CellRangeAddress> getListModelTypeCellRangeAddress(Sheet sheet, int firstRowOfNeedData, int lastRowOfNeedData, int lastColumnHeaderPercent) {
        List<CellRangeAddress> listCellRangeAddress = new ArrayList<>();

        int indexColumnHeaderModelType = lastColumnHeaderPercent + 1;

        for (CellRangeAddress cellAddress : sheet.getMergedRegions()) {
            if (cellAddress.getFirstColumn() == indexColumnHeaderModelType
                    && cellAddress.getLastColumn() == indexColumnHeaderModelType
                    && cellAddress.getFirstRow() >= firstRowOfNeedData
                    && cellAddress.getLastRow() <= lastRowOfNeedData) {
                listCellRangeAddress.add(cellAddress);
            }
        }
        return listCellRangeAddress;
    }

    public List<ResidualValue> mapResidualWithProduct(List<Product> products, Row row, FormulaEvaluator evaluator, List<ImportFailure> importFailures, String modelType, int year) {
        List<ResidualValue> residualValues = new ArrayList<>();
        for (Product product : products) {
            for (String headerColumnName : RESIDUAL_COLUMN.keySet()) {
                Integer hours = StringUtils.isNumber(headerColumnName);
                if (hours != null) {
                    Cell residualValueCell = row.getCell(RESIDUAL_COLUMN.get(headerColumnName));
                    double residualValuePercent = 0;
                    switch (residualValueCell.getCellType()) {
                        case NUMERIC:
                            residualValuePercent = residualValueCell.getNumericCellValue();
                            break;
                        case FORMULA:
                            evaluator.evaluateFormulaCell(residualValueCell);
                            residualValuePercent = residualValueCell.getNumericCellValue();
                            break;
                        default:
                            String reasonValue = (row.getRowNum() + 1) + ":" + (RESIDUAL_COLUMN.get(headerColumnName) + 1);
                            importFailureService.addIntoListImportFailure(importFailures, String.valueOf(row.getRowNum() + 1), "incorrect-format-at-cell", reasonValue, ImportFailureType.ERROR);
                    }

                    ResidualValue residualValue = new ResidualValue();
                    ResidualValueId id = new ResidualValueId();
                    id.setProduct(product);
                    id.setHours(hours);
                    residualValue.setId(id);
                    product.setModelType(modelType);
                    residualValue.setResidualValuePercent(residualValuePercent);
                    residualValue.setYears(year);
                    residualValues.add(residualValue);

                }
            }
        }

        return residualValues;
    }

    /**
     * {@link }
     */
    public void mapHeaderColumn(Sheet sheet, int headerNumRow) {
        Row headerHoursRow = sheet.getRow(headerNumRow);

        for (Cell cell : headerHoursRow) {
            if (cell.getCellType() == CellType.NUMERIC)
                RESIDUAL_COLUMN.put(String.valueOf((int) cell.getNumericCellValue()), cell.getColumnIndex());
            if (cell.getCellType() == CellType.STRING)
                RESIDUAL_COLUMN.put(cell.getStringCellValue(), cell.getColumnIndex());
        }

    }


    public Map<String, Integer> getIndexRangeOfNeedData(Sheet sheet) {
        Map<String, Integer> result = new HashMap<>();
        int headerRowNum = 0;
        loop:
        for (Row row : sheet) {
            for (Cell cell : row) {
                if (cell.getCellType() == CellType.STRING && cell.getStringCellValue().equals("Hours")) {
                    headerRowNum = row.getRowNum() + 2;
                    break loop;
                }
            }
        }

        int firstRowGetData = 0;
        int lastRowGetData = 0;
        int lastColumnOfHeaderPercent = 0;


        for (CellRangeAddress mergedRegion : sheet.getMergedRegions()) {
            int firstRow = mergedRegion.getFirstRow();
            int firstColumn = mergedRegion.getFirstColumn();
            Row row = sheet.getRow(firstRow);
            Cell cell = row.getCell(firstColumn);

            if (cell.getStringCellValue().contains("%")) {
                firstRowGetData = firstRow;
                lastRowGetData = mergedRegion.getLastRow();
                lastColumnOfHeaderPercent = mergedRegion.getLastColumn();
            }
        }
        int indexEndOfNeedDataRowWithoutHeader = firstRowGetData + 5;

        result.put("firstRow", indexEndOfNeedDataRowWithoutHeader);
        result.put("lastRow", lastRowGetData);
        result.put("headerRowNum", headerRowNum);
        result.put("lastColumnHeaderPercent", lastColumnOfHeaderPercent);

        return result;
    }

}