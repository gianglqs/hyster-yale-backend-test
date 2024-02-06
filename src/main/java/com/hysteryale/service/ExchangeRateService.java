package com.hysteryale.service;

import com.hysteryale.model.Currency;
import com.hysteryale.model.ExchangeRate;
import com.hysteryale.model.reports.CompareCurrencyRequest;
import com.hysteryale.model.reports.CompareCurrencyResponse;
import com.hysteryale.repository.ExchangeRateRepository;
import com.hysteryale.utils.CurrencyFormatUtils;
import com.hysteryale.utils.DateUtils;
import com.hysteryale.utils.EnvironmentUtils;
import com.hysteryale.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class ExchangeRateService extends BasedService {
    @Resource
    ExchangeRateRepository exchangeRateRepository;
    @Resource
    CurrencyService currencyService;
    @Resource
    FileUploadService fileUploadService;
    public static Map<Integer, String> fromCurrenciesTitle = new HashMap<>();

    public Map<Integer, String> getFromCurrencyTitle(Row row) {
        int end = 31;
        for(int i = 1; i <= end; i++) {
            String currency = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
            fromCurrenciesTitle.put(i, currency.toUpperCase());
        }
        log.info("Currencies: " + fromCurrenciesTitle);
        return fromCurrenciesTitle;
    }

    public String formatCurrencyInSpecialCase(String strCurrency) {

        //special cases of currency name
        switch (strCurrency.strip()) {
            case "NORWAY KRONER" :
                strCurrency = "NORWEGIAN KRONER";
                break;
            case "BRAZILIAN REAL":
                strCurrency = "BRAZILIAN";
                break;
            case "SING. DOLLAR":
                strCurrency = "SINGAPORE DOLLAR";
                break;
            case "N.Z. DOLLAR":
                strCurrency = "N.Z.DOLLAR";
                break;
        }
        return strCurrency;
    }

    /**
     * Get List of Currencies rates based on a toCurrency
     */
    public List<ExchangeRate> mapExcelDataToExchangeRate(Row row, LocalDate date) {
        List<ExchangeRate> exchangeRateList = new ArrayList<>();

        String strToCurrency = row.getCell(0).getStringCellValue().toUpperCase().strip();
        Currency toCurrency = currencyService.getCurrenciesByName(formatCurrencyInSpecialCase(strToCurrency));

        for(int i = 1; i <= 31; i++) {
            Cell cell = row.getCell(i);

            ExchangeRate exchangeRate = new ExchangeRate();
            double rate = cell.getNumericCellValue();
            String strFromCurrency = fromCurrenciesTitle.get(cell.getColumnIndex()).toUpperCase().strip();

            Currency fromCurrency = currencyService.getCurrenciesByName(formatCurrencyInSpecialCase(strFromCurrency));

            exchangeRate.setFrom(fromCurrency);
            exchangeRate.setTo(toCurrency);
            exchangeRate.setRate(rate);
            exchangeRate.setDate(date);

//            log.info("from: " + fromCurrency.getCurrency() + " to: " + toCurrency.getCurrency());

            Optional<ExchangeRate> dbExchangeRate = exchangeRateRepository.getExchangeRateByFromToCurrencyAndDate(fromCurrency.getCurrency(), toCurrency.getCurrency(), date);
            dbExchangeRate.ifPresent(value -> exchangeRate.setId(value.getId()));
            exchangeRateList.add(exchangeRate);
        }
        return exchangeRateList;
    }

    public void importExchangeRate() throws IOException {
        // Initialize folder path and file name
        String baseFolder = EnvironmentUtils.getEnvironmentValue("import-files.base-folder");
        String folderPath = baseFolder + EnvironmentUtils.getEnvironmentValue("import-files.currency");
        String fileName = "EXCSEP2023.xlsx";

        //Pattern for getting date from fileName
        Pattern pattern = Pattern.compile("^\\w{3}(\\w{3})(\\d{4}).");
        Matcher matcher = pattern.matcher(fileName);

        // Assign date get from fileName
        LocalDate date = LocalDate.now();
        if(matcher.find())
        {
            String month = matcher.group(1);
            int year = Integer.parseInt(matcher.group(2));

            date = LocalDate.of(year, DateUtils.getMonth(month), 1);
        }

        InputStream is = new FileInputStream(folderPath + "/" + fileName);
        XSSFWorkbook workbook = new XSSFWorkbook(is);

        Sheet sheet = workbook.getSheet("Summary AOP");
        List<ExchangeRate> exchangeRatesList = new ArrayList<>();

        for(int i = 3; i <=34; i++) {
            Row row = sheet.getRow(i);
            if(i == 3)
                fromCurrenciesTitle = getFromCurrencyTitle(row);
            else
                exchangeRatesList.addAll(mapExcelDataToExchangeRate(row, date));
        }

        exchangeRateRepository.saveAll(exchangeRatesList);
        log.info("ExchangeRate are newly saved or updated: " + exchangeRatesList.size());
        exchangeRatesList.clear();
    }

    public ExchangeRate getExchangeRate(String fromCurrency, String toCurrency, LocalDate monthYear) {
        Optional<ExchangeRate> optionalExchangeRate = exchangeRateRepository.getExchangeRateByFromToCurrencyAndDate(fromCurrency, toCurrency, monthYear);
        return optionalExchangeRate.orElse(null);
    }

    public void saveExchangeRate(ExchangeRate exchangeRate) {
        exchangeRateRepository.save(exchangeRate);
    }

    public ExchangeRate getNearestExchangeRate(String fromCurrency, String toCurrency) {
        Optional<ExchangeRate> optionalExchangeRate = exchangeRateRepository.getNearestExchangeRateByFromToCurrency(fromCurrency, toCurrency);
        if(optionalExchangeRate.isPresent())
            return optionalExchangeRate.get();
        else
            return null;
    }

    /**
     * Compare Currencies for reporting in Reports page
     */
    public Map<String, Object> compareCurrency(CompareCurrencyRequest request) {
        String currentCurrency = request.getCurrentCurrency();
        List<String> comparisonCurrencies = request.getComparisonCurrencies();

        Map<String, Object> data = new HashMap<>();
        List<String> stableCurrencies = new ArrayList<>();
        List<String> weakerCurrencies = new ArrayList<>();
        List<String> strongerCurrencies = new ArrayList<>();

        for(String currency : comparisonCurrencies) {
            List<ExchangeRate> exchangeRateList = exchangeRateRepository.getCurrentExchangeRate(currentCurrency, currency);

            if(exchangeRateList.isEmpty())
                continue;
            double nearestRate = exchangeRateList.get(0).getRate();
            double farthestRate = exchangeRateList.get(exchangeRateList.size() - 1).getRate();
            double differentRate = nearestRate - farthestRate;
            double differentRatePercentage = CurrencyFormatUtils.formatDoubleValue((differentRate / farthestRate) * 100, CurrencyFormatUtils.decimalFormatFourDigits);

            if(Math.abs(differentRatePercentage) > 5) {
                StringBuilder sb = new StringBuilder();
                Formatter formatter = new Formatter(sb);
                formatter.format("%,.7f", differentRate);

                if(differentRatePercentage < 0) weakerCurrencies.add(currency + " by " + sb + " (" + differentRatePercentage + "%)");
                else strongerCurrencies.add(currency + " by +" + sb + " (+" + differentRatePercentage + "%)");
            }
            else stableCurrencies.add(currency);
            data.put(currency, new CompareCurrencyResponse(exchangeRateList, differentRate, differentRatePercentage));
        }

        data.put("stable", stableCurrencies);
        data.put("weakening", weakerCurrencies);
        data.put("strengthening", strongerCurrencies);
        return data;
    }

    public void importExchangeRateFromFile(MultipartFile file, Authentication authentication) throws Exception {
        String baseFolder = EnvironmentUtils.getEnvironmentValue("upload_files.base-folder");

        // Verify file's type
        if(!FileUtils.isExcelFile(file.getInputStream()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is not an Excel file");

        // Verify whether file's name is null or not
        String originalFileName = file.getOriginalFilename();
        if(originalFileName == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File's name is not in appropriate format");

        //Pattern for getting date from fileName
        Pattern pattern = Pattern.compile("^\\w{3}(\\w{3})(\\d{4}).");
        Matcher matcher = pattern.matcher(originalFileName);

        LocalDate date;
        if(matcher.find()) {
            String month = matcher.group(1);
            month = month.charAt(0) + month.substring(1).toLowerCase();
            int year = Integer.parseInt(matcher.group(2));
            date = LocalDate.of(year, DateUtils.getMonth(month), 1);
        }
        else
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File's name is not in appropriate format");

        String filePath = fileUploadService.saveFileUploaded(file, authentication, baseFolder, ".xlsx");

        InputStream is = new FileInputStream(filePath);
        XSSFWorkbook workbook = new XSSFWorkbook(is);

        Sheet sheet = workbook.getSheet("Summary Current Interlocking");
        List<ExchangeRate> exchangeRatesList = new ArrayList<>();

        for(int i = 3; i <=34; i++) {
            Row row = sheet.getRow(i);
            if(i == 3)
                fromCurrenciesTitle = getFromCurrencyTitle(row);
            else
                exchangeRatesList.addAll(mapExcelDataToExchangeRate(row, date));
        }

        exchangeRateRepository.saveAll(exchangeRatesList);
        log.info("ExchangeRate are newly saved or updated: " + exchangeRatesList.size());
        exchangeRatesList.clear();
    }
}
