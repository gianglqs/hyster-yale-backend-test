package com.hysteryale.service.marginAnalyst;

import com.hysteryale.model.Currency;
import com.hysteryale.model.marginAnalyst.MarginAnalystMacro;
import com.hysteryale.model.upload.FileUpload;
import com.hysteryale.model_h2.IMMarginAnalystData;
import com.hysteryale.repository.marginAnalyst.MarginAnalystMacroRepository;
import com.hysteryale.repository.upload.FileUploadRepository;
import com.hysteryale.repository_h2.IMMarginAnalystDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;

@SpringBootTest
@Slf4j
@SuppressWarnings("unchecked")
public class IMMarginAnalystServiceTest {
    @Resource
    IMMarginAnalystDataService marginAnalystDataService;
    @Resource
    MarginAnalystMacroRepository marginAnalystMacroRepository;
    @Resource
    FileUploadRepository fileUploadRepository;
    @Resource
    IMMarginAnalystDataRepository marginAnalystDataRepository;

    @Test
    public void testGetManufacturingCost() {
        String modelCode = "Model Code";
        String partNumber = "Part Number";
        String currency = "USD";
        String plant = "HYM";
        LocalDate monthYear = LocalDate.of(2024, Month.FEBRUARY, 1);
        double manufacturingCost = 1234;

        MarginAnalystMacro macro = new MarginAnalystMacro();
        macro.setModelCode(modelCode);
        macro.setPartNumber(partNumber);
        macro.setCurrency(new Currency(currency));
        macro.setPlant(plant);
        macro.setMonthYear(monthYear);
        macro.setCostRMB(manufacturingCost);
        marginAnalystMacroRepository.save(macro);

        double dealerNet = 1000;
        double exchangeRate = 0.2;

        // Test case EXISTING Manufacturing Cost
        double result = marginAnalystDataService.getManufacturingCost(modelCode, partNumber, currency, plant, monthYear, dealerNet, exchangeRate);
        Assertions.assertEquals(manufacturingCost, result);

        // Test case NON-EXISTING Manufacturing Cost with PLANT = 'SN'
        double notFoundResultSN = marginAnalystDataService.getManufacturingCost(modelCode, partNumber, currency, "SN", monthYear, dealerNet, exchangeRate);
        Assertions.assertEquals(dealerNet * 0.9, notFoundResultSN);

        // Test case NON-EXISTING Manufacturing Cost with PLANT = 'HYM' (~ Maximal, Ruyi, Staxx ~)
        double notFoundResultHYM = marginAnalystDataService.getManufacturingCost(modelCode, "NOT FOUND PART", currency, "HYM", monthYear, dealerNet, exchangeRate);
        Assertions.assertEquals((dealerNet / 0.2) * 0.9, notFoundResultHYM);
    }

