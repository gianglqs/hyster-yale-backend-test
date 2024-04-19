/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.service.marginAnalyst;

import com.hysteryale.exception.IncorectFormatCellException;
import com.hysteryale.exception.MissingColumnException;
import com.hysteryale.exception.SeriesNotFoundException;
import com.hysteryale.model.Booking;
import com.hysteryale.model.marginAnalyst.MarginAnalysisAOPRate;
import com.hysteryale.model_h2.MarginData;
import com.hysteryale.model_h2.MarginSummary;
import com.hysteryale.model_h2.MarginDataId;
import com.hysteryale.model_h2.MarginSummaryId;
import com.hysteryale.repository.ProductRepository;
import com.hysteryale.repository.marginAnalyst.MarginAnalysisAOPRateRepository;
import com.hysteryale.repository_h2.MarginDataRepository;
import com.hysteryale.repository_h2.MarginSummaryRepository;
import com.hysteryale.service.BookingService;
import com.hysteryale.service.ExchangeRateService;
import com.hysteryale.service.FileUploadService;
import com.hysteryale.utils.CheckRequiredColumnUtils;
import com.hysteryale.utils.CurrencyFormatUtils;
import com.hysteryale.utils.EnvironmentUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
@EnableTransactionManagement
public class MarginDataService {
    @Resource
    MarginDataRepository marginDataRepository;
    @Resource
    MarginAnalystMacroService marginAnalystMacroService;
    @Resource
    FileUploadService fileUploadService;
    @Resource
    MarginAnalysisAOPRateRepository marginAnalysisAOPRateRepository;
    @Resource
    BookingService bookingService;
    @Resource
    ExchangeRateService exchangeRateService;
    @Resource
    ProductRepository productRepository;
    @Resource
    MarginSummaryRepository marginSummaryRepository;

    static HashMap<String, Integer> COLUMN_NAME = new HashMap<>();

    void getColumnName(Row row) {
        for(Cell cell : row) {
            String columnName = cell.getStringCellValue();
            COLUMN_NAME.put(columnName, cell.getColumnIndex());
        }
        log.info("Column Name: {}", COLUMN_NAME);
    }

    /**
     * Get ManufacturingCost value based on plant
     * if plant == HYM or SN -> then find the latest manufacturingCost(or CostRMB) in Macro
     * else plant == [EU_Plant] -> then getting from BookingOrder (Cost_Data file)
     */
    double getManufacturingCost(String modelCode, String partNumber, String strCurrency, String plant, double dealerNet, double exchangeRate, String region) {
        List<String> plantList;
        Double manufacturingCost;
        if(plant.equals("SN")) {
            plantList = new ArrayList<>(List.of("SN"));

            // SN USD -> then use extended params REGION for getting Manufacturing Cost
            if(strCurrency.equals("USD")) manufacturingCost = marginAnalystMacroService.getSNManufacturingCost(modelCode, partNumber, strCurrency, plantList, region);
            else manufacturingCost = marginAnalystMacroService.getManufacturingCost(modelCode, partNumber, strCurrency, plantList);
        } else {
            plantList = new ArrayList<>(List.of("HYM", "Ruyi", "Staxx", "Maximal"));
            manufacturingCost = marginAnalystMacroService.getManufacturingCost(modelCode, partNumber, strCurrency, plantList);
        }

        // if manufacturingCost is null -> it will equal 90% of DealerNet
        return Objects.requireNonNullElseGet(manufacturingCost, () -> plant.equals("HYM")
                ? (dealerNet / exchangeRate) * 0.9
                : dealerNet * 0.9);
    }

