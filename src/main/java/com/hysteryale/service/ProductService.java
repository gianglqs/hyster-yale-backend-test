package com.hysteryale.service;

import com.hysteryale.exception.MissingColumnException;
import com.hysteryale.model.Product;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.repository.ProductRepository;
import com.hysteryale.utils.*;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.*;


@Service
@Slf4j
public class ProductService extends BasedService {
    @Resource
    ProductRepository productRepository;

    @Resource
    FileUploadService fileUploadService;

    @Resource
    UpdateHistoryService updateHistoryService;

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

    public Product mapExcelSheetToProductDimension(Row row) throws MissingColumnException {
        Product product = new Product();

        // metaseries
        if (row.getCell(COLUMNS.get("Metaseries")) != null) {
            String metaSeries = row.getCell(COLUMNS.get("Metaseries")).getStringCellValue();
            product.setSeries(metaSeries);
        } else {
            throw new MissingColumnException("Not found column 'Metaseries'");
        }

        // modelCode
        if (row.getCell(COLUMNS.get("Model")) != null) {
            String modelCode = row.getCell(COLUMNS.get("Model")).getStringCellValue();
            product.setModelCode(modelCode);
        } else {
            throw new MissingColumnException("Not found column 'Model'");
        }

        //Segment
        if (row.getCell(COLUMNS.get("Segment")) != null) {
            String segment = row.getCell(COLUMNS.get("Segment")).getStringCellValue();
            product.setSegment(segment);
        } else {
            throw new MissingColumnException("Not found column 'Segment'");
        }

        // family
        if (row.getCell(COLUMNS.get("Family_Name")) != null) {
            String family = row.getCell(COLUMNS.get("Family_Name")).getStringCellValue();
            product.setFamily(family);
        } else {
            throw new MissingColumnException("Not found column 'Family_Name'");
        }

        // truckType
        if (row.getCell(COLUMNS.get("Truck_Type")) != null) {
            String truckType = row.getCell(COLUMNS.get("Truck_Type")).getStringCellValue();
            product.setTruckType(truckType);
        } else {
            throw new MissingColumnException("Not found column 'Truck_Type'");
        }

        return product;
    }


