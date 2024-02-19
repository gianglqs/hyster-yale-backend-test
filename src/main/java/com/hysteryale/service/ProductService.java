package com.hysteryale.service;

import com.hysteryale.model.Product;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.repository.ProductRepository;
import com.hysteryale.utils.ConvertDataFilterUtil;
import com.hysteryale.utils.EnvironmentUtils;
import com.hysteryale.utils.FileUtils;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.*;


@Service
@Slf4j
public class ProductService extends BasedService {
    @Resource
    ProductRepository productRepository;

    @Resource
    FileUploadService fileUploadService;

    private final HashMap<String, Integer> COLUMNS = new HashMap<>();

    public void assignColumnNames(Row row) {
        COLUMNS.clear();
        for (int i = 0; i < 30; i++) {
            if (row.getCell(i) == null || row.getCell(i).getCellType() != CellType.STRING)
                continue;
            String columnName = row.getCell(i).getStringCellValue();
            if (COLUMNS.get(columnName) == null) {
                COLUMNS.put(columnName, i);
            } else {
                COLUMNS.put(columnName + "_Y", i);
            }
        }
    }

    public Product mapExcelSheetToProductDimension(Row row) {
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

    private void importProduct(Sheet sheet) {
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

    public Product getProductDimensionDetail(String modelCode, String metaSeries) throws NotFoundException {
        Optional<Product> productOptional = productRepository.findByModelCodeAndMetaSeries(modelCode, metaSeries);
        if (productOptional.isEmpty())
            throw new NotFoundException("Not found Product with ModelCode " + modelCode);

        return productOptional.get();
    }

    /**
     * Extract Product (Model Code) from Part (in power bi files) --- will be removed later
     */
    public void extractProductFromPart() throws IOException {
        String baseFolder = EnvironmentUtils.getEnvironmentValue("import-files.base-folder");
        String folderPath = baseFolder + EnvironmentUtils.getEnvironmentValue("import-files.bi-download");
        List<String> files = FileUtils.getAllFilesInFolder(folderPath);

        logInfo("Files: " + files);

        for (String fileName : files) {
            String filePath = folderPath + "/" + fileName;
            //check file has been imported ?
            if (isImported(filePath)) {
                logWarning("file '" + fileName + "' has been imported");
                continue;
            }
            log.info("Importing " + fileName);
            importProductFromPowerBi(filePath);
        }
    }

    private HashMap<String, Integer> getPowerBiColumnsName(Row row) {
        HashMap<String, Integer> columns = new HashMap<>();
        for (Cell cell : row) {
            String columnsName = cell.getStringCellValue();
            columns.put(columnsName, cell.getColumnIndex());
        }
        return columns;
    }

    private boolean checkDuplicateModelCode(String modelCode, List<Product> productList) {
        for (Product p : productList) {
            if (Objects.equals(p.getModelCode(), modelCode))
                return true;
        }
        return false;
    }

    private void importProductFromPowerBi(String filePath) throws IOException {
        InputStream is = new FileInputStream(filePath);
        Workbook workbook = new XSSFWorkbook(is);
        Sheet sheet = workbook.getSheet("Export");

        List<Product> productForSaving = new ArrayList<>();

        HashMap<String, Integer> columns = getPowerBiColumnsName(sheet.getRow(0));
        for (Row row : sheet) {
            if (!row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().isEmpty() && row.getRowNum() > 0) {
                String modelCode = row.getCell(columns.get("Model")).getStringCellValue();
                Optional<Product> optionalProduct = productRepository.findByModelCode(modelCode);
                if (optionalProduct.isEmpty() && checkDuplicateModelCode(modelCode, productForSaving)) {
                    String series = row.getCell(columns.get("Series")).getStringCellValue();
                    Product seriesInformation = productRepository.getProductByMetaSeries(series.substring(1));
                    if (seriesInformation != null) {
                        productForSaving.add(new Product(
                                modelCode,
                                seriesInformation.getMetaSeries(),
                                seriesInformation.getBrand(),
                                seriesInformation.getPlant(),
                                seriesInformation.getClazz(),
                                seriesInformation.getSegment(),
                                seriesInformation.getFamily(),
                                seriesInformation.getTruckType(),
                                seriesInformation.getImage(),
                                seriesInformation.getDescription())
                        );
                    }
                }
            }
        }
        productRepository.saveAll(productForSaving);
    }


    public List<Product> mappedFromAPACFile(Row row) {
        List<Product> listProduct = new ArrayList<>();

        String plant = row.getCell(COLUMNS.get("Plant")).getStringCellValue();
        String clazz = row.getCell(COLUMNS.get("Class")).getStringCellValue();

        // Hyster
        Product hysterProduct = new Product();
        Cell hysterSeriesCell = row.getCell(COLUMNS.get("Hyster"));
        Cell hysterModelCell = row.getCell(COLUMNS.get("Model"));

        if (hysterSeriesCell.getCellType() == CellType.STRING && hysterModelCell.getCellType() == CellType.STRING) {
            String hysterSeries = hysterSeriesCell.getStringCellValue();
            String hysterModel = hysterModelCell.getStringCellValue();


            if (!hysterModel.equals("NA") && !hysterSeries.equals("NA")) {
                hysterProduct.setModelCode(hysterModel);
                hysterProduct.setMetaSeries(hysterSeries);
                hysterProduct.setPlant(plant);
                hysterProduct.setClazz(clazz);
                hysterProduct.setBrand("H");
                listProduct.add(hysterProduct);
            }
        }

        // Yale
        Product yaleProduct = new Product();
        Cell yaleSeriesCell = row.getCell(COLUMNS.get("Yale"));
        Cell yaleModelCell = row.getCell(COLUMNS.get("Model_Y"));

        if (yaleSeriesCell.getCellType() == CellType.STRING && yaleModelCell.getCellType() == CellType.STRING) {
            String yaleSeries = yaleSeriesCell.getStringCellValue();
            String yaleModel = yaleModelCell.getStringCellValue();


            if (!yaleModel.equals("NA") && !yaleSeries.equals("NA")) {
                yaleProduct.setModelCode(yaleModel);
                yaleProduct.setMetaSeries(yaleSeries);
                yaleProduct.setPlant(plant);
                yaleProduct.setClazz(clazz);
                yaleProduct.setBrand("Y");
                listProduct.add(yaleProduct);
            }
        }


        return listProduct;
    }

    public void importProduct(List<MultipartFile> fileList, Authentication authentication) throws Exception {
        String baseFolder = EnvironmentUtils.getEnvironmentValue("upload_files.base-folder");
        String excelFileExtension = FileUtils.EXCEL_FILE_EXTENSION;
        for (MultipartFile file : fileList) {
            String pathFileUploaded = fileUploadService.saveFileUploaded(file, authentication, baseFolder, excelFileExtension);
            try {
                if (file.getOriginalFilename().contains("APAC")) {
                    importBaseProduct(pathFileUploaded);
                }
                if (file.getOriginalFilename().contains("dimension")) {
                    importDimensionProduct(pathFileUploaded);
                }
            } catch (Exception e) {
                fileUploadService.deleteFileInDisk(pathFileUploaded);
            }
        }
    }

    // brand, segment, family, truckType
    private void importDimensionProduct(String pathFile) throws IOException {
        InputStream is = new FileInputStream(pathFile);
        XSSFWorkbook workbook = new XSSFWorkbook(is);

        Sheet sheet = workbook.getSheet("Data");
        Set<Product> listDimensionProduct = new HashSet<>();

        for (Row row : sheet) {
            if (row.getRowNum() == 1)
                assignColumnNames(row);
            else if (row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getCellType() != CellType.BLANK
                    && row.getRowNum() >= 2) {
                listDimensionProduct.add(mapExcelSheetToProductDimension(row));
            }
        }
        saveListDimensionProduct(listDimensionProduct);
    }


    private void importBaseProduct(String pathFile) throws IOException {
        InputStream is = new FileInputStream(pathFile);
        XSSFWorkbook workbook = new XSSFWorkbook(is);

        Sheet sheet = workbook.getSheet("Master Summary");
        Set<Product> listProduct = new HashSet<>();

        for (Row row : sheet) {
            if (row.getRowNum() == 0)
                assignColumnNames(row);
            else if (row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getCellType() != CellType.BLANK
                    && row.getRowNum() >= 1) {
                listProduct.addAll(mappedFromAPACFile(row));
            }
        }
        saveListBaseProduct(listProduct);
    }

    private void saveListDimensionProduct(Set<Product> listDimensionProduct) {
        List<Product> listProductInDB = new ArrayList<>();
        for (Product p : listDimensionProduct) {
            Optional<Product> optionalProduct = productRepository.findByModelCodeAndMetaSeries(p.getModelCode(), p.getMetaSeries());
            optionalProduct.ifPresent(listProductInDB::add);
        }

        List<Product> listNewProductIsExist = new ArrayList<>();

        for (Product oldProduct : listProductInDB) {
            for (Product newProduct : listDimensionProduct) {
                if (newProduct.getModelCode().equals(oldProduct.getModelCode())
                        //in DB is series but in ProductDimension file is metaSeries
                        && newProduct.getMetaSeries().equals(oldProduct.getMetaSeries().substring(1))) {
                    oldProduct.setFamily(newProduct.getFamily());
                    oldProduct.setSegment(newProduct.getSegment());
                    oldProduct.setTruckType(newProduct.getTruckType());
                    listNewProductIsExist.add(newProduct);
                    break;
                }
            }
        }

        listNewProductIsExist.forEach(listDimensionProduct::remove);
        listDimensionProduct.addAll(listProductInDB);

        productRepository.saveAll(listDimensionProduct);
    }


    private void saveListBaseProduct(Set<Product> list) {
        List<Product> listProductInDB = new ArrayList<>();
        for (Product p : list) {
            Optional<Product> optionalProduct = productRepository.findByModelCodeAndMetaSeries(p.getModelCode(), p.getMetaSeries());
            optionalProduct.ifPresent(listProductInDB::add);
        }

        List<Product> listNewProductIsExist = new ArrayList<>();

        // update product if exist
        for (Product newProduct : list) {
            for (Product product : listProductInDB) {
                if (product.getModelCode().equals(newProduct.getModelCode()) && product.getMetaSeries().equals(newProduct.getMetaSeries())) {
                    product.setPlant(newProduct.getPlant());
                    product.setClazz(newProduct.getClazz());
                    product.setBrand(newProduct.getBrand());
                    listNewProductIsExist.add(newProduct);
                    break;
                }
            }
        }
        listNewProductIsExist.forEach(list::remove);
        list.addAll(listProductInDB);

        productRepository.saveAll(list);

    }
}