    private void verifyNOVOCellFormat(Row row, String fileUUID) throws IncorectFormatCellException {
        // Verify the cells' format
        Cell modelCodeCell = row.getCell(COLUMN_NAME.get("Model Code"));
        if(modelCodeCell.getCellType() != CellType.STRING)
            throw new IncorectFormatCellException((row.getRowNum() + 1) + ":" + (COLUMN_NAME.get("Model Code") + 1), fileUUID);

        Cell partNumberCell = row.getCell(COLUMN_NAME.get("Part Number"));
        if(partNumberCell.getCellType() != CellType.STRING)
            throw new IncorectFormatCellException((row.getRowNum() + 1) + ":" + (COLUMN_NAME.get("Part Number") + 1), fileUUID);

        Cell descriptionCell = row.getCell(COLUMN_NAME.get("Part Description"));
        if(descriptionCell.getCellType() != CellType.STRING)
            throw new IncorectFormatCellException((row.getRowNum() + 1) + ":" + (COLUMN_NAME.get("Part Description") + 1), fileUUID);

        Cell listPriceCell = row.getCell(COLUMN_NAME.get("List Price"));
        if(listPriceCell.getCellType() != CellType.NUMERIC)
            throw new IncorectFormatCellException((row.getRowNum() + 1) + ":" + (COLUMN_NAME.get("List Price") + 1), fileUUID);

        Cell netPriceCell = row.getCell(COLUMN_NAME.get("Net Price Each"));
        if(netPriceCell.getCellType() != CellType.NUMERIC)
            throw new IncorectFormatCellException((row.getRowNum() + 1) + ":" + (COLUMN_NAME.get("Net Price Each") + 1), fileUUID);

        Cell typeCell = row.getCell(COLUMN_NAME.get("#"));
        if(typeCell.getCellType() != CellType.NUMERIC)
            throw new IncorectFormatCellException((row.getRowNum() + 1) + ":" + (COLUMN_NAME.get("#") + 1), fileUUID);

        Cell seriesCodeCell = row.getCell(COLUMN_NAME.get("Series Code"));
        if(seriesCodeCell.getCellType() != CellType.STRING)
            throw new IncorectFormatCellException((row.getRowNum() + 1) + ":" + (COLUMN_NAME.get("Series Code") + 1), fileUUID);

        Cell quoteNumberCell = row.getCell(COLUMN_NAME.get("Quote Number:"));
        if(quoteNumberCell.getCellType() != CellType.STRING)
            throw new IncorectFormatCellException((row.getRowNum() + 1) + ":" + (COLUMN_NAME.get("Quote Number:") + 1), fileUUID);
    }

