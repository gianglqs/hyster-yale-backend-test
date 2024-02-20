package com.hysteryale.service.marginAnalyst;

import com.hysteryale.model.Region;
import com.hysteryale.model.marginAnalyst.Freight;
import com.hysteryale.model.marginAnalyst.MarginAnalystMacro;
import com.hysteryale.model.marginAnalyst.TargetMargin;
import com.hysteryale.model.marginAnalyst.Warranty;
import com.hysteryale.repository.marginAnalyst.FreightRepository;
import com.hysteryale.repository.marginAnalyst.MarginAnalystMacroRepository;
import com.hysteryale.repository.marginAnalyst.TargetMarginRepository;
import com.hysteryale.repository.marginAnalyst.WarrantyRepository;
import com.hysteryale.utils.CurrencyFormatUtils;
import com.hysteryale.utils.DateUtils;
import com.hysteryale.utils.XLSB.Cell;
import com.hysteryale.utils.XLSB.Row;
import com.hysteryale.utils.XLSB.Sheet;
import com.hysteryale.utils.XLSB.XLSBWorkbook;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.xml.sax.SAXException;
import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest
@Slf4j
public class MarginAnalystMacroServiceTest {
    @Resource
    MarginAnalystMacroService marginAnalystMacroService;
    @Resource
    FreightRepository freightRepository;
    @Resource
    WarrantyRepository warrantyRepository;
    @Resource
    TargetMarginRepository targetMarginRepository;
    @Resource
    MarginAnalystMacroRepository marginAnalystMacroRepository;
    HashMap<String, String> MACRO_COLUMNS = new HashMap<>();

    @Test
    public void testImportMarginAnalystMacroFromFile() throws OpenXML4JException, IOException, SAXException {
        String fileName = "USD AUD Margin Analysis Template Macro_Oct  2023 Rev.xlsb";
        String filePath = "import_files/margin_analyst_data/USD AUD Margin Analysis Template Macro_Oct  2023 Rev.xlsb";
        marginAnalystMacroService.importMarginAnalystMacroFromFile(fileName, filePath);

        Pattern pattern = Pattern.compile(".* Macro_(\\w{3})\\s*(\\d{4}).*");
        Matcher matcher = pattern.matcher(fileName);
        String month;
        int year;

        if(matcher.find()) {
            month = matcher.group(1);
            year = Integer.parseInt(matcher.group(2));
        }
        else
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File name is not in appropriate format");

        LocalDate monthYear = LocalDate.of(year, DateUtils.getMonth(month), 1);

        Random random = new Random();

        XLSBWorkbook workbook = new XLSBWorkbook();
        String[] macroSheets = {"AUD HYM Ruyi Staxx", "USD HYM Ruyi Staxx", "SN USD Asia Template", "SN USD Pacific Template", "SN AUD Template"};

        for(String sheetName : macroSheets) {
            workbook.openFile(filePath);
            Sheet excelSheet = workbook.getSheet(sheetName);

            int columnNameRow = sheetName.contains("SN") ? 8 : 0;
            MACRO_COLUMNS.clear();
            getMacroColumns(excelSheet.getRow(columnNameRow));

            String region = "";

            if(sheetName.contains("SN"))
                if(sheetName.contains("USD"))
                    region = sheetName.contains("Asia") ? "Asia" : "Pacific";

            for(int i = 0; i < 10; i++) {
                int nextRow = random.nextInt(2000) + 10;
                Row row = excelSheet.getRow(nextRow);
                assertImportedData(row, sheetName, monthYear, region);
            }
        }
    }

    private void getMacroColumns(Row row) {
        for(Cell cell : row.getCellList()) {
            MACRO_COLUMNS.put(cell.getValue(), cell.getCellColumn());
        }
        log.info(MACRO_COLUMNS + "");
    }

