package com.hysteryale.service.impl;

import com.hysteryale.model.Part;
import com.hysteryale.model.marginAnalyst.MarginAnalystData;
import com.hysteryale.model.marginAnalyst.MarginAnalystMacro;
import com.hysteryale.model.marginAnalyst.MarginAnalystSummary;
import com.hysteryale.repository.marginAnalyst.MarginAnalystDataRepository;
import com.hysteryale.repository.marginAnalyst.MarginAnalystSummaryRepository;
import com.hysteryale.service.CurrencyService;
import com.hysteryale.service.PartService;
import com.hysteryale.service.marginAnalyst.MarginAnalystMacroService;
import com.hysteryale.service.marginAnalyst.MarginAnalystService;
import com.hysteryale.utils.CurrencyFormatUtils;
import com.hysteryale.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class MarginAnalystServiceImpl implements MarginAnalystService {
    @Resource
    MarginAnalystDataRepository marginAnalystDataRepository;
    @Resource
    CurrencyService currencyService;
    @Resource
    PartService partService;
    @Resource
    MarginAnalystSummaryRepository marginAnalystSummaryRepository;
    @Resource
    MarginAnalystMacroService marginAnalystMacroService;

    /**
     * Get List of MarginAnalystData having modelCode, currency in a month {monthYear}
     */
    @Override
    public Map<String, List<MarginAnalystData>> getMarginAnalystData(String modelCode, String currency, LocalDate monthYear) {
        List<MarginAnalystData> marginAnalystDataList = marginAnalystDataRepository.getMarginDataForAnalysis(modelCode, currency, monthYear);
        return Map.of("MarginAnalystData", marginAnalystDataList);
    }

    /**
     * @param modelCode
     * @param currency
     * @return
     */
    @Override
    public Map<String, MarginAnalystSummary> getMarginAnalystSummary(String modelCode, String currency, LocalDate monthYear) {
        // Based on the model Code, we can query all the parts related to it
        // At first, Yurini said that we can find all the parts in the "Novo Quotation Download", after that she said if we do this way
        // then they will have to download for each model code
        // So she wants to use the Power BI file, we can find the Model (AI) then we can get part Number (AT) then we can get the List price (AZ)

        //for the cost RMB, use Margin Analysis, model code + part -> Cost RMB

        //then find the dealer net

        //Calculate margin
        // if there is no Cost RMB
        //      margin @ aop USD = Dealer net (net price) * 0.1,
        //      else
        //         margin @ aop USD = (Dealer Net - Cost RMB *(1+ CostUpLift) * (1+ Warranty + Surcharge + Duty)* Margin Analysis @ AOP Rate)- Freight)

        Map<String, MarginAnalystSummary> marginAnalystSummaryMap = new HashMap<>();

        LocalDate calendarForAnnually = LocalDate.of(monthYear.getYear(), Month.JANUARY, 28);

        Optional<MarginAnalystSummary> optionalMarginAnalystSummaryMonthly = marginAnalystSummaryRepository.getMarginAnalystSummaryMonthly(modelCode, currency, monthYear);
        Optional<MarginAnalystSummary> optionalMarginAnalystSummaryAnnually = marginAnalystSummaryRepository.getMarginAnalystSummaryAnnually(modelCode, currency, calendarForAnnually);

        optionalMarginAnalystSummaryMonthly.ifPresent(marginAnalystSummary -> marginAnalystSummaryMap.put("MarginAnalystSummaryMonthly", marginAnalystSummary));
        optionalMarginAnalystSummaryAnnually.ifPresent(marginAnalystSummary -> marginAnalystSummaryMap.put("MarginAnalystSummaryAnnually", marginAnalystSummary));

        return marginAnalystSummaryMap;
    }

    /**
     * Assign value for MarginAnalysisData from Excel row
     */
    private MarginAnalystData mapMarginAnalysisData(Part part) {

        Optional<MarginAnalystMacro> optionalMarginAnalystMacro = marginAnalystMacroService.getMarginAnalystMacroByMonthYear(part.getModelCode(), part.getPartNumber(), part.getCurrency().getCurrency(), part.getRecordedTime());
        if(optionalMarginAnalystMacro.isPresent()) {
            MarginAnalystMacro marginAnalystMacro = optionalMarginAnalystMacro.get();

            MarginAnalystData marginAnalystData = new MarginAnalystData();

            // Non-calculated data fields
            marginAnalystData.setPlant(marginAnalystMacro.getPlant());
            marginAnalystData.setModelCode(marginAnalystMacro.getModelCode());
            marginAnalystData.setClass_(marginAnalystMacro.getClazz());
            marginAnalystData.setOptionCode(marginAnalystMacro.getPartNumber());
            marginAnalystData.setDescription(marginAnalystMacro.getDescription());
            marginAnalystData.setCurrency(marginAnalystMacro.getCurrency());
            marginAnalystData.setMonthYear(marginAnalystMacro.getMonthYear());

            marginAnalystData.setDealer(part.getBillTo());


            //Calculate margin
            // if there is no Cost RMB
            //      margin @ aop USD = Dealer net (net price) * 0.1,
            //      else
            //         margin @ aop USD = (Dealer Net - Cost RMB *(1+ CostUpLift) * (1+ Warranty + Surcharge + Duty)* Margin Analysis @ AOP Rate)- Freight)

            double listPrice = part.getListPrice();
            double netPrice = part.getNetPriceEach();
            double costRMB = marginAnalystMacro.getCostRMB();

            marginAnalystData.setDealerNet(CurrencyFormatUtils.formatDoubleValue(netPrice, CurrencyFormatUtils.decimalFormatFourDigits));

            // Notes: some of the parameters are assigned manually

            // AUD = 0.2159, USD = 0.1569
            double marginAnalysisAOPRate = part.getCurrency().getCurrency().equals("AUD") ? 0.2159 : 0.1569;
            double costUplift = 0.0;
            double surcharge = 0.015;
            double duty = 0.0;
            double marginAOP;
            double freight = 0;
            double warranty = 0;

            if(costRMB == 0.0) {
                marginAOP =  netPrice * 0.1;
            }
            else {
                marginAOP = (netPrice - costRMB * (1 + costUplift) * (1 + warranty + surcharge + duty) * marginAnalysisAOPRate) - freight;
            }

            // Calculated data fields
            marginAnalystData.setCostRMB(CurrencyFormatUtils.formatDoubleValue(costRMB, CurrencyFormatUtils.decimalFormatFourDigits));
            marginAnalystData.setListPrice(CurrencyFormatUtils.formatDoubleValue(listPrice, CurrencyFormatUtils.decimalFormatFourDigits));
            marginAnalystData.setMargin_aop(CurrencyFormatUtils.formatDoubleValue(marginAOP, CurrencyFormatUtils.decimalFormatFourDigits));
            return marginAnalystData;
        }
        return null;
    }

    public void importMarginAnalystData() {
        // Init folderPath and fileName
        String fileName = "Copy of USD AUD Margin Analysis Template Macro_Aug 1st.xlsx";       // "Margin Analysis Macro"

        // Extract monthYear from fileName pattern
        Pattern pattern = Pattern.compile(".* Macro_(\\w{3}) .*");
        Matcher matcher = pattern.matcher(fileName);
        String month = "Jan";
        int year = 2023;
        if(matcher.find()) {
           month = matcher.group(1);
        }
        LocalDate monthYear = LocalDate.of(year, DateUtils.getMonth(month), 1);

        String[] currencies = {"USD", "AUD"};

        List<String> modelCodeList = partService.getDistinctModelCodeByMonthYear(monthYear);

        for(String strCurrency : currencies) {
            for(String modelCode : modelCodeList) {
                log.info(" === Model Code " + modelCode + " === ");

                // parts are found by modelCode, monthYear and currency
                List<Part> partList = partService.getDistinctPart(modelCode, strCurrency);

                List<MarginAnalystData> marginAnalystDataList = new ArrayList<>();
                for(Part part : partList) {
                    MarginAnalystData marginAnalystData = mapMarginAnalysisData(part);
                    if(marginAnalystData != null) {
                        marginAnalystDataList.add(marginAnalystData);
                    }
                }

                log.info("Newly saved MarginAnalystData: " + marginAnalystDataList.size());
                marginAnalystDataRepository.saveAll(marginAnalystDataList);
                marginAnalystDataList.clear();
            }
        }
        // after having MarginAnalystData -> calculate MarginAnalystSummary
        calculateMarginAnalystSummary(monthYear, "USD", "monthly");
        calculateMarginAnalystSummary(monthYear, "AUD", "monthly");

        calculateMarginAnalystSummary(monthYear, "USD", "annually");
        calculateMarginAnalystSummary(monthYear, "AUD", "annually");
    }

    /**
     * Calculate MarginAnalysisSummary by {monthYear} and {currency}
     * @param monthYear
     * @param currency
     */
    public void calculateMarginAnalystSummary(LocalDate monthYear, String currency, String durationUnit) {
        // Get all distinct modelCode based on {monthYear} and {currency} from MarginAnalystData
        List<String> modelCodeList = marginAnalystDataRepository.getModelCodesByMonthYearAndCurrency(monthYear, currency);
        List<MarginAnalystSummary> marginAnalystSummaryList = new ArrayList<>();


        /* for each modelCode -> get List<MarginAnalystData> and List<Part>
        then calculate totalListPrice, dealerNet, manufacturingCostRMB, .....
        -> save MarginAnalystSummary monthly
        */
        log.info("Number of modelCode: " + modelCodeList.size());
        for(String modelCode : modelCodeList)
        {
            log.info(" === ModelCode " + modelCode + " === ");
            List<MarginAnalystData> marginAnalystDataList = getMarginAnalystData(modelCode, currency, monthYear).get("MarginAnalystData");

            // Initialize values
            double costUplift = 0.0;
            double warranty = 0.0;
            double surcharge = 0.015;
            double duty = 0.0;
            double freight = 0.0;
            boolean liIonIncluded = false; // NO

            double marginAnalysisAOPRate = getMarginAnalysisAOPRate(currency, durationUnit);

            double totalListPrice = 0.0;
            double dealerNet = 0.0;
            double manufacturingCostRMB = 0.0;
            log.info("  Margin data count: " + marginAnalystDataList.size());
            for(MarginAnalystData m : marginAnalystDataList) {
                log.info(m.getOptionCode() + " LP: " + m.getListPrice() + " CostRMB: " + m.getCostRMB());
                totalListPrice += m.getListPrice();
                manufacturingCostRMB += m.getCostRMB();

                dealerNet += m.getDealerNet();
            }
            double totalCostRMB = manufacturingCostRMB * (1 + costUplift) * (1 + warranty + surcharge + duty);
            double blendedDiscount = 1 - (dealerNet / totalListPrice);
            double fullCostAOPRate = totalCostRMB * marginAnalysisAOPRate;
            double margin = dealerNet - fullCostAOPRate;
            double marginPercentAopRate = margin / dealerNet;

            log.info("manufacturing cost RMB: " + manufacturingCostRMB);
            log.info("totalCost RMB: " + totalCostRMB);
            log.info("fullCostAOPRate: " + fullCostAOPRate);
            log.info("totalListPrice: " + totalListPrice);
            log.info("blendedDiscount: " + blendedDiscount);
            log.info("totalDealerNet: " + dealerNet);

            // Assign value of MarginAnalysisSummary
            MarginAnalystSummary marginAnalystSummary = new MarginAnalystSummary();

            // fields for both annually and monthly MarginAnalystSummary
            marginAnalystSummary.setModelCode(modelCode);
            marginAnalystSummary.setCurrency(currencyService.getCurrenciesByName(currency));
            marginAnalystSummary.setManufacturingCostRMB(CurrencyFormatUtils.formatDoubleValue(manufacturingCostRMB, CurrencyFormatUtils.decimalFormatFourDigits));
            marginAnalystSummary.setCostUplift(costUplift);
            marginAnalystSummary.setAddWarranty(warranty);
            marginAnalystSummary.setSurcharge(surcharge);
            marginAnalystSummary.setDuty(duty);
            marginAnalystSummary.setFreight(freight);
            marginAnalystSummary.setLiIonIncluded(liIonIncluded);
            marginAnalystSummary.setTotalCostRMB(CurrencyFormatUtils.formatDoubleValue(totalCostRMB, CurrencyFormatUtils.decimalFormatFourDigits));
            marginAnalystSummary.setTotalListPrice(CurrencyFormatUtils.formatDoubleValue(totalListPrice, CurrencyFormatUtils.decimalFormatFourDigits));
            marginAnalystSummary.setBlendedDiscountPercentage(CurrencyFormatUtils.formatDoubleValue(blendedDiscount, CurrencyFormatUtils.decimalFormatFourDigits));
            marginAnalystSummary.setDealerNet(CurrencyFormatUtils.formatDoubleValue(dealerNet, CurrencyFormatUtils.decimalFormatFourDigits));
            marginAnalystSummary.setMargin(CurrencyFormatUtils.formatDoubleValue(margin, CurrencyFormatUtils.decimalFormatFourDigits));
            marginAnalystSummary.setMarginAopRate(marginAnalysisAOPRate);

            if(durationUnit.equals("monthly")) {
                marginAnalystSummary.setMonthYear(monthYear);
                // monthly valued
                marginAnalystSummary.setFullMonthlyRate(CurrencyFormatUtils.formatDoubleValue(fullCostAOPRate, CurrencyFormatUtils.decimalFormatFourDigits));
                marginAnalystSummary.setMarginPercentMonthlyRate(CurrencyFormatUtils.formatDoubleValue(marginPercentAopRate, CurrencyFormatUtils.decimalFormatFourDigits));
            }
            else {
                // annually valued

                // if MarginAnalystSummary is annual then set {Date into 28, Month into JANUARY} (monthly date would be 1)
                LocalDate annualDate = LocalDate.of(monthYear.getYear(), Month.JANUARY, 28);

                marginAnalystSummary.setMonthYear(annualDate);
                marginAnalystSummary.setFullCostAopRate(CurrencyFormatUtils.formatDoubleValue(fullCostAOPRate, CurrencyFormatUtils.decimalFormatFourDigits));
                marginAnalystSummary.setMarginPercentAopRate(CurrencyFormatUtils.formatDoubleValue(marginPercentAopRate, CurrencyFormatUtils.decimalFormatFourDigits));
            }
            marginAnalystSummaryList.add(marginAnalystSummary);

            log.info(" === End === ");
        }

        marginAnalystSummaryRepository.saveAll(marginAnalystSummaryList);
        log.info("(Monthly) MarginAnalystSummary are newly saved: " + marginAnalystSummaryList.size());
        marginAnalystSummaryList.clear();
    }

    /**
     * Get MarginAnalysisAOP rate variable by currency and durationUnit (annually or monthly)
     * @param currency USD or AUD
     * @param durationUnit annually or monthly
     */
    private double getMarginAnalysisAOPRate(String currency, String durationUnit) {
        if(currency.equals("USD")) {
            if(durationUnit.equals("annually"))
                return 0.1569;
            else
                return 0.1484;
        }
        else {
            if(durationUnit.equals("annually"))
                return 0.2159;
            else
                return 0.2132;
        }
    }
    @Override
    public Map<String, List<Map<String, String>>>  getDealersFromMarginAnalystData() {
        List<String> dealerList = marginAnalystDataRepository.getDealersFromMarginAnalystData();
        List<Map<String, String>> dealerNameList = new ArrayList<>();
        for(String dealerName : dealerList) {
            Map<String, String> dMap = new HashMap<>();
            dMap.put("value", dealerName);

            dealerNameList.add(dMap);
        }
        return Map.of("dealers", dealerNameList);
    }
    @Override
    public Map<String, List<MarginAnalystData>> getMarginDataForAnalysisByDealer(String modelCode, String currency, LocalDate monthYear, String dealer) {
        return Map.of("MarginAnalystData", marginAnalystDataRepository.getMarginDataForAnalysisByDealer(modelCode, currency, monthYear, dealer));
    }
}
