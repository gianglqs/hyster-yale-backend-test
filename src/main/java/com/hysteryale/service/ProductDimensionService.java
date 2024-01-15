package com.hysteryale.service;

import com.hysteryale.model.ProductDimension;
import com.hysteryale.model.ProductDimension;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.repository.ProductDimensionRepository;
import com.hysteryale.repository.ProductDimensionRepository;
import com.hysteryale.utils.ConvertDataFilterUtil;
import com.hysteryale.utils.EnvironmentUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.swing.text.html.Option;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.*;


@Service
@Slf4j
public class ProductDimensionService extends BasedService {
    @Resource
    ProductDimensionRepository productDimensionRepository;

    private final HashMap<String, Integer> APAC_COLUMNS = new HashMap<>();

    public void getAPACColumnsName(Row row) {
        for (int i = 0; i < 35; i++) {
            String columnName = row.getCell(i).getStringCellValue();
            APAC_COLUMNS.put(columnName, i);
        }
    }

    public ProductDimension mapExcelSheetToProductDimension(Row row) throws IllegalAccessException {
        ProductDimension productDimension = new ProductDimension();
        Class<? extends ProductDimension> ProductDimensionClass = productDimension.getClass();
        Field[] fields = ProductDimensionClass.getDeclaredFields();

        for (Field field : fields) {
            // String key of column's name and capitalize first character
            String fieldName = field.getName();
            String hashMapKey = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            if (fieldName.equals("id"))
                continue;

            field.setAccessible(true);
            if (fieldName.equals("brand")) {
                field.set(productDimension, row.getCell(APAC_COLUMNS.get("Brand")).getStringCellValue());
            } else if (fieldName.equals("metaSeries")) {
                field.set(productDimension, row.getCell(APAC_COLUMNS.get("Metaseries")).getStringCellValue());
            } else if (fieldName.equals("plant")) {
                field.set(productDimension, row.getCell(APAC_COLUMNS.get("Plant")).getStringCellValue());
            } else if (fieldName.equals("clazz")) {
                field.set(productDimension, row.getCell(APAC_COLUMNS.get("Class_wBT")).getStringCellValue());
            } else if (fieldName.equals("segment")) {
                field.set(productDimension, row.getCell(APAC_COLUMNS.get("Segment")).getStringCellValue());
            } else if (fieldName.equals("model")) {
                field.set(productDimension, row.getCell(APAC_COLUMNS.get("Model")).getStringCellValue());
            }
        }
        return productDimension;
    }


    public void importProductDimension() throws IOException, IllegalAccessException {
        String baseFolder = EnvironmentUtils.getEnvironmentValue("import-files.base-folder");
        String folderPath = baseFolder + EnvironmentUtils.getEnvironmentValue("import-files.product-dimension");
        String fileName = "Product Fcst dimension 2023_02_24.xlsx";
        String pathFile = folderPath + "/" + fileName;
        //check file has been imported ?
        if (isImported(pathFile)) {
            logWarning("file '" + fileName + "' has been imported");
            return;
        }

        InputStream is = new FileInputStream(pathFile);
        XSSFWorkbook workbook = new XSSFWorkbook(is);

        Sheet orderSheet = workbook.getSheet("Data");
        for (Row row : orderSheet) {
            if (row.getRowNum() == 1)
                getAPACColumnsName(row);
            else if (row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getCellType() != CellType.BLANK
                    && row.getRowNum() >= 2) {

                ProductDimension newProductDimension = mapExcelSheetToProductDimension(row);
                if (!checkExist(newProductDimension))
                    productDimensionRepository.save(newProductDimension);
            }
        }
        updateStateImportFile(pathFile);

    }

    public boolean checkExist(ProductDimension productDimension) {
        Optional<ProductDimension> productDimensionOptional = productDimensionRepository.findByMetaSeries(productDimension.getMetaSeries());
        if (productDimensionOptional.isPresent())
            return true;
        return false;
    }

    /**
     * Get List of APAC Serial's Model for selecting filter
     */
    public List<Map<String, String>> getAllMetaSeries() {
        List<Map<String, String>> metaSeriesMap = new ArrayList<>();
        List<String> metaSeries = productDimensionRepository.getAllMetaSeries();
        metaSeries.sort(String::compareTo);
        for (String m : metaSeries) {
            Map<String, String> mMap = new HashMap<>();
            mMap.put("value", m);
            metaSeriesMap.add(mMap);
        }

        return metaSeriesMap;
    }

    /**
     * Get list of distinct Plants
     */
    public List<Map<String, String>> getAllPlants() {
        List<Map<String, String>> plantListMap = new ArrayList<>();
        List<String> plants = productDimensionRepository.getPlants();
        plants.sort(String::compareTo);
        for (String p : plants) {
            Map<String, String> pMap = new HashMap<>();
            pMap.put("value", p);

            plantListMap.add(pMap);
        }
        return plantListMap;
    }

    public ProductDimension getProductDimensionByMetaseries(String series) {
        Optional<ProductDimension> productDimensionOptional = productDimensionRepository.findByMetaSeries(series.substring(1));
        return productDimensionOptional.orElse(null);
    }


    public List<Map<String, String>> getAllClasses() {
        List<Map<String, String>> classMap = new ArrayList<>();
        List<String> classes = productDimensionRepository.getAllClass();
        classes.sort(String::compareTo);
        for (String m : classes) {
            Map<String, String> mMap = new HashMap<>();
            mMap.put("value", m);
            classMap.add(mMap);
        }
        return classMap;
    }

    public List<Map<String, String>> getAllSegments() {
        List<Map<String, String>> segmentMap = new ArrayList<>();
        List<String> segments = productDimensionRepository.getAllSegments();
        segments.sort(String::compareTo);
        for (String m : segments) {
            Map<String, String> mMap = new HashMap<>();
            mMap.put("value", m);
            segmentMap.add(mMap);
        }

        return segmentMap;
    }

    public String getModelFromMetaSeries(String metaSeries) {
        Optional<String> modelOptional = productDimensionRepository.getModelByMetaSeries(metaSeries);
        logInfo("mdoel" + modelOptional.get());
        return modelOptional.orElse(null);
    }

    public Map<String, Object> getDataByFilter(FilterModel filters) throws ParseException {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> filterMap = ConvertDataFilterUtil.loadDataFilterIntoMap(filters);
        // product filter by: plant, segment, brand, family, metaSeries
        List<ProductDimension> getData = productDimensionRepository.getDataByFilter(
                (String) filterMap.get("modelCode"), (List<String>) filterMap.get("plantFilter"),
                (List<String>) filterMap.get("metaSeriesFilter"), (List<String>) filterMap.get("classFilter"),
                (List<String>) filterMap.get("segmentFilter"), (List<String>) filterMap.get("brandFilter"),
                (List<String>) filterMap.get("familyFilter")
        );
        result.put("listData", getData);
        //Count data
        long countAll = productDimensionRepository.countAll(
                (String) filterMap.get("modelCode"), (List<String>) filterMap.get("plantFilter"),
                (List<String>) filterMap.get("metaSeriesFilter"), (List<String>) filterMap.get("classFilter"),
                (List<String>) filterMap.get("segmentFilter"), (List<String>) filterMap.get("brandFilter"),
                (List<String>) filterMap.get("familyFilter"));
        result.put("totalItems", countAll);

        return result;

    }
}