    public void importProductDimension() throws IOException, MissingColumnException {
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

    private void importProduct(Sheet sheet) throws MissingColumnException {
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
        Optional<Product> productDimensionOptional = productRepository.findByModelCodeAndSeries(product.getModelCode(), product.getSeries());
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


    public Map<String, Object> getDataByFilter(FilterModel filters) throws ParseException {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> filterMap = ConvertDataFilterUtil.loadDataFilterIntoMap(filters);
        logInfo(filterMap.toString());
        // product filter by: plant, segment, brand, family, metaSeries
        List<Product> getData = productRepository.getDataByFilter(
                (String) filterMap.get("modelCodeFilter"), (List<String>) filterMap.get("plantFilter"),
                (List<String>) filterMap.get("metaSeriesFilter"), (List<String>) filterMap.get("classFilter"),
                (List<String>) filterMap.get("segmentFilter"), (List<String>) filterMap.get("brandFilter"),
                (List<String>) filterMap.get("truckTypeFilter"),
                (List<String>) filterMap.get("familyFilter"), (Pageable) filterMap.get("pageable")
        );
        result.put("listData", getData);
        //Count data
        long countAll = productRepository.countAll(
                (String) filterMap.get("modelCodeFilter"), (List<String>) filterMap.get("plantFilter"),
                (List<String>) filterMap.get("metaSeriesFilter"), (List<String>) filterMap.get("classFilter"),
                (List<String>) filterMap.get("segmentFilter"), (List<String>) filterMap.get("brandFilter"),
                (List<String>) filterMap.get("truckTypeFilter"), (List<String>) filterMap.get("familyFilter"));
        result.put("totalItems", countAll);
        // get latest updated time
        Optional<LocalDateTime> latestUpdatedTimeOptional = productRepository.getLatestUpdatedTime();
        String latestUpdatedTime = null;
        if (latestUpdatedTimeOptional.isPresent()) {
            latestUpdatedTime = DateUtils.convertLocalDateTimeToString(latestUpdatedTimeOptional.get());
        }

        result.put("latestUpdatedTime",latestUpdatedTime);
        result.put("serverTimeZone", TimeZone.getDefault().getID());
        return result;

    }

    public void updateImageAndDescription(String modelCode, String series, String imagePath, String description) throws NotFoundException {
        Optional<Product> productOptional = productRepository.findByModelCodeAndSeries(modelCode, series);
        if (productOptional.isEmpty())
            throw new NotFoundException("No product found with modelCode: " + modelCode);

        Product product = productOptional.get();
        if (imagePath != null)
            product.setImage(imagePath);

        if (description != null)
            product.setDescription(description);

        productRepository.save(product);
    }

    public Product getProductDimensionDetail(String modelCode, String series) throws NotFoundException {
        Optional<Product> productOptional = productRepository.findByModelCodeAndSeries(modelCode, series);
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
                                seriesInformation.getSeries(),
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


    public List<Product> mappedFromAPACFile(Row row) throws MissingColumnException {
        List<Product> listProduct = new ArrayList<>();

        String plant;
        if (row.getCell(COLUMNS.get("Plant")) != null) {
            plant = row.getCell(COLUMNS.get("Plant")).getStringCellValue();
        } else {
            throw new MissingColumnException("Not found column 'Plant'");
        }

        String clazz;
        if (row.getCell(COLUMNS.get("Class")) != null) {
            clazz = row.getCell(COLUMNS.get("Class")).getStringCellValue();
        } else {
            throw new MissingColumnException("Not found column 'Class'");
        }

        // Hyster
        Product hysterProduct = new Product();
        Cell hysterSeriesCell;
        if (row.getCell(COLUMNS.get("Hyster")) != null) {
            hysterSeriesCell = row.getCell(COLUMNS.get("Hyster"));
        } else {
            throw new MissingColumnException("Not found column 'Hyster'");
        }

        Cell hysterModelCell;
        if (row.getCell(COLUMNS.get("Model")) != null) {
            hysterModelCell = row.getCell(COLUMNS.get("Model"));
        } else {
            throw new MissingColumnException("Not found column 'Model' of Hyster");
        }


        if (hysterSeriesCell.getCellType() == CellType.STRING && hysterModelCell.getCellType() == CellType.STRING) {
            String hysterSeries = hysterSeriesCell.getStringCellValue();
            String hysterModel = getValidModelCodeFromModelCell(hysterModelCell);

            if (!hysterModel.equals("NA") && !hysterSeries.equals("NA")) {
                hysterProduct.setModelCode(hysterModel);
                hysterProduct.setSeries(hysterSeries);
                hysterProduct.setPlant(plant);
                hysterProduct.setClazz(clazz);
                hysterProduct.setBrand("Hyster");
                listProduct.add(hysterProduct);
            }
        }

        // Yale
        Product yaleProduct = new Product();
        Cell yaleSeriesCell;
        if (row.getCell(COLUMNS.get("Yale")) != null) {
            yaleSeriesCell = row.getCell(COLUMNS.get("Yale"));
        } else {
            throw new MissingColumnException("Not found column 'Yale'");
        }

        Cell yaleModelCell;
        if (row.getCell(COLUMNS.get("Model_Y")) != null) {
            yaleModelCell = row.getCell(COLUMNS.get("Model_Y"));
        } else {
            throw new MissingColumnException("Not found column 'Model' of Yale");
        }

        if (yaleSeriesCell.getCellType() == CellType.STRING && yaleModelCell.getCellType() == CellType.STRING) {
            String yaleSeries = yaleSeriesCell.getStringCellValue();
            String yaleModel = getValidModelCodeFromModelCell(yaleModelCell);


            if (!yaleModel.equals("NA") && !yaleSeries.equals("NA")) {
                yaleProduct.setModelCode(yaleModel);
                yaleProduct.setSeries(yaleSeries);
                yaleProduct.setPlant(plant);
                yaleProduct.setClazz(clazz);
                yaleProduct.setBrand("Yale");
                listProduct.add(yaleProduct);
            }
        }


        return listProduct;
    }

    private String getValidModelCodeFromModelCell(Cell modelCell) {
        String modelCode = modelCell.getStringCellValue();
        return modelCode.split("_| -")[0];
    }

    public void importProduct(List<MultipartFile> fileList, Authentication authentication) throws Exception {
        String baseFolder = EnvironmentUtils.getEnvironmentValue("upload_files.base-folder");
        String excelFileExtension = FileUtils.EXCEL_FILE_EXTENSION;
        String modelType = ModelUtil.PRODUCT;
        for (MultipartFile file : fileList) {
            String savedFileNameUploaded = fileUploadService.saveFileUploaded(file, authentication, baseFolder, excelFileExtension, modelType);
            String pathFileUploaded = baseFolder + "/" + savedFileNameUploaded;
            boolean checkValidFileName = false;
            if (file.getOriginalFilename().contains("APAC")) {
                importBaseProduct(pathFileUploaded);
                checkValidFileName = true;
            }
            if (file.getOriginalFilename().contains("dimension")) {
                importDimensionProduct(pathFileUploaded);
                checkValidFileName = true;
            }

            if (!checkValidFileName) {
                throw new FileNotFoundException("File name is invalid");
            }

            updateHistoryService.handleUpdatedSuccessfully(savedFileNameUploaded, ModelUtil.PRODUCT, authentication);
        }
    }

    // brand, segment, family, truckType
    private void importDimensionProduct(String pathFile) throws IOException, MissingColumnException {
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


    private void importBaseProduct(String pathFile) throws IOException, MissingColumnException {
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
            List<Product> products = productRepository.findByModelCodeAndMetaSeries(p.getModelCode(), p.getSeries());
            listProductInDB.addAll(products);
        }

        for (Product oldProduct : listProductInDB) {
            for (Product newProduct : listDimensionProduct) {
                if (newProduct.getModelCode().equals(oldProduct.getModelCode())
                        //in DB : series but in ProductDimension file is metaSeries
                        && newProduct.getSeries().equals(oldProduct.getSeries().substring(1))) {
                    oldProduct.setFamily(newProduct.getFamily());
                    oldProduct.setSegment(newProduct.getSegment());
                    oldProduct.setTruckType(newProduct.getTruckType());
                    break;
                }
            }
        }
        productRepository.saveAll(listProductInDB);
    }


    private void saveListBaseProduct(Set<Product> list) {
        List<Product> listProductInDB = new ArrayList<>();
        for (Product p : list) {
            Optional<Product> optionalProduct = productRepository.findByModelCodeAndSeries(p.getModelCode(), p.getSeries());
            optionalProduct.ifPresent(listProductInDB::add);
        }

        List<Product> listNewProductIsExist = new ArrayList<>();

        // update product if exist
        for (Product newProduct : list) {
            for (Product product : listProductInDB) {
                if (product.getModelCode().equals(newProduct.getModelCode()) && product.getSeries().equals(newProduct.getSeries())) {
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

    public Product findProductByModelCodeAndSeries(List<Product> products, String modelCode, String series) {
        for (Product product : products) {
            if (product.getModelCode().equals(modelCode) && product.getSeries().equals(series))
                return product;
        }
        return null;
    }
}
