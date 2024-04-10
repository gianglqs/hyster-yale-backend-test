package com.hysteryale.service;

import com.hysteryale.exception.BlankSheetException;
import com.hysteryale.exception.MissingSheetException;
import com.hysteryale.model.Product;
import com.hysteryale.model.ResidualValue;
import com.hysteryale.model.enums.ImportFailureType;
import com.hysteryale.model.enums.ModelTypeEnum;
import com.hysteryale.model.importFailure.ImportFailure;
import com.hysteryale.repository.ProductRepository;
import com.hysteryale.repository.ResidualValueRepository;
import com.hysteryale.service.impl.ResidualValueServiceImp;
import com.hysteryale.utils.CheckRequiredColumnUtils;
import com.hysteryale.utils.EnvironmentUtils;
import com.hysteryale.utils.FileUtils;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SpringBootTest
public class ResidualValueServiceTest {


    @Resource
    private ResidualValueService residualValueService;

    @Resource
    private ResidualValueServiceImp residualValueServiceImp;

    @Resource
    private ResidualValueRepository residualValueRepository;

    @Resource
    AuthenticationManager authenticationManager;

    @Resource
    FileUploadService fileUploadService;


    @Resource
    ProductRepository productRepository;

    /**
     * {@link ResidualValueServiceImp#getDataByFilter(String)}
     */
    @Test
    public void testGetDataByFilter_modelCodeExist() {
        String modelCode = "J2.5XNL";
        List<ResidualValue> findAll = residualValueRepository.findAll();
        Map<String, Object> response = residualValueService.getDataByFilter(modelCode);
        List<ResidualValue> residualValueList = new ArrayList<>((Set<ResidualValue>) response.get("listResidualValue"));
        Assertions.assertEquals(residualValueList.size(), 104);
        Assertions.assertEquals(residualValueList.get(0).getId().getProduct().getModelCode(), modelCode);
    }

    /**
     * {@link ResidualValueServiceImp#getDataByFilter(String)}
     */
    @Test
    public void testGetDataByFilter_modelCodeNotExist() {
        String modelCode = "AAAA";
        List<ResidualValue> findAll = residualValueRepository.findAll();
        Map<String, Object> response = residualValueService.getDataByFilter(modelCode);
        List<ResidualValue> residualValueList = new ArrayList<>((Set<ResidualValue>) response.get("listResidualValue"));
        Assertions.assertEquals(residualValueList.size(), 0);
    }

