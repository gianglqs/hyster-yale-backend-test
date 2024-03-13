package com.hysteryale.service;

import com.hysteryale.model.Currency;
import com.hysteryale.model.ExchangeRate;
import com.hysteryale.model.reports.CompareCurrencyRequest;
import com.hysteryale.repository.ExchangeRateRepository;
import com.hysteryale.utils.CurrencyFormatUtils;
import com.hysteryale.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileUrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest
@Slf4j
@SuppressWarnings("unchecked")
public class ExchangeRateServiceTest {
    @Resource
    ExchangeRateService exchangeRateService;
    @Resource
    CurrencyService currencyService;
    @Resource
    ExchangeRateRepository exchangeRateRepository;
    @Resource
    AuthenticationManager authenticationManager;

    @Test
    public void testGetExchangeRate() {
        String from = "VND";
        String to = "USD";
        LocalDate monthYear = LocalDate.of(2050, Month.DECEMBER, 21);
        exchangeRateRepository.save(new ExchangeRate(new Currency(from), new Currency(to), 1.01, monthYear));

        ExchangeRate dbExchangeRate = exchangeRateService.getExchangeRate(from, to, monthYear);
        Assertions.assertEquals(from, dbExchangeRate.getFrom().getCurrency());
        Assertions.assertEquals(to, dbExchangeRate.getTo().getCurrency());
        Assertions.assertEquals(monthYear.getMonth(), dbExchangeRate.getDate().getMonth());
        Assertions.assertEquals(monthYear.getYear(), dbExchangeRate.getDate().getYear());
    }

    @Test
    public void testGetExchangeRate_notFound() {
        ExchangeRate dbExchangeRate = exchangeRateService.getExchangeRate("abc", "def", LocalDate.now());
        Assertions.assertNull(dbExchangeRate);
    }

    @Test
    public void testGetNearestExchangeRate() {
        String from = "VND";
        String to = "USD";
        LocalDate monthYear = LocalDate.now();
        exchangeRateRepository.save(new ExchangeRate(new Currency(from), new Currency(to), 1.01, monthYear));

        ExchangeRate dbExchangeRate = exchangeRateService.getNearestExchangeRate(from, to);
        Assertions.assertEquals(from, dbExchangeRate.getFrom().getCurrency());
        Assertions.assertEquals(to, dbExchangeRate.getTo().getCurrency());
        Assertions.assertEquals(monthYear.getMonth(), dbExchangeRate.getDate().getMonth());
        Assertions.assertEquals(monthYear.getYear(), dbExchangeRate.getDate().getYear());
    }

    @Test
    public void testGetNearestExchangeRate_notFound() {
        ExchangeRate dbExchangeRate = exchangeRateService.getNearestExchangeRate("abc", "def");
        Assertions.assertNull(dbExchangeRate);
    }