    /**
     * Mapping the data from uploaded files / template files as SN_AUD ... into a model
     */
    private MarginData mapIMMarginAnalystData(Row row, String plant, String strCurrency, String region, int userId) {
        // Initialize variables
        double aopRate = 1;
        double costUplift = 0.0;

        // Assign value for variables if existed
        String queryPlant = plant.equals("SN") ? "SN" : "HYM";
        Optional<MarginAnalysisAOPRate> optionalMarginAnalysisAOPRate = getLatestMarginAnalysisAOPRate(strCurrency, queryPlant, "annually");
        if(optionalMarginAnalysisAOPRate.isPresent()) {
            MarginAnalysisAOPRate marginAnalysisAOPRate = optionalMarginAnalysisAOPRate.get();
            aopRate = marginAnalysisAOPRate.getAopRate();
            costUplift = marginAnalysisAOPRate.getCostUplift();
        }

        String modelCode = row.getCell(COLUMN_NAME.get("Model Code")).getStringCellValue();
        String partNumber = row.getCell(COLUMN_NAME.get("Part Number")).getStringCellValue();
        String description = row.getCell(COLUMN_NAME.get("Part Description"), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
        double listPrice = row.getCell(COLUMN_NAME.get("List Price")).getNumericCellValue();
        double netPrice = row.getCell(COLUMN_NAME.get("Net Price Each")).getNumericCellValue();
        int type = (int) row.getCell(COLUMN_NAME.get("#")).getNumericCellValue();
        String series = row.getCell(COLUMN_NAME.get("Series Code"), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
        String quoteNumber = row.getCell(COLUMN_NAME.get("Quote Number:")).getStringCellValue();

        MarginData marginData = new MarginData(
                        new MarginDataId(quoteNumber, type, modelCode, partNumber, strCurrency, userId),
                        plant, CurrencyFormatUtils.formatDoubleValue(listPrice, CurrencyFormatUtils.decimalFormatFourDigits),
                        CurrencyFormatUtils.formatDoubleValue(netPrice, CurrencyFormatUtils.decimalFormatFourDigits),
                        series
                );

        // Assign ManufacturingCost
        // if Part is marked as SPED then
        // ManufacturingCost = ManufacturingCost * ExchangeRate based on Plant * (1 + costUplift) + 90% of DealerNet

        // ManufacturingCost must be multiplied by aopRate (to exchange the currency)
        // ManufacturingCost can be in RMB(CNY), USD or AUD -> then it must be exchanged to be the same as the currency of DealerNet
        double manufacturingCost = getManufacturingCost(modelCode, partNumber, strCurrency, queryPlant, netPrice, aopRate, region);
        if(description.contains("SPED")) {
            manufacturingCost = manufacturingCost * aopRate * (1 + costUplift) + 0.9 * netPrice;
            manufacturingCost = manufacturingCost /aopRate;
        }

        marginData.setManufacturingCost(CurrencyFormatUtils.formatDoubleValue(manufacturingCost, CurrencyFormatUtils.decimalFormatFourDigits));
        marginData.setRegion(region);
        return marginData;
    }

    /**
     * Get Duty value by class, if class is "Class 5 BT" then value = 0.05 or else = 0.0
     */
    private double getDutyValueByClass(String clazz, String strCurrency) {
        if(!strCurrency.equals("AUD"))
            return 0.0;
        if(clazz == null)
            return 0.0;
        else
            return clazz.equals("Class 5 BT") ? 0.05 : 0.0;
    }

    /**
     * Calculate MarginAnalystSummary and save into In-memory database
     */
    public MarginSummary calculateNonUSMarginAnalystSummary(String fileUUID, String plant, String strCurrency, String durationUnit, Integer type, String series,
                                                            String modelCode, String orderNumber, String region, int userId) {
        // Prepare Model Code for calculation if Model Code is null then --> use Series to find List of Mode Codes in a file with FileUUID
        List<String> modelCodeList = Collections.singletonList(modelCode);
        if(modelCode == null) modelCodeList = marginDataRepository.getModelCodesBySeries(fileUUID, series);

        log.info("Calculating {} summary", durationUnit);
        log.info("List of Model Codes in a summary: {}", modelCodeList);

        String quoteNumber = "";
        double totalListPrice = 0, totalManufacturingCost = 0, dealerNet = 0;
        for(String mc : modelCodeList) {
            List<MarginData> marginDataList =
                    marginDataRepository.getIMMarginAnalystData(mc, orderNumber, strCurrency, type, fileUUID, series, region);

            log.info("Data in a Summary: {}", marginDataList.size());
            // If the Model Code does not have any Margin Analysis Data then ignore it.
            if(marginDataList.isEmpty()) continue;
            quoteNumber = marginDataList.get(0).getId().getQuoteNumber();
            for(MarginData data : marginDataList) {
                totalListPrice += data.getListPrice();
                totalManufacturingCost += data.getManufacturingCost();
                dealerNet += data.getDealerNet();
            }
        }
        double costUplift = 0.0, surcharge = 0.015, aopRate = 1;
        boolean liIonIncluded = false;

        // Get the latest Exchange Rate value
        String queryPlant = plant.equals("SN") ? "SN" : "HYM";
        Optional<MarginAnalysisAOPRate> optionalMarginAnalysisAOPRate = getLatestMarginAnalysisAOPRate(strCurrency, queryPlant, durationUnit);
        if(optionalMarginAnalysisAOPRate.isPresent()) {
            aopRate = optionalMarginAnalysisAOPRate.get().getAopRate();
            surcharge = optionalMarginAnalysisAOPRate.get().getSurcharge();
        }

        String clazz = marginAnalystMacroService.getClassBySeries(series);
        double warranty = marginAnalystMacroService.getLatestWarrantyValue(clazz);
        double duty = getDutyValueByClass(clazz, strCurrency);
        double freight = marginAnalystMacroService.getLatestFreightValue(series.substring(1));

        double totalCost = totalManufacturingCost * (1 + costUplift) * (1 + warranty + surcharge + duty);
        double blendedDiscount = 1 - (dealerNet / totalListPrice);

        double fullCostAOPRate = totalCost * aopRate;
        double manufacturingCostUSD = totalManufacturingCost;

        // manufacturingCost in HYM plant is in RMB Currency -> then exchange to USD or AUD
        // manufacturingCost in SN plant is in USD Currency -> no need to exchange
        if(queryPlant.equals("HYM"))
            manufacturingCostUSD = totalManufacturingCost * aopRate;

        double warrantyCost = manufacturingCostUSD * warranty;
        double surchargeCost = manufacturingCostUSD * surcharge;
        double dutyCost = manufacturingCostUSD * duty;
        double totalCostWithoutFreight = manufacturingCostUSD + warrantyCost + surchargeCost + dutyCost;
        double totalCostWithFreight = 0;

        // If the currency is AUD then the Cost in including Freight (AUD only)
        if(strCurrency.equals("AUD")) {
            fullCostAOPRate = (totalCost * aopRate) + freight;
            totalCostWithFreight = totalCostWithoutFreight + freight * aopRate;
        }

        double margin = dealerNet - fullCostAOPRate;
        double marginPercentAopRate = dealerNet == 0 ? 0 : margin / dealerNet;

        type = type == null ? 0 : type;
        modelCode = modelCode == null ? "" : modelCode;

        MarginSummary marginSummary = new MarginSummary
                (
                        new MarginSummaryId(quoteNumber, type, modelCode, series, strCurrency, userId, durationUnit),
                        CurrencyFormatUtils.formatDoubleValue(totalManufacturingCost, CurrencyFormatUtils.decimalFormatFourDigits),
                        costUplift, warranty, surcharge, duty, freight, liIonIncluded,
                        CurrencyFormatUtils.formatDoubleValue(totalCost, CurrencyFormatUtils.decimalFormatFourDigits),
                        CurrencyFormatUtils.formatDoubleValue(totalListPrice, CurrencyFormatUtils.decimalFormatFourDigits),
                        CurrencyFormatUtils.formatDoubleValue(blendedDiscount, CurrencyFormatUtils.decimalFormatFourDigits),
                        CurrencyFormatUtils.formatDoubleValue(dealerNet, CurrencyFormatUtils.decimalFormatFourDigits),
                        CurrencyFormatUtils.formatDoubleValue(margin, CurrencyFormatUtils.decimalFormatFourDigits),
                        aopRate,
                        manufacturingCostUSD,
                        warrantyCost, surchargeCost, dutyCost, totalCostWithoutFreight, totalCostWithFreight, fileUUID, plant
                );
        marginSummary.setFullCostAOPRate(CurrencyFormatUtils.formatDoubleValue(fullCostAOPRate, CurrencyFormatUtils.decimalFormatFourDigits));
        marginSummary.setMarginPercentageAOPRate(CurrencyFormatUtils.formatDoubleValue(marginPercentAopRate, CurrencyFormatUtils.decimalFormatFourDigits));
        return marginSummary;
    }
    private Optional<MarginAnalysisAOPRate> getLatestMarginAnalysisAOPRate(String currency, String plant, String durationUnit) {
        return marginAnalysisAOPRateRepository.getLatestMarginAnalysisAOPRate(plant, currency, durationUnit);
    }

    /**
     * Get the In-memory Data which has already been calculated in the uploaded file
     */
    public List<MarginData> getIMMarginAnalystData(String modelCode, String strCurrency, String fileUUID, String orderNumber, Integer type, String series, String region) {
        return marginDataRepository.getIMMarginAnalystData(modelCode, orderNumber, strCurrency, type, fileUUID, series, region);
    }

    public MarginSummary calculateUSPlantMarginSummary(String modelCode, String series, String strCurrency, String durationUnit,
                                                       String orderNumber, Integer type, String fileUUID, int userId) {
        double defMFGCost = 0;
        LocalDate monthYear = LocalDate.now();
        Optional<Booking> optionalBookingOrder = bookingService.getBookingOrderByOrderNumber(orderNumber);
        if(optionalBookingOrder.isPresent())
        {
            defMFGCost = optionalBookingOrder.get().getTotalCost();
            monthYear = optionalBookingOrder.get().getDate();
        }

        // Initialize values
        double costUplift = 0.0, surcharge = 0.015;
        boolean liIonIncluded = false; // NO

        String clazz = marginAnalystMacroService.getClassBySeries(series);
        double warranty = marginAnalystMacroService.getWarrantyValue(clazz, monthYear);
        double duty = getDutyValueByClass(clazz, strCurrency);
        double freight = marginAnalystMacroService.getFreightValue(series.substring(1), monthYear);

        // ExchangeRate from strCurrency to USD
        monthYear = LocalDate.of(monthYear.getYear(), monthYear.getMonth(), 1);
        double aopRate = exchangeRateService.getExchangeRate(strCurrency, "USD", monthYear) == null
                ? 1
                : exchangeRateService.getExchangeRate(strCurrency, "USD", monthYear).getRate();     // considered as the ExchangeRate


        List<String> modelCodeList = Collections.singletonList(modelCode);
        if(modelCode == null) modelCodeList = marginDataRepository.getModelCodesBySeries(fileUUID, series);

        // calculate total of List Price, Manufacturing Cost and Dealer Net of Model Codes in a Series Code
        double totalListPrice = 0.0, dealerNet = 0.0, totalManufacturingCost = defMFGCost;
        String quoteNumber = "";
        for(String mc : modelCodeList) {
            List<MarginData> marginDataList =
                    marginDataRepository.getIMMarginAnalystData(mc, orderNumber, strCurrency, type, fileUUID, series, null);

            quoteNumber = marginDataList.get(0).getId().getQuoteNumber();

            for(MarginData data : marginDataList) {
                totalListPrice += data.getListPrice();
                dealerNet += data.getDealerNet();
                if(data.isSPED())
                    totalManufacturingCost += (data.getManufacturingCost() - defMFGCost);
            }
        }

        double totalCost = totalManufacturingCost * (1 + costUplift) * (1 + warranty + surcharge + duty);
        double blendedDiscount = 1 - (dealerNet / totalListPrice);

        double fullCostAOPRate = totalCost;
        double manufacturingCostUSD = totalManufacturingCost * aopRate;

        double warrantyCost = manufacturingCostUSD * warranty;
        double surchargeCost = manufacturingCostUSD * surcharge;
        double dutyCost = manufacturingCostUSD * duty;
        double totalCostWithoutFreight = manufacturingCostUSD + warrantyCost + surchargeCost + dutyCost;
        double totalCostWithFreight = 0;

        // Full Cost AOP Rate and Total Cost With Freight will be added with *freight* if the Currency is AUD
        if(strCurrency.equals("AUD")) {
            fullCostAOPRate = totalCost + freight;
            totalCostWithFreight = totalCostWithoutFreight + freight * aopRate;
        }

        double margin = dealerNet - fullCostAOPRate;
        double marginPercentAopRate = (dealerNet == 0) ? 0 : (margin / dealerNet);

        MarginSummary marginSummary = new MarginSummary
                (
                        new MarginSummaryId(quoteNumber, type, modelCode, series, strCurrency, userId, durationUnit),
                        CurrencyFormatUtils.formatDoubleValue(totalManufacturingCost, CurrencyFormatUtils.decimalFormatFourDigits),
                        costUplift, warranty, surcharge, duty, freight, liIonIncluded,
                        CurrencyFormatUtils.formatDoubleValue(totalCost, CurrencyFormatUtils.decimalFormatFourDigits),
                        CurrencyFormatUtils.formatDoubleValue(totalListPrice, CurrencyFormatUtils.decimalFormatFourDigits),
                        CurrencyFormatUtils.formatDoubleValue(blendedDiscount, CurrencyFormatUtils.decimalFormatFourDigits),
                        CurrencyFormatUtils.formatDoubleValue(dealerNet, CurrencyFormatUtils.decimalFormatFourDigits),
                        CurrencyFormatUtils.formatDoubleValue(margin, CurrencyFormatUtils.decimalFormatFourDigits),
                        aopRate,
                        manufacturingCostUSD,
                        warrantyCost, surchargeCost, dutyCost, totalCostWithoutFreight, totalCostWithFreight, fileUUID, null
                );

        marginSummary.setOrderNumber(orderNumber);
        marginSummary.setFileUUID(fileUUID);

        marginSummary.setFullCostAOPRate(CurrencyFormatUtils.formatDoubleValue(fullCostAOPRate, CurrencyFormatUtils.decimalFormatFourDigits));
        marginSummary.setMarginPercentageAOPRate(CurrencyFormatUtils.formatDoubleValue(marginPercentAopRate, CurrencyFormatUtils.decimalFormatFourDigits));
        return marginSummary;
    }

    public void calculateMarginAnalysisData(String fileUUID, String currency, String region, int userId) throws IOException, IncorectFormatCellException {
        String fileName = fileUploadService.getFileNameByUUID(fileUUID); // fileName has been hashed
        String baseFolder = EnvironmentUtils.getEnvironmentValue("public-folder");
        String baseFolderUploaded = EnvironmentUtils.getEnvironmentValue("upload_files.base-folder");
        String targetFolder = EnvironmentUtils.getEnvironmentValue("upload_files.novo");
        String filePath = baseFolder + baseFolderUploaded + targetFolder + fileName;
        FileInputStream is = new FileInputStream(filePath);
        XSSFWorkbook workbook = new XSSFWorkbook(is);
        Sheet sheet = workbook.getSheetAt(0);
        List<MarginData> marginDataList = new ArrayList<>();

        String orderNumber = "";
        for(Row row : sheet) {
            if(row.getRowNum() == 0)
                getColumnName(row);
            else if (!row.getCell(COLUMN_NAME.get("Model Code"), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().isEmpty()) {
                // Verify cells' format
                verifyNOVOCellFormat(row, fileUUID);

                // Check if the Part Number is "Commission" or blank then ignore it.
                String partNumber = row.getCell(COLUMN_NAME.get("Part Number"), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
                if(partNumber.equals("Commission") || partNumber.isEmpty())
                    continue;

                // Check if the file contains "Order ID" column
                // if not then not read Order ID value
                if(COLUMN_NAME.containsKey("Order ID")) {
                    // Get the value of Order Number from file.
                    String orderIDCellValue = row.getCell(COLUMN_NAME.get("Order ID"), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
                    if(!orderIDCellValue.isEmpty()) orderNumber = orderIDCellValue;
                }

                // Check plant of Model Code
                // If the Model Code cannot be found (which means plant is null) then ignore it
                String series = row.getCell(COLUMN_NAME.get("Series Code")).getStringCellValue();
                String plant = marginAnalystMacroService.getPlantBySeries(series);
                if(plant == null) {
                    plant = productRepository.getPlantBySeries(series);
                    if(plant == null) continue;
                }

                MarginData marginData;
                // calculate non US plant Margin Analysis Data
                if(plant.equals("Maximal") || plant.equals("Staxx") || plant.equals("Ruyi") || plant.equals("SN")) {
                    marginData = mapIMMarginAnalystData(row, plant, currency, region, userId);
                }
                else {
                    // calculate US plant Margin Analysis Data
                    Optional<Booking> optionalBookingOrder = bookingService.getBookingOrderByOrderNumber(orderNumber);
                    if(optionalBookingOrder.isEmpty()) continue;
                    double manufacturingCost = optionalBookingOrder.get().getTotalCost();
                    marginData = mapUSPlantMarginAnalysisData(row, manufacturingCost, currency, orderNumber, plant, userId);
                }
                marginData.setOrderNumber(orderNumber);
                marginData.setFileUUID(fileUUID);
                marginDataList.add(marginData);
            }
        }
        log.info("Save Margin Analysis Data: {}", marginDataList.size());
        marginDataRepository.saveAll(marginDataList);
    }

    public Map<String, Object> calculateMarginAnalysisSummary(String fileUUID, Integer type, String modelCode, String series, String orderNumber,
                                                              String currency, String region, int userId) throws SeriesNotFoundException {
        String plant = marginAnalystMacroService.getPlantBySeries(series);
        if(plant == null) {
            plant = productRepository.getPlantBySeries(series);
            if(plant == null)
                throw new SeriesNotFoundException("Series not found: " + series, series);
        }

        MarginSummary monthly;
        MarginSummary annually;
        if(plant.equals("Maximal") || plant.equals("Staxx") || plant.equals("Ruyi") || plant.equals("SN")) {
            monthly = calculateNonUSMarginAnalystSummary(fileUUID, plant, currency, "monthly", type, series, modelCode, orderNumber, region, userId);
            annually = calculateNonUSMarginAnalystSummary(fileUUID, plant, currency, "annually", type, series, modelCode, orderNumber, region, userId);
        }
        else {
            monthly = calculateUSPlantMarginSummary(modelCode, series, currency, "monthly", orderNumber, type, fileUUID, userId);
            annually = calculateUSPlantMarginSummary(modelCode, series, currency, "annually", orderNumber, type, fileUUID, userId);
        }

        marginSummaryRepository.save(monthly);
        marginSummaryRepository.save(annually);
        return Map.of(
                "MarginAnalystSummaryMonthly", monthly,
                "MarginAnalystSummaryAnnually", annually
        );
    }

    public MarginData mapUSPlantMarginAnalysisData(Row row, double manufacturingCost, String strCurrency, String orderNumber, String plant, int userId) {
        String modelCode = row.getCell(COLUMN_NAME.get("Model Code")).getStringCellValue();
        String partNumber = row.getCell(COLUMN_NAME.get("Part Number")).getStringCellValue();
        double listPrice = row.getCell(COLUMN_NAME.get("List Price")).getNumericCellValue();
        double netPrice = row.getCell(COLUMN_NAME.get("Net Price Each")).getNumericCellValue();
        String partDescription = row.getCell(COLUMN_NAME.get("Part Description"), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
        int type = (int) row.getCell(COLUMN_NAME.get("#")).getNumericCellValue();
        String series = row.getCell(COLUMN_NAME.get("Series Code"), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
        String quoteNumber = row.getCell(COLUMN_NAME.get("Quote Number:")).getStringCellValue();

        double manufacturingCostWithSPED = manufacturingCost;
        boolean isSPED = false;
        if(partDescription.contains("SPED"))
        {
            isSPED = true;
            manufacturingCostWithSPED += 0.9 * netPrice;
        }

        // Assigning value for imMarginAnalystData
        MarginData marginData = new MarginData(
                new MarginDataId(quoteNumber, type, modelCode, partNumber, strCurrency, userId),
                plant, CurrencyFormatUtils.formatDoubleValue(listPrice, CurrencyFormatUtils.decimalFormatFourDigits),
                CurrencyFormatUtils.formatDoubleValue(netPrice, CurrencyFormatUtils.decimalFormatFourDigits),
                series
        );
        marginData.setOrderNumber(orderNumber);
        marginData.setManufacturingCost(CurrencyFormatUtils.formatDoubleValue(manufacturingCostWithSPED, CurrencyFormatUtils.decimalFormatFourDigits));
        marginData.setSPED(isSPED);
        return marginData;
    }

    /**
     * Read NOVO file and create populating values for showing on Dropdown box in Margin Screen
     */
    public Map<String, Object> populateMarginFilters(String filePath, String fileUUID) throws IOException, MissingColumnException, IncorectFormatCellException {

        FileInputStream is = new FileInputStream(filePath);
        XSSFWorkbook workbook = new XSSFWorkbook(is);

        HashMap<String, Integer> modelCodeMap = new HashMap<>();
        HashMap<String, Integer> seriesCodeMap = new HashMap<>();
        HashMap<String, Integer> orderNumberMap = new HashMap<>();
        HashMap<Integer, Integer> typeMap = new HashMap<>();

        Sheet sheet = workbook.getSheetAt(0);
        for(Row row : sheet) {
            if(row.getRowNum() == 0) {
                getColumnName(row);
                CheckRequiredColumnUtils.checkRequiredColumn(new ArrayList<>(COLUMN_NAME.keySet()), CheckRequiredColumnUtils.NOVO_REQUIRED_COLUMN, fileUUID);
            }
            else if(!row.getCell(COLUMN_NAME.get("Model Code"), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().isEmpty()) {
                // Check cells' format before reading value.
                Cell modelCodeCell = row.getCell(COLUMN_NAME.get("Model Code"));
                if(modelCodeCell.getCellType() != CellType.STRING)
                    throw new IncorectFormatCellException((row.getRowNum() + 1) + ":" + (COLUMN_NAME.get("Model Code") + 1), fileUUID);

                Cell seriesCodeCell = row.getCell(COLUMN_NAME.get("Series Code"));
                if(seriesCodeCell.getCellType() != CellType.STRING)
                    throw new IncorectFormatCellException((row.getRowNum() + 1) + ":" + (COLUMN_NAME.get("Series Code") + 1), fileUUID);

                Cell typeCell = row.getCell(COLUMN_NAME.get("#"));
                if(typeCell.getCellType() != CellType.NUMERIC)
                    throw new IncorectFormatCellException((row.getRowNum() + 1) + ":" + (COLUMN_NAME.get("#") + 1), fileUUID);

                // Reading cells' value
                String orderIDCellValue = row.getCell(COLUMN_NAME.get("Order ID"), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
                if(!orderIDCellValue.isEmpty())
                    orderNumberMap.put(orderIDCellValue, 1);

                modelCodeMap.put(modelCodeCell.getStringCellValue(), 1);
                seriesCodeMap.put(seriesCodeCell.getStringCellValue(), 1);
                typeMap.put((int) typeCell.getNumericCellValue(), 1);
            }
        }

        List<Object> modelCodeValues = new ArrayList<>();
        for(String item : modelCodeMap.keySet()) {
            modelCodeValues.add(Map.of("value", item));
        }

        List<Object> seriesCodeValues = new ArrayList<>();
        for(String item : seriesCodeMap.keySet()) {
            seriesCodeValues.add(Map.of("value", item));
        }

        List<Object> orderNumberValues = new ArrayList<>();
        for(String item : orderNumberMap.keySet()) {
            orderNumberValues.add(Map.of("value", item));
        }

        List<Object> typeValues = new ArrayList<>();
        for(Integer item : typeMap.keySet()) {
            typeValues.add(Map.of("value", item));
        }

        return Map.of(
                "modelCodes", modelCodeValues,
                "series", seriesCodeValues,
                "orderNumbers", orderNumberValues,
                "types", typeValues
        );
    }

    /**
     * Check a file which has fileUUID has already been calculated Margin Data or not
     */
    public boolean isFileCalculated(String fileUUID, String currency, String region) {
        return marginDataRepository.isFileCalculated(fileUUID, currency, region);
    }

    /**
     * View the history calculated Quotation Margin % Data
     */
    public List<MarginData> viewHistoryMargin(MarginDataId id) {
        return marginDataRepository.viewHistoryMarginData(id.getQuoteNumber(), id.getType(), id.getModelCode(), id.getCurrency(), id.getUserId());
    }
}
