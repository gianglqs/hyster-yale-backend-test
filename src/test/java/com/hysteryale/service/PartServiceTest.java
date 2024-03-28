package com.hysteryale.service;

import com.hysteryale.exception.*;
import com.hysteryale.model.Clazz;
import com.hysteryale.model.Currency;
import com.hysteryale.model.Part;
import com.hysteryale.repository.ClazzRepository;
import com.hysteryale.repository.PartRepository;
import com.hysteryale.utils.CurrencyFormatUtils;
import com.hysteryale.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest
@Slf4j
public class PartServiceTest {
    @Resource
    PartService partService;
    @Resource
    PartRepository partRepository;
    @Resource
    CurrencyService currencyService;
    @Resource
    ClazzRepository clazzRepository;

    @Test
    public void testImportPartFromFile() throws IOException, MissingColumnException, MissingSheetException, ExchangeRatesException, InvalidFileNameException, IncorectFormatCellException {
        String fileName = "power bi Oct 23.xlsx";
        String filePath = "import_files/bi_download/power bi Oct 23.xlsx";
        partService.importPartFromFile(fileName, filePath, "");

        Pattern pattern = Pattern.compile("\\w{5} \\w{2} (\\w{3}) (\\d{2}).xlsx");
        Matcher matcher = pattern.matcher(fileName);
        String month;
        int year;
        if (matcher.find()) {
            month = matcher.group(1);
            year = 2000 + Integer.parseInt(matcher.group(2));
        } else
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File name is not in appropriate format");
        LocalDate recordedTime = LocalDate.of(year, DateUtils.getMonth(month), 1);

        InputStream is = new FileInputStream(filePath);
        XSSFWorkbook workbook = new XSSFWorkbook(is);
        Sheet sheet = workbook.getSheet("Export");
        Map<String, Integer> columns = getPowerBiColumnsName(sheet.getRow(0));

        Random random = new Random();
        int bound = sheet.getLastRowNum();
        for(int i = 0; i < 10; i++) {
            int nextInt = random.nextInt(bound - 1) + 1;
            Row row = sheet.getRow(nextInt);
            Part part = mapPartValue(row, columns);
            part.setRecordedTime(recordedTime);

            Part dbPart = partService.getPart(part.getModelCode(), part.getPartNumber(), part.getOrderNumber(), recordedTime, part.getCurrency().getCurrency());
            assertPartValue(part, dbPart);
        }
    }

    private Map<String,Integer> getPowerBiColumnsName(Row row) {
        Map<String, Integer> powerBiExportColumns = new HashMap<>();
        for (Cell cell : row) {
            String columnsName = cell.getStringCellValue();
            powerBiExportColumns.put(columnsName, cell.getColumnIndex());
        }
        return powerBiExportColumns;
    }