    private void assertImportedData(Row row, String sheetName, LocalDate monthYear, String region) {
        String plant;
        String seriesCode;
        String clazz;
        String modelCode;
        String optionCode;
        String description;
        double costRMB;

        if(sheetName.contains("SN")){
            plant = "SN";
            seriesCode = row.getCell(MACRO_COLUMNS.get("Series")).getValue();
            clazz = row.getCell(MACRO_COLUMNS.get("Class")).getValue();

            modelCode = row.getCell(MACRO_COLUMNS.get("MODEL CD    (inc \"-\")")).getValue();
            if(modelCode.isEmpty())
                modelCode = row.getCell(MACRO_COLUMNS.get("MODEL CD (incl \"-\")")).getValue();

            optionCode = row.getCell(MACRO_COLUMNS.get("Option Code")).getValue();
            description = row.getCell(MACRO_COLUMNS.get("DESCRIPTION")).getValue();
            costRMB = CurrencyFormatUtils.formatDoubleValue(row.getCell(MACRO_COLUMNS.get("TP USD")).getNumericCellValue(), CurrencyFormatUtils.decimalFormatFourDigits);
        }
        else {
            plant = row.getCell(MACRO_COLUMNS.get("Plant")).getValue();
            seriesCode = row.getCell(MACRO_COLUMNS.get("Series Code")).getValue();
            clazz = row.getCell(MACRO_COLUMNS.get("Class")).getValue();
            modelCode = row.getCell(MACRO_COLUMNS.get("Model Code")).getValue();
            optionCode = row.getCell(MACRO_COLUMNS.get("Option Code")).getValue();
            description = row.getCell(MACRO_COLUMNS.get("Description")).getValue();
            costRMB = CurrencyFormatUtils.formatDoubleValue(row.getCell(MACRO_COLUMNS.get("Add on Cost RMB")).getNumericCellValue(), CurrencyFormatUtils.decimalFormatFourDigits);
        }

        String strCurrency = sheetName.contains("USD") ? "USD" : "AUD";

        Optional<MarginAnalystMacro> optionalMacro = marginAnalystMacroRepository.getMacroForTesting(modelCode, optionCode, strCurrency, monthYear, description, clazz, region);
        Assertions.assertTrue(optionalMacro.isPresent());
        MarginAnalystMacro macro = optionalMacro.get();

        Assertions.assertEquals(plant, macro.getPlant());
        Assertions.assertEquals(seriesCode, macro.getSeriesCode());
        Assertions.assertEquals(clazz, macro.getClazz());
        Assertions.assertEquals((int) costRMB, (int) macro.getCostRMB());
    }

    @Test
    public void testGetFreightValue() {
        String metaSeries = "abc";
        double expectedFreight = 123;
        LocalDate monthYear = LocalDate.of(2024, Month.JANUARY, 1);

        // Test case Freight is existed
        freightRepository.save(new Freight(metaSeries, expectedFreight, monthYear));
        double result = marginAnalystMacroService.getFreightValue(metaSeries, monthYear);
        Assertions.assertEquals(expectedFreight, result);

        // Test case Freight is not existed
        double notFoundResult = marginAnalystMacroService.getFreightValue("not found freight", monthYear);
        Assertions.assertEquals(0, notFoundResult);
    }

    @Test
    public void testGetWarranty() {
        String clazz = "Class Test";
        LocalDate monthYear = LocalDate.of(2024, Month.JANUARY, 1);
        double expectedWarranty = 123;

        // Test case Warranty is existed
        warrantyRepository.save(new Warranty(clazz, monthYear, expectedWarranty));
        double result = marginAnalystMacroService.getWarrantyValue(clazz, monthYear);
        Assertions.assertEquals(expectedWarranty, result);

        // Test case Warranty is not existed
        double notFoundResult = marginAnalystMacroService.getWarrantyValue("Not Found Class", monthYear);
        Assertions.assertEquals(0, notFoundResult);
    }

    @Test
    public void testGetTargetMargin() {
        String region = "Asia";
        String metaSeries = "MetaSeries Test";
        LocalDate monthYear = LocalDate.of(2024, Month.JANUARY, 1);
        double expectedTargetMargin = 100;

        // Test case TargetMargin is existed
        targetMarginRepository.save(new TargetMargin(new Region(1, "Asia", "A"), metaSeries, monthYear, expectedTargetMargin));
        double result = marginAnalystMacroService.getTargetMarginValue(region, metaSeries, monthYear);
        Assertions.assertEquals(expectedTargetMargin, result);

        // Test case TargetMargin is not existed
        double notFoundResult = marginAnalystMacroService.getTargetMarginValue("Not Found Region", "Not Found Meta Series", monthYear);
        Assertions.assertEquals(0, notFoundResult);
    }

    @Test
    public void testGetClassByModelCode() {
        String clazz = "Class Test";
        String modelCode = "Model Code Test";

        MarginAnalystMacro macro = new MarginAnalystMacro();
        macro.setModelCode(modelCode);
        macro.setClazz(clazz);
        marginAnalystMacroRepository.save(macro);

        String result = marginAnalystMacroService.getClassByModelCode(modelCode);
        Assertions.assertEquals(clazz, result);
    }
}
