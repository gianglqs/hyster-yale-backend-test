package com.hysteryale.service;

import com.hysteryale.exception.*;
import com.hysteryale.model.Currency;
import com.hysteryale.model.ExchangeRate;
import com.hysteryale.model.reports.CompareCurrencyRequest;
import com.hysteryale.repository.ExchangeRateRepository;
import com.hysteryale.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

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
@SuppressWarnings("unchecked")
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
        for (int i = 1; i <= end; i++) {
            String currency = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
            fromCurrenciesTitle.put(i, currency.toUpperCase());
        }
        log.info("Currencies: " + fromCurrenciesTitle);
        return fromCurrenciesTitle;
    }

    public String formatCurrencyInSpecialCase(String strCurrency) {

        //special cases of currency name
        switch (strCurrency.strip()) {
            case "NORWAY KRONER":
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
    public List<ExchangeRate> mapExcelDataToExchangeRate(Row row, LocalDate date, String fileName) throws IncorectFormatCellException, ExchangeRatesException {
        List<ExchangeRate> exchangeRateList = new ArrayList<>();

        String strToCurrency = row.getCell(0).getStringCellValue().toUpperCase().strip();
        Currency toCurrency = currencyService.getCurrenciesByName(formatCurrencyInSpecialCase(strToCurrency));

        for (int i = 1; i <= 31; i++) {
            Cell cell = row.getCell(i);

            ExchangeRate exchangeRate = new ExchangeRate();
            if(cell.getCellType() != CellType.FORMULA)
                throw new IncorectFormatCellException(cell.getRowIndex() + ":" + cell.getColumnIndex(), fileName);
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

    public void importExchangeRate() throws IOException, IncorectFormatCellException, ExchangeRatesException {
        // Initialize folder path and file name
        String baseFolder = EnvironmentUtils.getEnvironmentValue("import-files.base-folder");
        String folderPath = baseFolder + EnvironmentUtils.getEnvironmentValue("import-files.currency");
        String fileName = "EXCSEP2023.xlsx";

        //Pattern for getting date from fileName
        Pattern pattern = Pattern.compile("^\\w{3}(\\w{3})(\\d{4}).");
        Matcher matcher = pattern.matcher(fileName);

        // Assign date get from fileName
        LocalDate date = LocalDate.now();
        if (matcher.find()) {
            String month = matcher.group(1);
            int year = Integer.parseInt(matcher.group(2));

            date = LocalDate.of(year, DateUtils.getMonth(month), 1);
        }

        InputStream is = new FileInputStream(folderPath + "/" + fileName);
        XSSFWorkbook workbook = new XSSFWorkbook(is);

        Sheet sheet = workbook.getSheet("Summary AOP");
        List<ExchangeRate> exchangeRatesList = new ArrayList<>();

        for (int i = 3; i <= 34; i++) {
            Row row = sheet.getRow(i);
            if (i == 3)
                fromCurrenciesTitle = getFromCurrencyTitle(row);
            else
                exchangeRatesList.addAll(mapExcelDataToExchangeRate(row, date, fileName));
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
        return optionalExchangeRate.orElse(null);
    }

    /**
     * Compare Currencies for reporting in Reports page
     */
    public Map<String, Object> compareCurrency(CompareCurrencyRequest request) throws ExchangeRatesException {
        Currency currentCurrency = currencyService.getCurrenciesByName(request.getCurrentCurrency());
        List<String> comparisonCurrencies = request.getComparisonCurrencies();
        LocalDate fromDate = parseDateFromRequest(request.getFromDate());
        LocalDate toDate = parseDateFromRequest(request.getToDate());

        // if fromDate and toDate is not set then limit will be 12
        // if toDate is not set => set current month & year
        // fromDate will be: toDate - limit (limit can be 12 or 60)
        int limit = fromDate == null || toDate == null ? 12 : 60;
        if(toDate == null) toDate = LocalDate.of(LocalDate.now().getYear(), LocalDate.now().getMonth(), 1);
        if(fromDate == null) fromDate = toDate.minusMonths(limit);

        Map<String, Object> data = new HashMap<>();
        List<String> stableCurrencies = new ArrayList<>();
        List<String> weakerCurrencies = new ArrayList<>();
        List<String> strongerCurrencies = new ArrayList<>();

        for (String strCurrency : comparisonCurrencies) {
            Currency currency = currencyService.getCurrenciesByName(strCurrency);
            List<ExchangeRate> exchangeRateList = new ArrayList<>();
            LocalDate queryDate = toDate;

            double nearestRate = 0;
            double farthestRate = 0;
            int numberOfMonths = 1;
            boolean isSetNearestRate = false;

            while(queryDate.isAfter(fromDate) && numberOfMonths <= limit) {
                Optional<ExchangeRate> optional = exchangeRateRepository.getExchangeRateByFromToCurrencyAndDate(currentCurrency.getCurrency(), strCurrency, queryDate);

                // if Exchange Rate in the month & year does not exist then the RATE value will be null
                if (optional.isEmpty()) exchangeRateList.add(new ExchangeRate(currentCurrency, currency, null, queryDate));
                else exchangeRateList.add(optional.get());

                // Set the nearest and farthest Exchange Rates to calculate difference
                if(optional.isPresent()) {
                    if(!isSetNearestRate) {
                        nearestRate = optional.get().getRate();
                        isSetNearestRate = true;
                    }
                    farthestRate = optional.get().getRate();
                }

                // update looping condition
                queryDate = queryDate.minusMonths(1);
                numberOfMonths++;
            }

            // Calculate the value difference between the nearest and the farthest Exchange Rates
            double differentRate = nearestRate - farthestRate;
            double differentRatePercentage = CurrencyFormatUtils.formatDoubleValue((differentRate / farthestRate) * 100, CurrencyFormatUtils.decimalFormatTwoDigits);

            if (Math.abs(differentRatePercentage) > 5) {
                StringBuilder sb = formatNumericValue(differentRate);
                if (differentRatePercentage < 0)
                    weakerCurrencies.add(currency.getCurrency() + ": " + sb + " (" + differentRatePercentage + "%)");
                else strongerCurrencies.add(currency.getCurrency() + ": +" + sb + " (+" + differentRatePercentage + "%)");
            } else stableCurrencies.add(currency.getCurrency());
            data.put(currency.getCurrency(), exchangeRateList);
        }

        data.put("stable", stableCurrencies);
        data.put("weakening", weakerCurrencies);
        data.put("strengthening", strongerCurrencies);
        return data;
    }

    public Map<String, Object> compareCurrencyFromAPI(CompareCurrencyRequest request) throws ExchangeRatesException {
        Currency currentCurrency = currencyService.getCurrenciesByName(request.getCurrentCurrency());
        List<String> comparisonCurrencies = request.getComparisonCurrencies();
        LocalDate fromDate = parseDateFromRequest(request.getFromDate());
        LocalDate toDate = parseDateFromRequest(request.getToDate());

        // if fromDate and toDate is not set then limit will be 12
        // if toDate is not set => set current month & year
        // fromDate will be: toDate - limit (limit can be 12 or 60)
        int limit = fromDate == null || toDate == null ? 12 : 60;
        if(toDate == null) toDate = LocalDate.of(LocalDate.now().getYear(), LocalDate.now().getMonth(), 1);
        if(fromDate == null) fromDate = toDate.minusMonths(limit);

        Map<String, Object> data = new HashMap<>();
        for (String currency : comparisonCurrencies) {
            data.put(currency, new ArrayList<>());
        }

        List<String> stableCurrencies = new ArrayList<>();
        List<String> weakerCurrencies = new ArrayList<>();
        List<String> strongerCurrencies = new ArrayList<>();

        LocalDate queryDate = toDate;
        int numberOfMonths = 1;
        while(queryDate.isAfter(fromDate) && numberOfMonths <= limit) {
            getExchangeRatesFromAPI(currentCurrency.getCurrency(), comparisonCurrencies, queryDate, data);
            // update looping condition
            queryDate = queryDate.minusMonths(1);
            numberOfMonths++;
        }

        for (String currency : comparisonCurrencies) {
            List<ExchangeRate> exchangeRateList = (List<ExchangeRate>) data.get(currency);
            if(exchangeRateList.size() > 2) {
                double nearestRate = exchangeRateList.get(0).getRate();
                double farthestRate = exchangeRateList.get(exchangeRateList.size() - 1).getRate();
                double differentRate = nearestRate - farthestRate;
                double differentRatePercentage = CurrencyFormatUtils.formatDoubleValue((differentRate / farthestRate) * 100, CurrencyFormatUtils.decimalFormatTwoDigits);

                if (Math.abs(differentRatePercentage) > 5) {
                    StringBuilder sb = formatNumericValue(differentRate);
                    if (differentRatePercentage < 0)
                        weakerCurrencies.add(currency + ": " + sb + " (" + differentRatePercentage + "%)");
                    else strongerCurrencies.add(currency + ": +" + sb + " (+" + differentRatePercentage + "%)");
                } else stableCurrencies.add(currency);
            }
        }

        data.put("stable", stableCurrencies);
        data.put("weakening", weakerCurrencies);
        data.put("strengthening", strongerCurrencies);
        return data;
    }

    private void getExchangeRatesFromAPI (String currentCurrency, List<String> comparisonCurrencies, LocalDate queryDate, Map<String, Object> result) throws ExchangeRatesException {
        RestTemplate template = new RestTemplate();
        String apiKey = EnvironmentUtils.getEnvironmentValue("exchange_rate_api_key");
        String url;

        LocalDate now = LocalDate.now();

        int year = queryDate.getYear();
        int month = queryDate.getMonthValue();
        int day = month == now.getMonthValue() ? now.getDayOfMonth() : queryDate.getMonth().minLength();

        Map<String, Object> response;
        Map<String, Object> conversionRates;

        try {
            url = "https://v6.exchangerate-api.com/v6/" + apiKey + "/history/" + currentCurrency + "/" + year + "/" + month + "/" + day;
            response = (Map<String, Object>) template.getForObject(url, Map.class);
            assert response != null;
            conversionRates = (Map<String, Object>) response.get("conversion_rates");
        } catch (Exception e) {
            if (e.getMessage().contains("404"))
                throw new ExchangeRatesException("Unsupported currency: " + currentCurrency, currentCurrency);
            else if (e.getMessage().contains("403"))
                throw new ExchangeRatesException("Inactive API Keys. Please check API Keys expired date.");
            else if (e.getMessage().contains("401"))
                throw new ExchangeRatesException("Plan updated required");
            else
                throw new ExchangeRatesException("Unexpected error");
        }

        for (String currency : comparisonCurrencies) {
            Object rateObject = conversionRates.get(currency);
            if(rateObject == null)
                throw new ExchangeRatesException("Unsupported currency before 31/12/2020: " + currency, currency);

            double rate = Double.parseDouble(conversionRates.get(currency).toString());
            ExchangeRate exchangeRate = new ExchangeRate(new Currency(currentCurrency), new Currency(currency), rate, queryDate);

            List<ExchangeRate> exchangeRateList = (List<ExchangeRate>) result.get(currency);
            exchangeRateList.add(exchangeRate);
            result.put(currency, exchangeRateList);
        }
    }

    /**
     * Format numeric value up to 7 decimal number
     */
    private StringBuilder formatNumericValue(double differentRate) {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);
        formatter.format("%,.7f", differentRate);

        String[] array = sb.toString().split("");
        sb.delete(0, sb.length());
        int i = array.length - 1;
        while (array[i].equals("0")) {
            array[i] = "";
            i--;
        }
        for (String s : array) {
            sb.append(s);
        }
        return sb;
    }

    public void importExchangeRateFromFile(MultipartFile file, Authentication authentication) throws Exception {
        String baseFolder = EnvironmentUtils.getEnvironmentValue("public-folder");
        String baseFolderUploaded = EnvironmentUtils.getEnvironmentValue("upload_files.base-folder");
        String targetFolder = EnvironmentUtils.getEnvironmentValue("upload_files.exchange_rate");
        String fileName = fileUploadService.saveFileUploaded(file, authentication, targetFolder, FileUtils.EXCEL_FILE_EXTENSION, ModelUtil.EXCHANGE_RATE);
        String filePath = baseFolder + baseFolderUploaded + targetFolder + fileName;

        // Verify the Excel file
        if (!FileUtils.isExcelFile(filePath))
            throw new InvalidFileFormatException(file.getOriginalFilename() + " is not Excel", file.getOriginalFilename(), "Excel");

        // Verify whether file's name is null or not
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null)
            throw new InvalidFileNameException(file.getOriginalFilename(), fileName);

        //Pattern for getting date from fileName
        Pattern pattern = Pattern.compile("^\\w{3}(\\w{3})(\\d{4}).");
        Matcher matcher = pattern.matcher(originalFileName);

        LocalDate date;
        if (matcher.find()) {
            String month = matcher.group(1);
            month = month.charAt(0) + month.substring(1).toLowerCase();
            int year = Integer.parseInt(matcher.group(2));
            date = LocalDate.of(year, DateUtils.getMonth(month), 1);
        } else
            throw new InvalidFileNameException(file.getOriginalFilename(), fileName);

        InputStream is = new FileInputStream(filePath);
        XSSFWorkbook workbook = new XSSFWorkbook(is);

        // Check the existence of sheet's name
        Sheet sheet = workbook.getSheet("Summary Current Interlocking");
        if(sheet == null) throw new MissingSheetException("Summary Current Interlocking", fileName);

        List<ExchangeRate> exchangeRatesList = new ArrayList<>();
        for (int i = 3; i <= 34; i++) {
            Row row = sheet.getRow(i);
            if (i == 3)
                fromCurrenciesTitle = getFromCurrencyTitle(row);
            else
                exchangeRatesList.addAll(mapExcelDataToExchangeRate(row, date, fileName));
        }

        exchangeRateRepository.saveAll(exchangeRatesList);
        log.info("ExchangeRate are newly saved or updated: " + exchangeRatesList.size());
        exchangeRatesList.clear();
        fileUploadService.handleUpdatedSuccessfully(fileName);
    }

    private LocalDate parseDateFromRequest(String dateFromRequest) {
        Pattern pattern = Pattern.compile("(\\d{4})-(\\d{2})");
        Matcher matcher = pattern.matcher(dateFromRequest);
        if(matcher.find()) {
            int year = Integer.parseInt(matcher.group(1));
            int month = Integer.parseInt(matcher.group(2));
            return LocalDate.of(year, month, 1);
        } else
            return null;
    }
}