    private Part mapPartValue(Row row, Map<String, Integer> powerBIExportColumns) throws ExchangeRatesException {
        String strCurrency = row.getCell(powerBIExportColumns.get("Currency")).getStringCellValue().strip();
        Currency currency = currencyService.getCurrenciesByName(strCurrency);

        String quoteId = row.getCell(powerBIExportColumns.get("Quote Number")).getStringCellValue();
        int quantity = (int) row.getCell(powerBIExportColumns.get("Quoted Quantity")).getNumericCellValue();

        String series = row.getCell(powerBIExportColumns.get("Series")).getStringCellValue();
        String partNumber = row.getCell(powerBIExportColumns.get("Part Number")).getStringCellValue();
        double listPrice = row.getCell(powerBIExportColumns.get("ListPrice")).getNumericCellValue();
        String modelCode = row.getCell(powerBIExportColumns.get("Model")).getStringCellValue();
        String clazz = row.getCell(powerBIExportColumns.get("Class"), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
        String region = row.getCell(powerBIExportColumns.get("Region"), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();

        double discountPercentage;
        Cell discountPercentageCell = row.getCell(powerBIExportColumns.get("Discount"));
        if (discountPercentageCell.getCellType() == CellType.NUMERIC) discountPercentage = discountPercentageCell.getNumericCellValue();
        else discountPercentage = Double.parseDouble(discountPercentageCell.getStringCellValue().isEmpty() ? "0" : discountPercentageCell.getStringCellValue());

        double discount = listPrice * (1 - (discountPercentage / 100));
        String billTo = row.getCell(powerBIExportColumns.get("Dealer")).getStringCellValue();

        double netPriceEach = row.getCell(powerBIExportColumns.get("Net Price")).getNumericCellValue();

        double customerPrice;
        Cell customerPriceCell = row.getCell(powerBIExportColumns.get("Customer Price"));
        if (customerPriceCell.getCellType() == CellType.NUMERIC) customerPrice = customerPriceCell.getNumericCellValue();
        else customerPrice = Double.parseDouble(customerPriceCell.getStringCellValue().isEmpty() ? "0" : customerPriceCell.getStringCellValue());

        double extendedCustomerPrice;
        Cell extendedCustomerPriceCell = row.getCell(powerBIExportColumns.get("Ext Customer Price"));
        if (extendedCustomerPriceCell.getCellType() == CellType.NUMERIC)
            extendedCustomerPrice = extendedCustomerPriceCell.getNumericCellValue();
        else
            extendedCustomerPrice = Double.parseDouble(extendedCustomerPriceCell.getStringCellValue().isEmpty() ? "0" : extendedCustomerPriceCell.getStringCellValue());

        String orderNumber = row.getCell(powerBIExportColumns.get("Order Number")).getStringCellValue();

        boolean isSPED =
                row.getCell(powerBIExportColumns.get("Part Description: English US"), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().contains("SPED") ||
                row.getCell(powerBIExportColumns.get("Part Description: English UK"), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().contains("SPED");

        return new Part(quoteId, quantity, orderNumber, modelCode, series, partNumber, listPrice, discount, discountPercentage, billTo, netPriceEach, customerPrice, extendedCustomerPrice, currency, getClazzByClazzName(clazz), region, isSPED);
    }

    private Clazz getClazzByClazzName(String clazzName) {
        clazzName = clazzName.equals("Class 5") ? "Class 5 BT" : clazzName;
        Optional<Clazz> optionalClazz = clazzRepository.getClazzByClazzName(clazzName);
        return optionalClazz.orElse(null);
    }

    private void assertPartValue(Part excel, Part dbPart) {
        Assertions.assertEquals(excel.getBillTo(), dbPart.getBillTo());
        Assertions.assertEquals(
                CurrencyFormatUtils.formatDoubleValue(excel.getCustomerPrice(), CurrencyFormatUtils.decimalFormatFourDigits),
                CurrencyFormatUtils.formatDoubleValue(dbPart.getCustomerPrice(), CurrencyFormatUtils.decimalFormatFourDigits)
        );
        Assertions.assertEquals(
                CurrencyFormatUtils.formatDoubleValue(excel.getDiscount(), CurrencyFormatUtils.decimalFormatFourDigits),
                CurrencyFormatUtils.formatDoubleValue(dbPart.getDiscount(), CurrencyFormatUtils.decimalFormatFourDigits)
        );
        Assertions.assertEquals(
                CurrencyFormatUtils.formatDoubleValue(excel.getDiscountPercentage(), CurrencyFormatUtils.decimalFormatFourDigits),
                CurrencyFormatUtils.formatDoubleValue(dbPart.getDiscountPercentage(), CurrencyFormatUtils.decimalFormatFourDigits)
        );
        Assertions.assertEquals(
                CurrencyFormatUtils.formatDoubleValue(excel.getDiscountToCustomerPercentage(), CurrencyFormatUtils.decimalFormatFourDigits),
                CurrencyFormatUtils.formatDoubleValue(dbPart.getDiscountToCustomerPercentage(), CurrencyFormatUtils.decimalFormatFourDigits)
        );
        Assertions.assertEquals(
                CurrencyFormatUtils.formatDoubleValue(excel.getExtendedCustomerPrice(), CurrencyFormatUtils.decimalFormatFourDigits),
                CurrencyFormatUtils.formatDoubleValue(dbPart.getExtendedCustomerPrice(), CurrencyFormatUtils.decimalFormatFourDigits)
        );
        Assertions.assertEquals(excel.isSPED(), dbPart.isSPED());
        Assertions.assertEquals(
                CurrencyFormatUtils.formatDoubleValue(excel.getListPrice(), CurrencyFormatUtils.decimalFormatFourDigits),
                CurrencyFormatUtils.formatDoubleValue(dbPart.getListPrice(), CurrencyFormatUtils.decimalFormatFourDigits)
        );
        Assertions.assertEquals(
                CurrencyFormatUtils.formatDoubleValue(excel.getNetPriceEach(), CurrencyFormatUtils.decimalFormatFourDigits),
                CurrencyFormatUtils.formatDoubleValue(dbPart.getNetPriceEach(), CurrencyFormatUtils.decimalFormatFourDigits)
        );
        Assertions.assertEquals(excel.getQuantity(), dbPart.getQuantity());
        Assertions.assertEquals(excel.getRegion(), dbPart.getRegion());
        Assertions.assertEquals(excel.getSeries(), dbPart.getSeries());
    }

    @Test
    public void testGetPart() {
        String modelCode = "Model Code";
        String partNumber = "Part Number";
        String orderNumber = "Order Number";
        LocalDate recordedTime = LocalDate.of(2024, Month.FEBRUARY, 1);
        Currency currency = new Currency("USD");

        Part part = new Part();
        part.setModelCode(modelCode);
        part.setPartNumber(partNumber);
        part.setOrderNumber(orderNumber);
        part.setRecordedTime(recordedTime);
        part.setCurrency(currency);
        partRepository.save(part);

        Part result = partService.getPart(modelCode, partNumber, orderNumber, recordedTime, currency.getCurrency());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(modelCode, result.getModelCode());
        Assertions.assertEquals(partNumber, result.getPartNumber());
        Assertions.assertEquals(orderNumber, result.getOrderNumber());
        Assertions.assertEquals(recordedTime.getYear(), result.getRecordedTime().getYear());
        Assertions.assertEquals(recordedTime.getMonth(), result.getRecordedTime().getMonth());
        Assertions.assertEquals(currency.getCurrency(), result.getCurrency().getCurrency());
    }

    @Test
    public void testGetAverageDealerNet() {
        String region = "Region";
        String clazz = "Class 1";
        String series = "Series";

        double averageDealerNet = 0;
        int numberOfParts = 10;
        Random random = new Random();

        List<Part> savingParts = new ArrayList<>();
        for(int i = 0; i < numberOfParts; i++) {
            double nextDouble = random.nextDouble();
            Part part = new Part();
            part.setRegion(region);
            part.setClazz(getClazzByClazzName(clazz));
            part.setSeries(series);
            part.setNetPriceEach(nextDouble);
            savingParts.add(part);

            averageDealerNet += nextDouble;
        }
        partRepository.saveAll(savingParts);
        averageDealerNet = averageDealerNet / numberOfParts;


        // Test case EXISTING Parts with Region + Class + Series
        Double result = partService.getAverageDealerNet(region, clazz, series);
        log.info(result + "");
        Assertions.assertNotNull(result);
        Assertions.assertEquals(
                CurrencyFormatUtils.formatDoubleValue(averageDealerNet, CurrencyFormatUtils.decimalFormatFourDigits),
                CurrencyFormatUtils.formatDoubleValue(result, CurrencyFormatUtils.decimalFormatFourDigits)
        );

        // Test cast NON-EXISTING Parts with Region + Class + Series
        Double notFoundResult = partService.getAverageDealerNet("NOT FOUND", "NOT FOUND", "NOT FOUND");
        Assertions.assertNotNull(notFoundResult);
        Assertions.assertEquals(0, notFoundResult);
    }

    @Test
    public void testGetAllPartNumberByOrderNo() {
        String orderNumber = "Order Number 123";
        int numberOfParts = 10;

        List<Part> savingParts = new ArrayList<>();
        for(int i = 0; i < numberOfParts; i++) {
            Part part = new Part();
            part.setOrderNumber(orderNumber);
            savingParts.add(part);
        }
        partRepository.saveAll(savingParts);

        List<String> result = partService.getAllPartNumbersByOrderNo(orderNumber);
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(numberOfParts, result.size());
    }

    @Test
    public void testGetCurrencyByOrderNo() {
        String orderNumber = "Order Number 123456";
        String currency = "USD";

        Part part = new Part();
        part.setOrderNumber(orderNumber);
        part.setCurrency(new Currency(currency));
        partRepository.save(part);

        Currency result = partService.getCurrencyByOrderNo(orderNumber);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(currency, result.getCurrency());
    }

}
