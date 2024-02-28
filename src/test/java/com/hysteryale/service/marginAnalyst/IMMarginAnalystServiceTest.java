package com.hysteryale.service.marginAnalyst;

import com.hysteryale.model.Currency;
import com.hysteryale.model.marginAnalyst.MarginAnalystMacro;
import com.hysteryale.model.upload.FileUpload;
import com.hysteryale.model_h2.IMMarginAnalystData;
import com.hysteryale.model_h2.IMMarginAnalystSummary;
import com.hysteryale.repository.marginAnalyst.MarginAnalystMacroRepository;
import com.hysteryale.repository.upload.FileUploadRepository;
import com.hysteryale.repository_h2.IMMarginAnalystDataRepository;
import com.hysteryale.utils.CurrencyFormatUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
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
    IMMarginAnalystDataRepository marginAnalystDataRepository;
    @Resource
    MarginAnalystMacroRepository marginAnalystMacroRepository;
    @Resource
    MarginAnalystMacroService marginAnalystMacroService;
    @Resource
    FileUploadRepository fileUploadRepository;


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
        double result = marginAnalystDataService.getManufacturingCost(modelCode, partNumber, currency, plant, dealerNet, exchangeRate);
        Assertions.assertEquals(manufacturingCost, result);

        // Test case NON-EXISTING Manufacturing Cost with PLANT = 'SN'
        double notFoundResultSN = marginAnalystDataService.getManufacturingCost(modelCode, partNumber, currency, "SN", dealerNet, exchangeRate);
        Assertions.assertEquals(dealerNet * 0.9, notFoundResultSN);

        // Test case NON-EXISTING Manufacturing Cost with PLANT = 'HYM' (~ Maximal, Ruyi, Staxx ~)
        double notFoundResultHYM = marginAnalystDataService.getManufacturingCost(modelCode, "NOT FOUND PART", currency, "HYM", dealerNet, exchangeRate);
        Assertions.assertEquals((dealerNet / 0.2) * 0.9, notFoundResultHYM);
    }

    @Test
    public void testPopulateMarginFilters() throws IOException {
        FileUpload fileUpload = new FileUpload();
        fileUpload.setFileName("import_files/novo/SN_AUD.xlsx");
        fileUpload.setUuid("UUID populate Margin Filters");
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
        Map<String, Object> result = marginAnalystDataService.populateMarginFilters("UUID populate Margin Filters");
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
        for(Cell cell : row) {
            String columnName = cell.getStringCellValue();
            COLUMN_NAME.put(columnName, cell.getColumnIndex());
        }
        return COLUMN_NAME;
    }

    @Test
    public void testIsFileCalculated() {
        IMMarginAnalystData data = new IMMarginAnalystData();
        data.setFileUUID("UUID test file calculated");
        data.setCurrency("USD");
        marginAnalystDataRepository.save(data);

        boolean result = marginAnalystDataService.isFileCalculated("UUID test file calculated", "USD");
        Assertions.assertTrue(result);
    }

    @Test
    public void testCalculateMarginAnalysisData() throws IOException {
        FileUpload fileUpload = new FileUpload();
        fileUpload.setFileName("import_files/novo/SN_AUD.xlsx");
        fileUpload.setUuid("UUID For Calculating Margin Data");
        fileUploadRepository.save(fileUpload);
        marginAnalystDataService.calculateMarginAnalysisData("UUID For Calculating Margin Data", "AUD");

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

            Optional<IMMarginAnalystData> optional = marginAnalystDataRepository.getIMMarginAnalystDataForTesting(modelCode, partNumber, type, "UUID For Calculating Margin Data");
            if(optional.isPresent()) {
                IMMarginAnalystData dbData = optional.get();

                Assertions.assertEquals(series, dbData.getSeries());
                Assertions.assertEquals(listPrice, dbData.getListPrice());
                Assertions.assertEquals(dealerNet, dbData.getDealerNet());
            }
        }
    }

    @Test
    public void testGetMarginAnalysisData() throws IOException {
        String modelCode = "H2.5UT";
        String strCurrency = "USD";
        String fileUUID = "UUID Get Margin Data";
        String orderNumber = "H82381";
        Integer type = 1;
        String series = "A3C1";

        FileUpload fileUpload = new FileUpload();
        fileUpload.setFileName("import_files/novo/example 1_HYM.xlsx");
        fileUpload.setUuid(fileUUID);
        fileUploadRepository.save(fileUpload);
        marginAnalystDataService.calculateMarginAnalysisData(fileUUID, strCurrency);

        List<IMMarginAnalystData> result = marginAnalystDataService.getIMMarginAnalystData(modelCode, strCurrency, fileUUID, orderNumber, type, series);
        Assertions.assertEquals(34, result.size());

        for(IMMarginAnalystData data : result) {
            Assertions.assertEquals(modelCode, data.getModelCode());
            Assertions.assertEquals(strCurrency, data.getCurrency());
            Assertions.assertEquals(fileUUID, data.getFileUUID());
            Assertions.assertEquals(orderNumber, data.getOrderNumber());
            Assertions.assertEquals(type, data.getType());
            Assertions.assertEquals(series, data.getSeries());

            Double manufacturingCost = marginAnalystMacroRepository.getManufacturingCost(
                    modelCode, data.getOptionCode(), strCurrency,
                    new ArrayList<>(List.of("Maximal"))
            );
            double exchangeRate = 0.1436; // Exchange Rate in 2023 Decembers
            if(manufacturingCost == null)
                manufacturingCost = (data.getDealerNet() / exchangeRate) * 0.9;

            if(data.isSPED()) {
                manufacturingCost = manufacturingCost * exchangeRate + 0.9 * data.getDealerNet();
                manufacturingCost = manufacturingCost / exchangeRate;
            }
            Assertions.assertEquals(manufacturingCost, data.getManufacturingCost());

        }
    }

    @Test
    public void testCalculateMarginAnalystSummary() throws IOException {
        String modelCode = "H2.5UT";
        String strCurrency = "USD";
        String fileUUID = "UUID Get Margin Data 2";
        String orderNumber = "H82381";
        Integer type = 1;
        String series = "A3C1";

        FileUpload fileUpload = new FileUpload();
        fileUpload.setFileName("import_files/novo/example 1_HYM.xlsx");
        fileUpload.setUuid(fileUUID);
        fileUploadRepository.save(fileUpload);
        marginAnalystDataService.calculateMarginAnalysisData(fileUUID, strCurrency);

        List<IMMarginAnalystData> dataList = marginAnalystDataService.getIMMarginAnalystData(modelCode, strCurrency, fileUUID, orderNumber, type, series);
        Assertions.assertEquals(34, dataList.size());

        Map<String, Object> result = marginAnalystDataService.calculateMarginAnalysisSummary(fileUUID, type, modelCode, series, orderNumber, strCurrency);
        IMMarginAnalystSummary monthlyResult = (IMMarginAnalystSummary) result.get("MarginAnalystSummaryMonthly");
        IMMarginAnalystSummary annuallyResult = (IMMarginAnalystSummary) result.get("MarginAnalystSummaryAnnually");

        assertMarginAnalystSummary(monthlyResult, dataList, series, true);
        assertMarginAnalystSummary(annuallyResult, dataList, series, false);
    }

    private void assertMarginAnalystSummary(IMMarginAnalystSummary result, List<IMMarginAnalystData> dataList, String series, boolean isMonthly) {
        LocalDate monthYear = LocalDate.of(2023, Month.DECEMBER, 1);
        double totalListPrice = 0, totalManufacturingCost = 0, totalDealerNet = 0;
        for(IMMarginAnalystData data : dataList) {
            totalListPrice += data.getListPrice();
            totalManufacturingCost += data.getManufacturingCost();
            totalDealerNet += data.getDealerNet();
        }

        double costUplift = 0.0, surcharge = 0.015, aopRate = isMonthly ? 0.1401 : 0.1436;
        String clazz = marginAnalystMacroService.getClassBySeries(series);
        double warranty = marginAnalystMacroService.getWarrantyValue(clazz, monthYear);
        double duty = clazz != null
                        ? clazz.equals("Class 5 BT") ? 0.05 : 0.0
                        : 0.0;
        double totalCost = totalManufacturingCost * (1 + costUplift) * (1 + warranty + surcharge + duty);
        double blendedDiscount = 1 - (totalDealerNet / totalListPrice);
        double fullCostAOPRate = totalCost * aopRate;
        double manufacturingCostUSD = totalManufacturingCost * aopRate;
        double warrantyCost = manufacturingCostUSD * warranty;
        double surchargeCost = manufacturingCostUSD * surcharge;
        double dutyCost = manufacturingCostUSD * duty;
        double totalCostWithoutFreight = manufacturingCostUSD + warrantyCost + surchargeCost + dutyCost;
        double margin = totalDealerNet - fullCostAOPRate;
        double marginPercentAopRate = totalDealerNet == 0 ? 0 : margin / totalDealerNet;

        Assertions.assertEquals(CurrencyFormatUtils.formatDoubleValue(totalListPrice, CurrencyFormatUtils.decimalFormatFourDigits), result.getTotalListPrice());
        Assertions.assertEquals(CurrencyFormatUtils.formatDoubleValue(totalManufacturingCost, CurrencyFormatUtils.decimalFormatFourDigits), result.getTotalManufacturingCost());
        Assertions.assertEquals( CurrencyFormatUtils.formatDoubleValue(totalDealerNet, CurrencyFormatUtils.decimalFormatFourDigits), result.getDealerNet());
        Assertions.assertEquals(aopRate, result.getMarginAopRate());
        Assertions.assertEquals(warranty, result.getAddWarranty());
        Assertions.assertEquals(duty, result.getDuty());
        Assertions.assertEquals(CurrencyFormatUtils.formatDoubleValue(totalCost, CurrencyFormatUtils.decimalFormatFourDigits), result.getTotalCost());
        Assertions.assertEquals(CurrencyFormatUtils.formatDoubleValue(blendedDiscount, CurrencyFormatUtils.decimalFormatFourDigits), result.getBlendedDiscountPercentage());
        Assertions.assertEquals(manufacturingCostUSD, result.getManufacturingCostUSD());
        Assertions.assertEquals(warrantyCost, result.getWarrantyCost());
        Assertions.assertEquals(surchargeCost, result.getSurchargeCost());
        Assertions.assertEquals(dutyCost, result.getDutyCost());
        Assertions.assertEquals(totalCostWithoutFreight, result.getTotalCostWithoutFreight());
        Assertions.assertEquals(CurrencyFormatUtils.formatDoubleValue(margin, CurrencyFormatUtils.decimalFormatFourDigits), result.getMargin());

        // consider monthly and annually rate
        Assertions.assertEquals(CurrencyFormatUtils.formatDoubleValue(fullCostAOPRate, CurrencyFormatUtils.decimalFormatFourDigits), isMonthly ? result.getFullMonthlyRate() : result.getFullCostAopRate());
        Assertions.assertEquals(CurrencyFormatUtils.formatDoubleValue(marginPercentAopRate, CurrencyFormatUtils.decimalFormatFourDigits), isMonthly ? result.getMarginPercentMonthlyRate() : result.getMarginPercentAopRate());
    }
}
