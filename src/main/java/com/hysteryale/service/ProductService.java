package com.hysteryale.service;

import com.hysteryale.model.Product;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.repository.ProductRepository;
import com.hysteryale.utils.ConvertDataFilterUtil;
import com.hysteryale.utils.EnvironmentUtils;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.text.ParseException;
import java.util.*;


@Service
@Slf4j
public class ProductService extends BasedService {
    @Resource
    ProductRepository productRepository;

    private final HashMap<String, Integer> COLUMNS = new HashMap<>();

    public void assignColumnNames(Row row) {
        for (int i = 0; i < 35; i++) {
            String columnName = row.getCell(i).getStringCellValue();
            COLUMNS.put(columnName, i);
        }
    }

    public Product mapExcelSheetToProductDimension(Row row) throws IllegalAccessException {
        Product product = new Product();

        // brand
        String brand = row.getCell(COLUMNS.get("Brand")).getStringCellValue();
        product.setBrand(brand);

        // metaseries
        String metaSeries = row.getCell(COLUMNS.get("Metaseries")).getStringCellValue();
        product.setMetaSeries(metaSeries);

        // plant
        String plant = row.getCell(COLUMNS.get("Plant")).getStringCellValue();
        product.setPlant(plant);

        // Class
        String clazz = row.getCell(COLUMNS.get("Class_wBT")).getStringCellValue();
        product.setClazz(clazz);

        //Segment
        String segment = row.getCell(COLUMNS.get("Segment")).getStringCellValue();
        product.setSegment(segment);

        // modeCode
        String modelCode = row.getCell(COLUMNS.get("Model")).getStringCellValue();
        product.setModelCode(modelCode);

        // family
        String family = row.getCell(COLUMNS.get("Family_Name")).getStringCellValue();
        product.setFamily(family);

        // truckType
        String truckType = row.getCell(COLUMNS.get("Truck_Type")).getStringCellValue();
        product.setTruckType(truckType);

        return product;
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

        Sheet sheet = workbook.getSheet("Data");
        importProduct(sheet);
        updateStateImportFile(pathFile);

    }

    public void importProduct(String path) throws IOException, IllegalAccessException {
        InputStream is = new FileInputStream(path);
        XSSFWorkbook workbook = new XSSFWorkbook(is);

        Sheet sheet = workbook.getSheet("Data");
        importProduct(sheet);
    }

    private void importProduct(Sheet sheet) throws IllegalAccessException {
        for (Row row : sheet) {
            if (row.getRowNum() == 1)
                assignColumnNames(row);
            else if (row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getCellType() != CellType.BLANK
                    && row.getRowNum() >= 2) {

                Product newProduct = mapExcelSheetToProductDimension(row);
                if (!checkExist(newProduct))
                    productRepository.save(newProduct);
            }
        }
    }

    public boolean checkExist(Product product) {
        Optional<Product> productDimensionOptional = productRepository.findByModelCodeAndMetaSeries(product.getModelCode(), product.getMetaSeries());
        if (productDimensionOptional.isPresent())
            return true;
        return false;
    }

    /**
     * Get List of APAC Serial's Model for selecting filter
     */
    public List<Map<String, String>> getAllMetaSeries() {
        List<Map<String, String>> metaSeriesMap = new ArrayList<>();
        List<String> metaSeries = productRepository.getAllMetaSeries();
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
        List<String> plants = productRepository.getPlants();
        plants.sort(String::compareTo);
        for (String p : plants) {
            Map<String, String> pMap = new HashMap<>();
            pMap.put("value", p);

            plantListMap.add(pMap);
        }
        return plantListMap;
    }

    public Product getProductDimensionByModelCode(String modelCode) {
        Optional<Product> productDimensionOptional = productRepository.findByModelCode(modelCode);
        return productDimensionOptional.orElse(null);
    }


    public List<Map<String, String>> getAllClasses() {
        List<Map<String, String>> classMap = new ArrayList<>();
        List<String> classes = productRepository.getAllClass();
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
        List<String> segments = productRepository.getAllSegments();
        segments.sort(String::compareTo);
        for (String m : segments) {
            Map<String, String> mMap = new HashMap<>();
            mMap.put("value", m);
            segmentMap.add(mMap);
        }

        return segmentMap;
    }

    public String getModelFromMetaSeries(String metaSeries) {
        Optional<String> modelOptional = productRepository.getModelByMetaSeries(metaSeries);
        return modelOptional.orElse(null);
    }

    public Map<String, Object> getDataByFilter(FilterModel filters) throws ParseException {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> filterMap = ConvertDataFilterUtil.loadDataFilterIntoMap(filters);
        logInfo(filterMap.toString());
        // product filter by: plant, segment, brand, family, metaSeries
        List<Product> getData = productRepository.getDataByFilter(
                (String) filterMap.get("modelCodeFilter"), (List<String>) filterMap.get("plantFilter"),
                (List<String>) filterMap.get("metaSeriesFilter"), (List<String>) filterMap.get("classFilter"),
                (List<String>) filterMap.get("segmentFilter"), (List<String>) filterMap.get("brandFilter"),
                (List<String>) filterMap.get("familyFilter"), (Pageable) filterMap.get("pageable")
        );
        result.put("listData", getData);
        //Count data
        long countAll = productRepository.countAll(
                (String) filterMap.get("modelCode"), (List<String>) filterMap.get("plantFilter"),
                (List<String>) filterMap.get("metaSeriesFilter"), (List<String>) filterMap.get("classFilter"),
                (List<String>) filterMap.get("segmentFilter"), (List<String>) filterMap.get("brandFilter"),
                (List<String>) filterMap.get("truckType"), (List<String>) filterMap.get("familyFilter"));
        result.put("totalItems", countAll);

        return result;

    }

    public void updateImageAndDescription(String modelCode, String imagePath, String description) throws NotFoundException {
        Optional<Product> productOptional = productRepository.findByModelCode(modelCode);
        if (productOptional.isEmpty())
            throw new NotFoundException("No product found with modelCode: " + modelCode);

        Product product = productOptional.get();
        if (imagePath != null)
            product.setImage(imagePath);

        if (description != null)
            product.setDescription(description);

        productRepository.save(product);
    }

    public Product getProductDimensionDetail(String modelCode) throws NotFoundException {
        Optional<Product> productOptional = productRepository.getProductByModelCode(modelCode);
        if (productOptional.isEmpty())
            throw new NotFoundException("Not found Product with ModelCode " + modelCode);

        return productOptional.get();
    }

}
