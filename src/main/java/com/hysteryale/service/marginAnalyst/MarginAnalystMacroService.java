package com.hysteryale.service.marginAnalyst;

import com.hysteryale.model.Currency;
import com.hysteryale.model.marginAnalyst.MarginAnalysisAOPRate;
import com.hysteryale.model.marginAnalyst.MarginAnalystMacro;
import com.hysteryale.repository.marginAnalyst.MarginAnalysisAOPRateRepository;
import com.hysteryale.repository.marginAnalyst.MarginAnalystMacroRepository;
import com.hysteryale.service.CurrencyService;
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

    static HashMap<String, String> MACRO_COLUMNS = new HashMap<>();

    private void getMacroColumns(Row row) {
        for(Cell cell : row.getCellList()) {
            MACRO_COLUMNS.put(cell.getValue(), cell.getCellColumn());
        }
        log.info(MACRO_COLUMNS + "");
    }

    private MarginAnalystMacro mapExcelDataToMarginAnalystMacro(Row row, String strCurrency, Calendar monthYear, String plant) {
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
        Pattern pattern = Pattern.compile(".* Macro_(\\w{3}) .*");
        Matcher matcher = pattern.matcher(fileName);
        int year = 2023;
        String month;

        if(matcher.find()) {
            month = matcher.group(1);
        }
        else
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File name is not in appropriate format");

        Calendar monthYear = Calendar.getInstance();
        monthYear.set(year, DateUtils.monthMap.get(month) -1, 1);

        log.info("Reading " + fileName);
        XLSBWorkbook workbook = new XLSBWorkbook();

        String[] macroSheets = {"AUD HYM Ruyi Staxx", "USD HYM Ruyi Staxx", "SN USD Asia Template", "SN USD Pacific Template", "SN AUD Template"};
        for(String macroSheet : macroSheets) {
            log.info("Importing " + macroSheet);

            String currency = macroSheet.contains("USD") ? "USD" : "AUD";
            String plant  = macroSheet.contains("SN") ? "SN" : "HYM";
            int columnNameRow = plant.equals("SN") ? 8 : 0;

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
                        Optional<MarginAnalystMacro> optionalMarginAnalystMacro = getMarginAnalystMacro(row, plant, currency, monthYear);

                        if(optionalMarginAnalystMacro.isEmpty()) {
                            marginAnalystMacroList.add(marginAnalystMacro);
                        }
                        else {
                            MarginAnalystMacro dbMacro = updateMacro(optionalMarginAnalystMacro.get(), marginAnalystMacro);
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

    private Optional<MarginAnalystMacro> getMarginAnalystMacro(Row row, String plant, String currency, Calendar monthYear) {
        String modelCode;
        String partNumber = row.getCell(MACRO_COLUMNS.get("Option Code")).getValue();

        if(plant.equals("HYM"))
            modelCode = row.getCell(MACRO_COLUMNS.get("Model Code")).getValue();
        else {
            modelCode = row.getCell(MACRO_COLUMNS.get("MODEL CD    (inc \"-\")")).getValue();
            if(modelCode.isEmpty())
                modelCode = row.getCell(MACRO_COLUMNS.get("MODEL CD (incl \"-\")")).getValue();
        }
//        log.info(modelCode + " - " + partNumber + " -  " + currency + " - " + monthYear.get(Calendar.MONTH));
        return marginAnalystMacroRepository.getMarginAnalystMacroByMonthYear(modelCode, partNumber, currency, monthYear);
    }

    private MarginAnalystMacro updateMacro(MarginAnalystMacro dbMacro, MarginAnalystMacro fileMacro) {
        fileMacro.setId(dbMacro.getId());
        return fileMacro;
    }


    /**
     * Get Margin Analysis @ AOP Rate from Excel sheet: 'USD HYM Ruyi Staxx', 'SN AUD Template' and 'AUD HYM Ruyi Staxx'
     */
    void saveMarginAnalysisAOPRate(Sheet sheet, String currency, Calendar monthYear, String durationUnit) {

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

    public Optional<MarginAnalystMacro> getMarginAnalystMacroByMonthYear(String modelCode, String partNumber, String strCurrency, Calendar monthYear) {
        return marginAnalystMacroRepository.getMarginAnalystMacroByMonthYear(modelCode, partNumber, strCurrency, monthYear);
    }

    public Double getManufacturingCost(String modelCode, String partNumber, String strCurrency, List<String> plants, Calendar monthYear) {
        return marginAnalystMacroRepository.getManufacturingCost(modelCode, partNumber, strCurrency, plants, monthYear);
    }

    public List<MarginAnalystMacro> getMarginAnalystMacroByPlantAndListPartNumber(String modelCode, List<String> partNumber, String strCurrency, String plant, Calendar monthYear) {
        return marginAnalystMacroRepository.getMarginAnalystMacroByPlantAndListPartNumber(modelCode, partNumber, strCurrency, plant, monthYear);
    }
    public List<MarginAnalystMacro> getMarginAnalystMacroByHYMPlantAndListPartNumber(String modelCode, List<String> partNumber, String currency, Calendar monthYear) {
        return marginAnalystMacroRepository.getMarginAnalystMacroByHYMPlantAndListPartNumber(modelCode, partNumber, currency, monthYear);
    }
}
