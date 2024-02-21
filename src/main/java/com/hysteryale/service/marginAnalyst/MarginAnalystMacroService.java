package com.hysteryale.service.marginAnalyst;

import com.hysteryale.model.Currency;
import com.hysteryale.model.ExchangeRate;
import com.hysteryale.model.Region;
import com.hysteryale.model.marginAnalyst.*;
import com.hysteryale.repository.marginAnalyst.*;
import com.hysteryale.service.CurrencyService;
import com.hysteryale.service.ExchangeRateService;
import com.hysteryale.service.RegionService;
import com.hysteryale.utils.CurrencyFormatUtils;
import com.hysteryale.utils.DateUtils;
import com.hysteryale.utils.EnvironmentUtils;
import com.hysteryale.utils.FileUtils;
import com.hysteryale.utils.XLSB.Cell;
import com.hysteryale.utils.XLSB.Row;
import com.hysteryale.utils.XLSB.Sheet;
import com.hysteryale.utils.XLSB.XLSBWorkbook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class MarginAnalystMacroService {
    @Resource
    MarginAnalystMacroRepository marginAnalystMacroRepository;
    @Resource
    CurrencyService currencyService;
    @Resource
    MarginAnalysisAOPRateRepository marginAnalysisAOPRateRepository;
    @Resource
    FreightRepository freightRepository;
    @Resource
    TargetMarginRepository targetMarginRepository;
    @Resource
    WarrantyRepository warrantyRepository;
    @Resource
    ExchangeRateService exchangeRateService;
    @Resource
    RegionService regionService;

    static HashMap<String, String> MACRO_COLUMNS = new HashMap<>();
    static List<MarginAnalystMacro> listMarginData = new ArrayList<>();

    private void getMacroColumns(Row row) {
        for(Cell cell : row.getCellList()) {
            MACRO_COLUMNS.put(cell.getValue(), cell.getCellColumn());
        }
        log.info(MACRO_COLUMNS + "");
    }

    private MarginAnalystMacro mapExcelDataToMarginAnalystMacro(Row row, String strCurrency, LocalDate monthYear, String plant) {
        MarginAnalystMacro marginAnalystMacro = new MarginAnalystMacro();

        double costRMB;

        // Assign values based on plant (due to 2 different format of HYM and SN sheets)
        if(plant.equals("SN")){
            marginAnalystMacro.setPlant(plant);
            marginAnalystMacro.setSeriesCode(row.getCell(MACRO_COLUMNS.get("Series")).getValue());

            marginAnalystMacro.setClazz(row.getCell(MACRO_COLUMNS.get("Class")).getValue());

            String modelCode = row.getCell(MACRO_COLUMNS.get("MODEL CD    (inc \"-\")")).getValue();
            if(modelCode.isEmpty())
                modelCode = row.getCell(MACRO_COLUMNS.get("MODEL CD (incl \"-\")")).getValue();
            marginAnalystMacro.setModelCode(modelCode);

            marginAnalystMacro.setPartNumber(row.getCell(MACRO_COLUMNS.get("Option Code")).getValue());
            marginAnalystMacro.setDescription(row.getCell(MACRO_COLUMNS.get("DESCRIPTION")).getValue());
            marginAnalystMacro.setMonthYear(monthYear);

            costRMB = CurrencyFormatUtils.formatDoubleValue(row.getCell(MACRO_COLUMNS.get("TP USD")).getNumericCellValue(), CurrencyFormatUtils.decimalFormatFourDigits);
        }
        else {
            marginAnalystMacro.setPlant(row.getCell(MACRO_COLUMNS.get("Plant")).getValue());
            marginAnalystMacro.setSeriesCode(row.getCell(MACRO_COLUMNS.get("Series Code")).getValue());
            marginAnalystMacro.setClazz(row.getCell(MACRO_COLUMNS.get("Class")).getValue());
            marginAnalystMacro.setModelCode(row.getCell(MACRO_COLUMNS.get("Model Code")).getValue());
            marginAnalystMacro.setPartNumber(row.getCell(MACRO_COLUMNS.get("Option Code")).getValue());
            marginAnalystMacro.setDescription(row.getCell(MACRO_COLUMNS.get("Description")).getValue());
            marginAnalystMacro.setMonthYear(monthYear);

            costRMB = CurrencyFormatUtils.formatDoubleValue(row.getCell(MACRO_COLUMNS.get("Add on Cost RMB")).getNumericCellValue(), CurrencyFormatUtils.decimalFormatFourDigits);
        }

        // Set currency
        Currency currency = currencyService.getCurrenciesByName(strCurrency);
        marginAnalystMacro.setCurrency(currency);

        // Numeric values
        marginAnalystMacro.setCostRMB(costRMB);

        return marginAnalystMacro;
    }

    // Import Macro from a file
    public void importMarginAnalystMacroFromFile(String fileName, String filePath) {
        // Extract monthYear from fileName pattern
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

        log.info("Reading " + fileName);
        XLSBWorkbook workbook = new XLSBWorkbook();

        String[] macroSheets = {"AUD HYM Ruyi Staxx", "USD HYM Ruyi Staxx", "SN USD Asia Template", "SN USD Pacific Template", "SN AUD Template"};
        for(String macroSheet : macroSheets) {
            log.info("Importing " + macroSheet);

            String currency = macroSheet.contains("USD") ? "USD" : "AUD";
            String plant  = macroSheet.contains("SN") ? "SN" : "HYM";
            int columnNameRow = plant.equals("SN") ? 8 : 0;
            String strRegion = "";

            if(plant.equals("SN")) {
                if(macroSheet.contains("USD"))
                    strRegion = macroSheet.contains("Asia") ? "Asia" : "Pacific";
                listMarginData = marginAnalystMacroRepository.loadListMacroData(plant, currency, monthYear, strRegion);
            }
            else
                listMarginData = marginAnalystMacroRepository.loadListHYMMacroData(currency, monthYear);

            log.info("Size: " + listMarginData.size());

            try {
                workbook.openFile(filePath);
                Sheet sheet = workbook.getSheet(macroSheet);

                List<MarginAnalystMacro> marginAnalystMacroList = new ArrayList<>();
                log.info("Num of rows: " + sheet.getRowList().size());
                for(Row row : sheet.getRowList()) {
                    if(row.getRowNum() == columnNameRow) {
                        log.info("Column name row: " + columnNameRow);
                        MACRO_COLUMNS.clear();
                        getMacroColumns(row);

                        // Save MarginAnalysisAOPRate
                        saveMarginAnalysisAOPRate(sheet, currency, monthYear, "monthly");
                        saveMarginAnalysisAOPRate(sheet, currency, monthYear, "annually");
                    }
                    else if(row.getRowNum() > columnNameRow) {
                        MarginAnalystMacro marginAnalystMacro = mapExcelDataToMarginAnalystMacro(row, currency, monthYear, plant);
                        marginAnalystMacro.setRegion(strRegion);
                        MarginAnalystMacro existedMacro = isMacroExisted(row, plant, currency, monthYear, strRegion);

                        if(existedMacro == null) {
                            marginAnalystMacroList.add(marginAnalystMacro);
                        }
                        else {
                            MarginAnalystMacro dbMacro = updateMacro(existedMacro, marginAnalystMacro);
                            marginAnalystMacroList.add(dbMacro);
                        }
                    }
                }
                // Save MarginAnalystMacro
                log.info("MarginAnalystMacro saved: " + marginAnalystMacroList.size());
                marginAnalystMacroRepository.saveAll(marginAnalystMacroList);
                marginAnalystMacroList.clear();

            } catch (Exception e) {
                log.error(e.getMessage());
            }

            listMarginData.clear();
        }
        importFreightFromFile(filePath, monthYear);
        importTargetMarginFromFile(filePath, monthYear);
        importWarrantyFromFile(filePath, monthYear);
    }

    /**
     * Import Freight data from Macro file (from 'Freight' tab)
     */
    public void importFreightFromFile(String filePath, LocalDate monthYear) {
        try{
            XLSBWorkbook workbook = new XLSBWorkbook();
            workbook.openFile(filePath);

            HashMap<String, String> columnMap = new HashMap<>();
            Sheet sheet = workbook.getSheet("Freight");
            for(Row row : sheet.getRowList()) {
                if(row.getRowNum() == 0) {
                    for(Cell cell : row.getCellList())
                        columnMap.put(cell.getValue(), cell.getCellColumn());
                }
                else {
                    String metaSeries = row.getCell(columnMap.get("Meta Sers for China")).getValue();
                    double freightValue = row.getCell(columnMap.get("Total")).getNumericCellValue();
                    Freight freight = new Freight(metaSeries, freightValue, monthYear);

                    Optional<Freight> optionalFreight = freightRepository.getFreight(metaSeries, monthYear);
                    optionalFreight.ifPresent(value -> freight.setId(value.getId()));

                    freightRepository.save(freight);
                }
            }
        } catch (Exception exception) {
            log.error(exception.getMessage());
        }
    }

    /**
     * Import TargetMargin and Margin Guideline from Macro file (in 'AOPF 2023' tab)
     */
    public void importTargetMarginFromFile(String filePath, LocalDate monthYear) {
        try {
            XLSBWorkbook workbook = new XLSBWorkbook();
            workbook.openFile(filePath);

            HashMap<String, String> columnMap = new HashMap<>();
            List<TargetMargin> targetMarginList = new ArrayList<>();

            Sheet sheet = workbook.getAOPFSheet();
            for(Row row : sheet.getRowList()) {
                if(row.getRowNum() == 0) {
                    for(Cell cell : row.getCellList())
                        columnMap.put(cell.getValue(), cell.getCellColumn());
                }
                else {
                    // Map the Target Margin value from AOPF 2023
                    String strRegion = row.getCell(columnMap.get("Region")).getValue();
                    String metaSeries = row.getCell(columnMap.get("Series")).getValue();
                    double stdMarginPercentage = row.getCell(columnMap.get("Margin % STD")).getNumericCellValue();

                    Region region = regionService.getRegionByName(strRegion);
                    if(region == null)
                        continue;
                    TargetMargin targetMargin = new TargetMargin(region, metaSeries, monthYear, stdMarginPercentage);

                    Optional<TargetMargin> optionalTargetMargin = targetMarginRepository.getTargetMargin(region.getRegionName(), metaSeries, monthYear);
                    optionalTargetMargin.ifPresent(margin -> targetMargin.setId(margin.getId()));

                    targetMarginList.add(targetMargin);
                }
            }
            targetMarginRepository.saveAll(targetMarginList );
        } catch(Exception exception) {
            log.error(exception.getMessage());
        }
    }

    /**
     * Import Warranty from Macro file (in 'Mappting' tab)
     */
    public void importWarrantyFromFile(String filePath, LocalDate monthYear) {
        try {
            XLSBWorkbook workbook = new XLSBWorkbook();
            workbook.openFile(filePath);
            Sheet sheet = workbook.getSheet("Mappting");

            // Getting Warranty values
            for (int i = 7; i <= 12; i++) {
                String clazz = sheet.getRow(i).getCell("A").getValue();
                double warrantyValue = sheet.getRow(i).getCell("B").getNumericCellValue();

                Warranty warranty = new Warranty(clazz, monthYear, warrantyValue);
                Optional<Warranty> optionalWarranty = warrantyRepository.getWarranty(clazz, monthYear);
                optionalWarranty.ifPresent(value -> warranty.setId(value.getId()));

                warrantyRepository.save(warranty);
            }

            // Getting Exchange Rate value between CNY (RMB), USD and AUD
            Pattern pattern = Pattern.compile("(\\w{3}) to (\\w{3})");
            for(int i = 1; i <= 4; i++) {
                Matcher matcher = pattern.matcher(sheet.getRow(i).getCell("A").getValue());
                if(matcher.find()) {
                    Currency fromCurrency = currencyService.getCurrenciesByName(matcher.group(1));
                    Currency toCurrency = currencyService.getCurrenciesByName(matcher.group(2));
                    double annuallyRate = sheet.getRow(i).getCell("B").getNumericCellValue();

                    if(exchangeRateService.getExchangeRate(fromCurrency.getCurrency(), toCurrency.getCurrency(), monthYear) == null) {
                        ExchangeRate exchangeRate = new ExchangeRate(fromCurrency, toCurrency, annuallyRate, monthYear);
                        exchangeRateService.saveExchangeRate(exchangeRate);
                    }
                }
            }

        } catch (Exception exception) {
            log.error(exception.getMessage());
        }
    }


    // Import all Macro file in directory
    public void importMarginAnalystMacro() {
        String folderPath = EnvironmentUtils.getEnvironmentValue("import-files.base-folder") + EnvironmentUtils.getEnvironmentValue("import-files.margin_macro");

        List<String> files = FileUtils.getAllFilesInFolder(folderPath);
        for(String fileName : files) {
            importMarginAnalystMacroFromFile(fileName, folderPath + "/" + fileName);
        }
    }

    private MarginAnalystMacro isMacroExisted(Row row, String plant, String currency, LocalDate monthYear, String strRegion) {
        String modelCode;
        String partNumber = row.getCell(MACRO_COLUMNS.get("Option Code")).getValue();
        String description;

        if(plant.equals("HYM")) {
            modelCode = row.getCell(MACRO_COLUMNS.get("Model Code")).getValue();
            description = row.getCell(MACRO_COLUMNS.get("Description")).getValue();
        }
        else {
            description = row.getCell(MACRO_COLUMNS.get("DESCRIPTION")).getValue();
            modelCode = row.getCell(MACRO_COLUMNS.get("MODEL CD    (inc \"-\")")).getValue();
            if(modelCode.isEmpty())
                modelCode = row.getCell(MACRO_COLUMNS.get("MODEL CD (incl \"-\")")).getValue();
        }
        for(MarginAnalystMacro macro : listMarginData) {
            LocalDate dbMonthYear = macro.getMonthYear();
            if(
                    macro.getModelCode().equals(modelCode)
                    && macro.getPartNumber().equals(partNumber)
                    && macro.getCurrency().getCurrency().equals(currency)
                    && dbMonthYear.getYear() == monthYear.getYear()
                    && dbMonthYear.getMonthValue() == monthYear.getMonthValue()
                    && macro.getRegion().equals(strRegion)
                    && macro.getDescription().equals(description)
            )
                return macro;
        }
        return null;
    }

    private MarginAnalystMacro updateMacro(MarginAnalystMacro dbMacro, MarginAnalystMacro fileMacro) {
        fileMacro.setId(dbMacro.getId());
        return fileMacro;
    }


    /**
     * Get Margin Analysis @ AOP Rate from Excel sheet: 'USD HYM Ruyi Staxx', 'SN AUD Template' and 'AUD HYM Ruyi Staxx'
     */
    void saveMarginAnalysisAOPRate(Sheet sheet, String currency, LocalDate monthYear, String durationUnit) {

        if(sheet.getSheetName().equals("USD HYM Ruyi Staxx") || sheet.getSheetName().equals("SN AUD Template") ||sheet.getSheetName().equals("AUD HYM Ruyi Staxx")) {
            MarginAnalysisAOPRate marginAnalysisAOPRate = new MarginAnalysisAOPRate();
            String plant = sheet.getSheetName().contains("SN") ? "SN" : "HYM";

            double aopRate;
            double costUplift;
            double addWarranty;
            double surcharge;
            double duty;
            double freight;

            String cellIndex;
            int rowIndex;

            if (sheet.getSheetName().equals("SN AUD Template")) {
                cellIndex = durationUnit.equals("annually") ? "AF" : "AI";
                rowIndex = 1;

            }
            else {
                cellIndex = durationUnit.equals("annually") ? "V" : "Y";
                rowIndex = 0;

            }
            aopRate = sheet.getRow(rowIndex).getCell(cellIndex).getNumericCellValue();
            costUplift = sheet.getRow(rowIndex + 2).getCell(cellIndex).getNumericCellValue();
            surcharge = sheet.getRow(rowIndex + 4).getCell(cellIndex).getNumericCellValue();
            addWarranty = sheet.getRow(rowIndex + 3).getCell(cellIndex).getNumericCellValue();
            duty = sheet.getRow(rowIndex + 5).getCell(cellIndex).getNumericCellValue();
            freight = sheet.getRow(rowIndex + 6).getCell(cellIndex).getNumericCellValue();

            marginAnalysisAOPRate.setMonthYear(monthYear);
            marginAnalysisAOPRate.setPlant(plant);

            marginAnalysisAOPRate.setAopRate(aopRate);
            marginAnalysisAOPRate.setCostUplift(costUplift);
            marginAnalysisAOPRate.setAddWarranty(addWarranty);
            marginAnalysisAOPRate.setSurcharge(surcharge);
            marginAnalysisAOPRate.setDuty(duty);
            marginAnalysisAOPRate.setFreight(freight);
            marginAnalysisAOPRate.setCurrency(currencyService.getCurrenciesByName(currency));
            marginAnalysisAOPRate.setDurationUnit(durationUnit);

            Optional<MarginAnalysisAOPRate> optional = marginAnalysisAOPRateRepository.getMarginAnalysisAOPRate(plant, currency, monthYear, durationUnit);
            if(optional.isEmpty())
                marginAnalysisAOPRateRepository.save(marginAnalysisAOPRate);
            else {
                MarginAnalysisAOPRate dbAOPRate = optional.get();
                marginAnalysisAOPRate.setId(dbAOPRate.getId());
                marginAnalysisAOPRateRepository.save(marginAnalysisAOPRate);
            }
        }

    }

    public Optional<MarginAnalystMacro> getMarginAnalystMacroByMonthYear(String modelCode, String partNumber, String strCurrency, LocalDate monthYear) {
        return marginAnalystMacroRepository.getMarginAnalystMacroByMonthYear(modelCode, partNumber, strCurrency, monthYear);
    }

    public Double getManufacturingCost(String modelCode, String partNumber, String strCurrency, List<String> plants, LocalDate monthYear) {
        return marginAnalystMacroRepository.getManufacturingCost(modelCode, partNumber, strCurrency, plants, monthYear);
    }

    public List<MarginAnalystMacro> getMarginAnalystMacroByPlantAndListPartNumber(String modelCode, List<String> partNumber, String strCurrency, String plant, LocalDate monthYear) {
        return marginAnalystMacroRepository.getMarginAnalystMacroByPlantAndListPartNumber(modelCode, partNumber, strCurrency, plant, monthYear);
    }
    public List<MarginAnalystMacro> getMarginAnalystMacroByHYMPlantAndListPartNumber(String modelCode, List<String> partNumber, String currency, LocalDate monthYear) {
        return marginAnalystMacroRepository.getMarginAnalystMacroByHYMPlantAndListPartNumber(modelCode, partNumber, currency, monthYear);
    }

    /**
     * Get Freight value if existed else return 0
     */
    public double getFreightValue(String metaSeries, LocalDate monthYear) {
        Optional<Freight> optionalFreight = freightRepository.getFreight(metaSeries, monthYear);
        return optionalFreight.map(Freight::getFreight).orElse(0.0);
    }

    /**
     * Get Warranty value if existed else return 0
     */
    public double getWarrantyValue(String clazz, LocalDate monthYear) {
        Optional<Warranty> optionalWarranty = warrantyRepository.getWarranty(clazz, monthYear);
        return optionalWarranty.map(Warranty::getWarranty).orElse(0.0);
    }

    /**
     * Get Target Margin % if existed else return 0
     */
    public double getTargetMarginValue(String region, String metaSeries, LocalDate monthYear) {
        Optional<TargetMargin> optionalTargetMargin = targetMarginRepository.getTargetMargin(region, metaSeries, monthYear);
        return optionalTargetMargin.map(TargetMargin::getStdMarginPercentage).orElse(0.0);
    }

    /**
     * Get class of the Model Code
     */
    public String getClassByModelCode(String modelCode) {
        return marginAnalystMacroRepository.getClassByModelCode(modelCode);
    }
}