    @Test
    public void testPopulateMarginFilters() throws IOException {
        FileUpload fileUpload = new FileUpload();
        fileUpload.setFileName("import_files/novo/SN_AUD.xlsx");
        fileUpload.setUuid("UUID");
        fileUploadRepository.save(fileUpload);

        FileInputStream is = new FileInputStream("import_files/novo/SN_AUD.xlsx");
        XSSFWorkbook workbook = new XSSFWorkbook(is);
        Sheet sheet = workbook.getSheetAt(0);
        Map<String, Integer> columns = new HashMap<>();

        HashMap<String, Integer> modelCodeMap = new HashMap<>();
        HashMap<String, Integer> seriesCodeMap = new HashMap<>();
        HashMap<String, Integer> orderNumberMap = new HashMap<>();
        HashMap<Integer, Integer> typeMap = new HashMap<>();

        for(Row row : sheet) {
            if(row.getRowNum() == 0)
                columns = getColumnName(row);
            else if(!row.getCell(columns.get("Model Code"), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().isEmpty()) {
                String orderIDCellValue = row.getCell(columns.get("Order ID"), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
                if(!orderIDCellValue.isEmpty())
                    orderNumberMap.put(orderIDCellValue, 1);

                modelCodeMap.put(row.getCell(columns.get("Model Code")).getStringCellValue(), 1);
                seriesCodeMap.put(row.getCell(columns.get("Series Code")).getStringCellValue(), 1);
                typeMap.put((int) row.getCell(columns.get("#")).getNumericCellValue(), 1);
            }
        }
        Map<String, Object> result = marginAnalystDataService.populateMarginFilters("UUID");
        Assertions.assertNotNull(result.get("modelCodes"));
        Assertions.assertNotNull(result.get("series"));
        Assertions.assertNotNull(result.get("orderNumbers"));
        Assertions.assertNotNull(result.get("types"));

        List<Object> modelCodes = (List<Object>) result.get("modelCodes");
        List<Object> series = (List<Object>) result.get("series");
        List<Object> orderNumbers = (List<Object>) result.get("orderNumbers");
        List<Object> types = (List<Object>) result.get("types");

        Assertions.assertEquals(modelCodeMap.size(), modelCodes.size());
        Assertions.assertEquals(seriesCodeMap.size(), series.size());
        Assertions.assertEquals(orderNumberMap.size(), orderNumbers.size());
        Assertions.assertEquals(typeMap.size(), types.size());
    }

    private Map<String, Integer> getColumnName(Row row) {
        Map<String, Integer> COLUMN_NAME = new HashMap<>();
        for(int i = 0; i < 23; i++) {
            String columnName = row.getCell(i).getStringCellValue();
            COLUMN_NAME.put(columnName, i);
        }
        return COLUMN_NAME;
    }

    @Test
    public void testIsFileCalculated() {
        IMMarginAnalystData data = new IMMarginAnalystData();
        data.setFileUUID("UUID");
        data.setCurrency("USD");
        marginAnalystDataRepository.save(data);

        boolean result = marginAnalystDataService.isFileCalculated("UUID", "USD");
        Assertions.assertTrue(result);
    }

    @Test
    public void testCalculateMarginAnalysisData() throws IOException {
        FileUpload fileUpload = new FileUpload();
        fileUpload.setFileName("import_files/novo/SN_AUD.xlsx");
        fileUpload.setUuid("UUID");
        fileUploadRepository.save(fileUpload);
        marginAnalystDataService.calculateMarginAnalysisData("UUID", "AUD");

        FileInputStream is = new FileInputStream("import_files/novo/SN_AUD.xlsx");
        XSSFWorkbook workbook = new XSSFWorkbook(is);
        Sheet sheet = workbook.getSheetAt(0);
        Map<String, Integer> columns = getColumnName(sheet.getRow(0));

        Random random = new Random();
        for(int i = 0; i < 10; i++) {
            int rowInt = random.nextInt(400) + 1;
            Row row = sheet.getRow(rowInt);
            int type = (int) row.getCell(columns.get("#")).getNumericCellValue();
            String modelCode = row.getCell(columns.get("Model Code")).getStringCellValue();
            String partNumber = row.getCell(columns.get("Part Number")).getStringCellValue();
            String series = row.getCell(columns.get("Series Code")).getStringCellValue();

            double listPrice = row.getCell(columns.get("List Price")).getNumericCellValue();
            double dealerNet = row.getCell(columns.get("Net Price Each")).getNumericCellValue();

            Optional<IMMarginAnalystData> optional = marginAnalystDataRepository.getIMMarginAnalystDataForTesting(modelCode, partNumber, type);
            if(optional.isPresent()) {
                IMMarginAnalystData dbData = optional.get();

                Assertions.assertEquals(series, dbData.getSeries());
                Assertions.assertEquals(listPrice, dbData.getListPrice());
                Assertions.assertEquals(dealerNet, dbData.getDealerNet());
            }
        }
    }
}