    @Test
    public void testImportExchangeRateFromFile() throws Exception {
        String filePath = "import_files/currency_exchangerate/EXCSEP2023.xlsx";

        // Set up Authentication
        String username = "user1@gmail.com";
        String password = "123456";

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        password
                )
        );

        // Set up uploaded file
        org.springframework.core.io.Resource fileResource = new ClassPathResource(filePath);
        Assertions.assertNotNull(fileResource);

        MultipartFile file = new MockMultipartFile(
                "file",
                fileResource.getFilename(),
                MediaType.MULTIPART_FORM_DATA_VALUE,
                fileResource.getInputStream()
        );
        exchangeRateService.importExchangeRateFromFile(file, authentication);

        Pattern pattern = Pattern.compile("^\\w{3}(\\w{3})(\\d{4}).");
        Matcher matcher = pattern.matcher(file.getOriginalFilename());

        LocalDate date;
        if(matcher.find()) {
            String month = matcher.group(1);
            month = month.charAt(0) + month.substring(1).toLowerCase();
            int year = Integer.parseInt(matcher.group(2));
            date = LocalDate.of(year, DateUtils.getMonth(month), 1);
        }
        else
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File's name is not in appropriate format");

        InputStream is = new FileInputStream(filePath);
        XSSFWorkbook workbook = new XSSFWorkbook(is);
        Sheet sheet = workbook.getSheet("Summary Current Interlocking");
        Map<Integer, String> fromCurrenciesTitle = getFromCurrencyTitle(sheet.getRow(3));

        Random random = new Random();

        for(int i = 0; i < 10; i++) {
            int cellIndex = random.nextInt(30) + 1;
            int rowIndex = random.nextInt(31) + 4;

            String strFromCurrency = fromCurrenciesTitle.get(cellIndex).toUpperCase().strip();
            log.info(strFromCurrency);
            Currency fromCurrency = currencyService.getCurrenciesByName(exchangeRateService.formatCurrencyInSpecialCase(strFromCurrency));

            Row row = sheet.getRow(rowIndex);
            String strToCurrency = row.getCell(0).getStringCellValue().toUpperCase().strip();
            log.info(strToCurrency);
            Currency toCurrency = currencyService.getCurrenciesByName(exchangeRateService.formatCurrencyInSpecialCase(strToCurrency));


            Optional<ExchangeRate> dbExchangeRate = exchangeRateRepository.getExchangeRateByFromToCurrencyAndDate(fromCurrency.getCurrency(), toCurrency.getCurrency(), date);
            Assertions.assertNotNull(dbExchangeRate);

            double rate = row.getCell(cellIndex).getNumericCellValue();
            Assertions.assertEquals(
                    CurrencyFormatUtils.formatDoubleValue(rate, CurrencyFormatUtils.decimalFormatFourDigits),
                    CurrencyFormatUtils.formatDoubleValue(dbExchangeRate.get().getRate(), CurrencyFormatUtils.decimalFormatFourDigits)
            );
        }
    }

    @Test
    public void testImportExchangeRateFromFile_errorCases() throws Exception {
        // Set up Authentication
        String username = "user1@gmail.com";
        String password = "123456";

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        password
                )
        );

        // Test case: Uploaded file is not an Excel file.
        String filePath1 = Objects.requireNonNull(getClass().getClassLoader().getResource("fakeExcelFile.xlsx")).getPath();
        org.springframework.core.io.Resource fileResource1 = new FileUrlResource(filePath1);
        Assertions.assertNotNull(fileResource1);

        MultipartFile file1 = new MockMultipartFile(
                "file1",
                fileResource1.getFilename(),
                MediaType.MULTIPART_FORM_DATA_VALUE,
                fileResource1.getInputStream()
        );
        ResponseStatusException exception1 = Assertions.assertThrows(
                ResponseStatusException.class,
                () -> exchangeRateService.importExchangeRateFromFile(file1, authentication)
        );
        Assertions.assertEquals(400, exception1.getStatus().value());
        Assertions.assertEquals("Uploaded file is not an Excel file", exception1.getReason());


        // Test case: Uploaded file's name is not in appropriate format
        String filePath2 = Objects.requireNonNull(getClass().getClassLoader().getResource("excelFileToTest.xlsx")).getPath();
        org.springframework.core.io.Resource fileResource2 = new FileUrlResource(filePath2);
        Assertions.assertNotNull(fileResource2);

        MultipartFile file2 = new MockMultipartFile(
                "file2",
                fileResource2.getFilename(),
                MediaType.MULTIPART_FORM_DATA_VALUE,
                fileResource2.getInputStream()
        );
        ResponseStatusException exception2 = Assertions.assertThrows(
                ResponseStatusException.class,
                () -> exchangeRateService.importExchangeRateFromFile(file2, authentication)
        );
        Assertions.assertEquals(400, exception2.getStatus().value());
        Assertions.assertEquals("File's name is not in appropriate format", exception2.getReason());
    }

    private Map<Integer, String> getFromCurrencyTitle(Row row) {
        HashMap<Integer, String> fromCurrenciesTitle = new HashMap<>();

        int end = 31;
        for(int i = 1; i <= end; i++) {
            String currency = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
            fromCurrenciesTitle.put(i, currency.toUpperCase());
        }
        return fromCurrenciesTitle;
    }

    @Test
    public void testCompareExchangeRate() {
        String currency1 = "USD";
        String currency2 = "JPY";
        String currency3 = "VND";

        CompareCurrencyRequest request = new CompareCurrencyRequest(
                "EUR",
                List.of(currency1, currency2, currency3),
                false,
                "",
                ""
        );
        int limit = request.getFromDate().isEmpty() || request.getToDate().isEmpty() ? 12 : 60;
        Map<String, Object> result = exchangeRateService.compareCurrency(request);
        Assertions.assertNotNull(result.get(currency1));
        Assertions.assertNotNull(result.get(currency2));
        Assertions.assertNotNull(result.get(currency3));
        Assertions.assertNotNull(result.get("stable"));
        Assertions.assertNotNull(result.get("weakening"));
        Assertions.assertNotNull(result.get("strengthening"));

        int stableCurrencies = 0;
        int weakerCurrencies = 0;
        int strongerCurrencies = 0;

        for(String currency : request.getComparisonCurrencies()) {
            List<ExchangeRate> exchangeRateList = exchangeRateRepository.getCurrentExchangeRate(
                    request.getCurrentCurrency(), currency,
                    parseDateFromRequest(request.getFromDate()), parseDateFromRequest(request.getToDate()),
                    limit
            );

            if(exchangeRateList.isEmpty())
                continue;
            double nearestRate = exchangeRateList.get(0).getRate();
            double farthestRate = exchangeRateList.get(exchangeRateList.size() - 1).getRate();
            double differentRate = CurrencyFormatUtils.formatDoubleValue(nearestRate - farthestRate, CurrencyFormatUtils.decimalFormatFourDigits);
            double differentRatePercentage = CurrencyFormatUtils.formatDoubleValue((differentRate / farthestRate) * 100, CurrencyFormatUtils.decimalFormatFourDigits);

            if(Math.abs(differentRatePercentage) > 5) {
                if(differentRatePercentage < 0) weakerCurrencies ++;
                else strongerCurrencies ++;
            }
            else stableCurrencies ++;
        }

        Assertions.assertEquals(stableCurrencies, ((List<String>) result.get("stable")).size());
        Assertions.assertEquals(weakerCurrencies, ((List<String>) result.get("weakening")).size());
        Assertions.assertEquals(strongerCurrencies, ((List<String>) result.get("strengthening")).size());
    }

    private LocalDate parseDateFromRequest(String dateFromRequest) {
        Pattern pattern = Pattern.compile("(\\d{4})-(\\d{2})");
        Matcher matcher = pattern.matcher(dateFromRequest);
        if(matcher.find()) {
            int year = Integer.parseInt(matcher.group(1));
            int month = Integer.parseInt(matcher.group(2));
            return LocalDate.of(year, month, 1);
        } else return null;
    }


}