    @Test
    public void testSaveFileUpload() throws IOException {
        // Set up Authentication
        String username = "user1@gmail.com";
        String password = "123456";

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        password
                )
        );

        // Set up uploaded file
        org.springframework.core.io.Resource fileResource = new ClassPathResource("import_files/residual_value/RV_ASIA_2023_v1.1-2.xlsx");
        Assertions.assertNotNull(fileResource);

        MultipartFile file = new MockMultipartFile(
                "file",
                fileResource.getFilename(),
                MediaType.MULTIPART_FORM_DATA_VALUE,
                fileResource.getInputStream()
        );

        String targetFolder = EnvironmentUtils.getEnvironmentValue("upload_files.residual_value");
        String excelFileExtension = FileUtils.EXCEL_FILE_EXTENSION;
        // Assertions
        Assertions.assertDoesNotThrow(() -> fileUploadService.saveFileUploaded(file, authentication, targetFolder, excelFileExtension, ModelTypeEnum.RESIDUAL_VALUE.getValue()));

    }

    /**
     * {@link ResidualValueServiceImp#mapResidualWithProduct(List, Row, FormulaEvaluator, List, String, int)}
     */
    @Test
    public void testMapResidualWithProduct() throws IOException {
        String modelCode = "J2.5XNL";
        List<Product> products = productRepository.findAllByModelCode(modelCode);

        org.springframework.core.io.Resource fileResource = new ClassPathResource("import_files/residual_value/RV_ASIA_2023_v1.1-2.xlsx");
        XSSFWorkbook workbook = new XSSFWorkbook(fileResource.getInputStream());

        String sheetName = CheckRequiredColumnUtils.RESIDUAL_VALUE_REQUIRED_SHEET;
        Sheet sheet = workbook.getSheet(sheetName);
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        List<ImportFailure> importFailures = new ArrayList<>();
        String modelType = ModelTypeEnum.RESIDUAL_VALUE.getValue();
        int year = 2023;
        Row row = sheet.getRow(308);
        Map<String, Integer> indexRangeOfNeedDataMap = residualValueServiceImp.getIndexRangeOfNeedData(sheet);
        residualValueServiceImp.mapHeaderColumn(sheet, indexRangeOfNeedDataMap.get("headerRowNum"));

        List<ResidualValue> residualValueListAfterMapWithProduct = residualValueServiceImp.mapResidualWithProduct(products, row, evaluator, importFailures, modelType, year);

        Assertions.assertNotNull(residualValueListAfterMapWithProduct);
        Assertions.assertEquals(residualValueListAfterMapWithProduct.size(), 105);
        Assertions.assertEquals(residualValueListAfterMapWithProduct.get(0).getId().getProduct().getModelCode(), modelCode);
    }

    /**
     * {@link ResidualValueServiceImp#mapHeaderColumn(Sheet, int)}
     */
    @Test
    public void testMapHeaderColumn() throws IOException {
        org.springframework.core.io.Resource fileResource = new ClassPathResource("import_files/residual_value/RV_ASIA_2023_v1.1-2.xlsx");
        XSSFWorkbook workbook = new XSSFWorkbook(fileResource.getInputStream());
        String sheetName = CheckRequiredColumnUtils.RESIDUAL_VALUE_REQUIRED_SHEET;
        Sheet sheet = workbook.getSheet(sheetName);
        Map<String, Integer> indexRangeOfNeedDataMap = residualValueServiceImp.getIndexRangeOfNeedData(sheet);
        residualValueServiceImp.mapHeaderColumn(sheet, indexRangeOfNeedDataMap.get("headerRowNum"));
        Map<String, Integer> RESIDUAL_COLUMN = residualValueServiceImp.getRESIDUAL_COLUMN();
        Assertions.assertEquals(RESIDUAL_COLUMN.size(), 107);
        Set<String> listColumnName = RESIDUAL_COLUMN.keySet();

        Assertions.assertTrue(listColumnName.containsAll(List.of("Yale Model", "Hyster Model", "24500", "24750",
                "241000", "241250", "241500", "241750", "242000", "242250", "242500", "242750", "243000", "243250",
                "243500", "243750", "244000", "36500", "36750", "361000", "361250", "361500", "361750", "362000", "362250",
                "362500", "362750", "363000", "363250", "363500", "363750", "364000", "48500", "48750", "481000", "481250",
                "481500", "481750", "482000", "482250", "482500", "482750", "483000", "483250", "483500", "483750", "484000",
                "60500", "60750", "601000", "601250", "601500", "601750", "602000", "602250", "602500", "602750", "603000",
                "603250", "603500", "603750", "604000", "72500", "72750", "721000", "721250", "721500", "721750", "722000",
                "722250", "722500", "722750", "723000", "723250", "723500", "723750", "724000", "84500", "84750", "841000",
                "841250", "841500", "841750", "842000", "842250", "842500", "842750", "843000", "843250", "843500", "843750",
                "844000", "961000", "961250", "961500", "961750", "962000", "1081000", "1081250", "1081500", "1081750",
                "1082000", "1201000", "1201250", "1201500", "1201750", "1202000")));
    }

    /**
     * {@link ResidualValueServiceImp#getIndexRangeOfNeedData(Sheet)}
     */
    @Test
    public void testGetIndexRangeOfNeedData() throws IOException {
        org.springframework.core.io.Resource fileResource = new ClassPathResource("import_files/residual_value/RV_ASIA_2023_v1.1-2.xlsx");
        XSSFWorkbook workbook = new XSSFWorkbook(fileResource.getInputStream());
        String sheetName = CheckRequiredColumnUtils.RESIDUAL_VALUE_REQUIRED_SHEET;
        Sheet sheet = workbook.getSheet(sheetName);
        Map<String, Integer> indexRangeOfNeedDataMap = residualValueServiceImp.getIndexRangeOfNeedData(sheet);
        Assertions.assertEquals(indexRangeOfNeedDataMap.get("firstRow"), 187);
        Assertions.assertEquals(indexRangeOfNeedDataMap.get("lastColumnHeaderPercent"), 1);
        Assertions.assertEquals(indexRangeOfNeedDataMap.get("lastRow"), 357);
        Assertions.assertEquals(indexRangeOfNeedDataMap.get("headerRowNum"), 7);
    }

    /**
     * {@link ResidualValueServiceImp#mapDataExcelIntoResidualValue(Row, List, List, FormulaEvaluator, List, String, int)}
     */
    @Test
    public void testMapDataExcelIntoResidualValue() throws IOException {
        org.springframework.core.io.Resource fileResource = new ClassPathResource("import_files/residual_value/RV_ASIA_2023_v1.1-2.xlsx");
        XSSFWorkbook workbook = new XSSFWorkbook(fileResource.getInputStream());

        String sheetName = CheckRequiredColumnUtils.RESIDUAL_VALUE_REQUIRED_SHEET;
        Sheet sheet = workbook.getSheet(sheetName);
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        List<ImportFailure> importFailures = new ArrayList<>();
        List<ResidualValue> residualValues = new ArrayList<>();
        String modelType = ModelTypeEnum.RESIDUAL_VALUE.getValue();
        List<Product> products = productRepository.findAll();

        int year = 2023;
        Row row = sheet.getRow(308);
        Map<String, Integer> indexRangeOfNeedDataMap = residualValueServiceImp.getIndexRangeOfNeedData(sheet);
        residualValueServiceImp.mapHeaderColumn(sheet, indexRangeOfNeedDataMap.get("headerRowNum"));
        residualValueServiceImp.mapDataExcelIntoResidualValue(row, products, importFailures, evaluator, residualValues, modelType, year);
        Assertions.assertEquals(residualValues.size(), 210);
        Assertions.assertEquals(residualValues.get(0).getId().getProduct().getModelCode(), "J2.5XNL");
    }

    /**
     * {@link ResidualValueServiceImp#importResidualValue}
     */
    @Test
    public void testImportResidualValue() throws IOException, BlankSheetException, MissingSheetException {
        org.springframework.core.io.Resource fileResource = new ClassPathResource("import_files/residual_value/RV_ASIA_2023_v1.1-2.xlsx");
        String fileUUID = "test";
        int year = 2023;

        List<ImportFailure> importFailures = residualValueService.importResidualValue(fileResource.getURL().getPath(), fileUUID, year);
        Assertions.assertEquals(importFailures.size(), 209);
        Assertions.assertEquals(importFailures.get(0).getType(), ImportFailureType.ERROR.getValue());
        Assertions.assertEquals(importFailures.get(0).getReasonKey(), "not-find-product-with-modelCode");
        Assertions.assertEquals(importFailures.get(0).getPrimaryKey(), "188");

    }


}
