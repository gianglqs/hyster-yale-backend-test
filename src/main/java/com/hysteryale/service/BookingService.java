package com.hysteryale.service;

import com.hysteryale.exception.BlankSheetException;
import com.hysteryale.exception.MissingColumnException;
import com.hysteryale.exception.MissingSheetException;
import com.hysteryale.model.Currency;
import com.hysteryale.model.*;
import com.hysteryale.model.enums.ImportFailureType;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.model.importFailure.ImportFailure;
import com.hysteryale.model.marginAnalyst.MarginAnalystMacro;
import com.hysteryale.repository.*;
import com.hysteryale.repository.importFailure.ImportFailureRepository;
import com.hysteryale.repository.upload.FileUploadRepository;
import com.hysteryale.service.marginAnalyst.MarginAnalystMacroService;
import com.hysteryale.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@SuppressWarnings("unchecked")
public class BookingService extends BasedService {
    @Resource
    BookingRepository bookingRepository;
    @Resource
    ProductService productService;

    @Resource
    ProductRepository productRepository;

    @Resource
    AOPMarginService aopMarginService;

    @Resource
    AOPMarginRepository aopMarginRepository;

    @Resource
    PartRepository partRepository;

    @Resource
    MarginAnalystMacroService marginAnalystMacroService;

    @Resource
    PartService partService;

    @Resource
    ExchangeRateService exchangeRateService;

    @Resource
    RegionService regionService;

    @Resource
    CurrencyService currencyService;

    @Resource
    RegionRepository regionRepository;

    @Resource
    DealerRepository dealerRepository;

    @Resource
    FileUploadRepository fileUploadRepository;

    @Resource
    CountryService countryService;


    @Resource
    CountryRepository countryRepository;

    @Resource
    ImportFailureService importFailureService;

    @Resource
    DealerService dealerService;

    @Resource
    ImportFailureRepository importFailureRepository;

    @Resource
    LocaleUtils localeUtils;

    /**
     * Get Columns' name in Booking Excel file, then store them (columns' name) respectively with the index into HashMap
     *
     * @param row which contains columns' name
     */
    public void getOrderColumnsName(Row row, HashMap<String, Integer> ORDER_COLUMNS_NAME) {
        for (int i = 0; i < 50; i++) {
            if (row.getCell(i) != null) {
                String columnName = row.getCell(i).getStringCellValue().trim();
                if (ORDER_COLUMNS_NAME.containsKey(columnName)) continue;
                ORDER_COLUMNS_NAME.put(columnName, i);
            }
        }
    }

    /**
     * Get all files having name starting with {01. Bookings Register} and ending with {.xlsx}
     *
     * @param folderPath path to folder contains Booking Order
     * @return list of files' name
     */
    public List<String> getAllFilesInFolder(String folderPath) {
        Pattern pattern = Pattern.compile(".*(.xlsx)$");


        List<String> fileList = new ArrayList<>();
        Matcher matcher;
        try {
            DirectoryStream<Path> folder = Files.newDirectoryStream(Paths.get(folderPath));
            for (Path path : folder) {
                matcher = pattern.matcher(path.getFileName().toString());
                if (matcher.matches()) fileList.add(path.getFileName().toString());
                else logError("Wrong formatted file's name: " + path.getFileName().toString());
            }
        } catch (Exception e) {
            logInfo(e.getMessage());

        }
        return fileList;
    }


    Booking mapExcelDataIntoOrderObject(Row row, HashMap<String, Integer> ORDER_COLUMNS_NAME, List<Product> products,
                                        List<Region> regions, List<AOPMargin> aopMargins, List<Dealer> dealers,
                                        List<Country> countries, List<ImportFailure> importFailures, Set<Country> newCountrySet,
                                        Set<Region> newRegionSet) {
        Booking booking = new Booking();

        //set OrderNo

        String orderNo = row.getCell(ORDER_COLUMNS_NAME.get("ORDERNO")).getStringCellValue();
        booking.setOrderNo(orderNo);


        // Series
        String series;
        series = row.getCell(ORDER_COLUMNS_NAME.get("SERIES")).getStringCellValue();
        booking.setSeries(series);


        // set billToCost
        Cell billtoCell = row.getCell(ORDER_COLUMNS_NAME.get("BILLTO"));
        booking.setBillTo(billtoCell.getStringCellValue());


        //set model
        Cell modelCell = row.getCell(ORDER_COLUMNS_NAME.get("MODEL"));
        //set ProductDimension
        Product product = null;
        for (Product p : products) {
            if (p.equals(modelCell.getStringCellValue(), series))
                product = p;
        }
        if (product == null) {
            String reasonValue = modelCell.getStringCellValue() + "###" + series;
            importFailureService.addIntoListImportFailure(importFailures, orderNo, "not-find-product-with-modelCode-series", reasonValue, ImportFailureType.ERROR);
            return null;
        }
        booking.setProduct(product);


        //set date
        String strDate = String.valueOf(row.getCell(ORDER_COLUMNS_NAME.get("DATE")).getNumericCellValue());
        Pattern pattern = Pattern.compile("^\\d(\\d\\d)(\\d\\d)(\\d\\d)");
        Matcher matcher = pattern.matcher(strDate);
        int year, month, day;

        if (matcher.find()) {
            year = Integer.parseInt(matcher.group(1)) + 2000;
            month = Integer.parseInt(matcher.group(2));
            day = Integer.parseInt(matcher.group(3));

            booking.setDate(LocalDate.of(year, DateUtils.getMonth(month), day));
        }


        // dealer
        String dealerName = row.getCell(ORDER_COLUMNS_NAME.get("DEALERNAME")).getStringCellValue();
        Dealer dealer = dealerService.getDealerByName(dealers, dealerName);
        if (dealer == null) {
            importFailureService.addIntoListImportFailure(importFailures, orderNo, "not-find-Dealer-with-name", dealerName, ImportFailureType.ERROR);
            return null;
        }
        booking.setDealer(dealer);

        //get region
        String regionCode = row.getCell(ORDER_COLUMNS_NAME.get("REGION")).getStringCellValue();
        Region region = regionService.getRegionInListRegionByShortName(regions, regionCode);
        if (region == null) {
            region = new Region();
            region.setRegionShortName(regionCode);
            newRegionSet.add(region);
        }

        // country
        Cell ctryCodeCell = row.getCell(ORDER_COLUMNS_NAME.get("CTRYCODE"));
        String ctryCode = ctryCodeCell.getStringCellValue();
        Country country = countryService.findByCountryCode(countries, ctryCode);
        if (country == null) {
            // create new Country with ctry_code and region
            country = new Country();
            country.setCode(ctryCode);
            country.setRegion(region);
            newCountrySet.add(country);
            importFailureService.addIntoListImportFailure(importFailures, orderNo,
                    "not-find-country-with-code", ctryCode, ImportFailureType.WARNING);
        }
        booking.setCountry(country);


        // truck class
        Cell truckClass = row.getCell(ORDER_COLUMNS_NAME.get("TRUCKCLASS"));
        booking.setTruckClass(truckClass.getStringCellValue());

        // Order type
        Cell orderType = row.getCell(ORDER_COLUMNS_NAME.get("ORDERTYPE"));
        booking.setOrderType(orderType.getStringCellValue());


        // Dealer Pro
        Cell dealerPo = row.getCell(ORDER_COLUMNS_NAME.get("DEALERPO"));
        booking.setDealerPO(dealerPo.getStringCellValue());

        // AOPMargin
        AOPMargin aopMargin = aopMarginService.getAOPMargin(aopMargins, booking.getCountry().getRegion(), booking.getSeries(), booking.getProduct().getPlant(), booking.getDate());
        if (aopMargin == null) {
            String reasonValue = booking.getCountry().getRegion().getRegionName() + "###" + series.substring(1) + "###" + booking.getProduct().getPlant() + "###" + booking.getDate().getYear();
            importFailureService.addIntoListImportFailure(importFailures, orderNo,
                    "not-find-AOPMargin-with-region-series-plant-year", reasonValue,
                    ImportFailureType.ERROR);
            return null;
        }

        booking.setAOPMargin(aopMargin);


        return booking;
    }

    private Date extractDate(String fileName) {
        String dateRegex = "\\d{2}_\\d{2}_\\d{4}";
        Matcher m = Pattern.compile(dateRegex).matcher(fileName);
        Date date = null;
        try {
            if (m.find()) {
                date = new SimpleDateFormat("MM_dd_yyyy").parse(m.group());
                date.setMonth(date.getMonth() - 1); //TODO recheck month of file
            } else {
                logError("Can not extract Date from File name: " + fileName);
            }

        } catch (java.text.ParseException e) {
            logError("Can not extract Date from File name: " + fileName);
        }
        return date;
    }


    /**
     * new
     */
    public void importOrder() throws IOException, IllegalAccessException, MissingColumnException, MissingSheetException, BlankSheetException {

        // Folder contains Excel file of Booking Order
        String baseFolder = EnvironmentUtils.getEnvironmentValue("import-files.base-folder");
        String folderPath = baseFolder + EnvironmentUtils.getEnvironmentValue("import-files.booked-order");

        // Get files in Folder Path
        List<String> fileList = getAllFilesInFolder(folderPath);
        String[] listMonth = DateUtils.getAllMonthsAsString();

        String month = "", year = "";

        for (String fileName : fileList) {
            String pathFile = folderPath + "/" + fileName;
            //check file has been imported ?
            if (isImported(pathFile)) {
                logWarning("file '" + fileName + "' has been imported");
                continue;
            }

            logInfo("{ Start importing file: '" + fileName + "'");
            for (String shortMonth : listMonth) {
                String yearRegex = "\\b\\d{4}\\b";
                Pattern pattern = Pattern.compile(yearRegex, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(fileName);
                if (matcher.find()) {
                    year = matcher.group();
                }
                if (fileName.toLowerCase().contains(shortMonth.toLowerCase())) {
                    month = shortMonth;
                }
            }


            boolean isOldData = checkOldData(month, year);
            if (isOldData) {

                importOldBookingFileByFile(pathFile, month, year);
            } else {
                InputStream getListCostDataByMonthAndYear = getInputStreamForCostData(month, year);
                importNewBookingFileByFile(pathFile, getListCostDataByMonthAndYear);
            }
            updateStateImportFile(pathFile);
        }
    }

    private void importNewBookingFileByFile(String filePath, InputStream isListCostData) throws IOException, MissingColumnException, MissingSheetException, BlankSheetException {
        InputStream is = new FileInputStream(filePath);
        XSSFWorkbook workbook = new XSSFWorkbook(is);
        List<Booking> bookingList = new LinkedList<>();

        String sheetName = CheckRequiredColumnUtils.BOOKING_REQUIRED_SHEET;
        XSSFSheet orderSheet = workbook.getSheet(sheetName);
        if (orderSheet == null)
            throw new MissingSheetException(sheetName, "need check"); // TODO: need check


        HashMap<String, Integer> ORDER_COLUMNS_NAME = new HashMap<>();
        List<String> USPlant = PlantUtil.getUSPlant();
        List<CostDataFile> listCostDataByMonthAndYear = getListCostDataByMonthAndYear(isListCostData);
        int numRowName = 0;

        // prepare data for import
        List<Product> products = productRepository.findAll();
        List<Region> regions = regionRepository.findAll();
        List<AOPMargin> aopMargins = aopMarginRepository.findAll();
        List<Dealer> dealers = dealerRepository.findAll();
        List<Country> countries = countryRepository.findAll();
        List<ImportFailure> importFailures = new ArrayList<>();
        Set<Country> newCountrySet = new HashSet<>();
        Set<Region> newRegionSet = new HashSet<>();

        //get list cost data from month and year

        for (Row row : orderSheet) {
            if (row.getRowNum() == numRowName) {
                getOrderColumnsName(row, ORDER_COLUMNS_NAME);
                CheckRequiredColumnUtils.checkRequiredColumn(new ArrayList<>(ORDER_COLUMNS_NAME.keySet()), CheckRequiredColumnUtils.BOOKING_REQUIRED_COLUMN, "");
            } else if (!row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().isEmpty() && row.getRowNum() > numRowName) {
                Booking newBooking = mapExcelDataIntoOrderObject(row, ORDER_COLUMNS_NAME, products, regions, aopMargins, dealers, countries, importFailures, newCountrySet, newRegionSet);

                if (newBooking == null)
                    continue;

                // import DN, DNAfterSurcharge
                newBooking = importDNAndDNAfterSurcharge(newBooking);
                if (newBooking == null)
                    continue;

                // check productDimension
                if (USPlant.contains(newBooking.getProduct().getPlant())) {
                    newBooking = setTotalCost(newBooking, listCostDataByMonthAndYear);
                    logInfo("US Plant");
                } else {
                    newBooking = importCostRMBOfEachParts(newBooking);
                }
                newBooking = calculateMargin(newBooking);
                bookingList.add(newBooking);

            }
        }
        bookingRepository.saveAll(bookingList);

    }

    public List<ImportFailure> importNewBookingFileByFile(String filePath, String fileUUID) throws IOException, MissingColumnException, MissingSheetException, BlankSheetException {

        InputStream is = new FileInputStream(filePath);
        XSSFWorkbook workbook = new XSSFWorkbook(is);
        List<Booking> bookingList = new LinkedList<>();
        HashMap<String, Integer> ORDER_COLUMNS_NAME = new HashMap<>();
        List<String> USPlant = PlantUtil.getUSPlant();
        String sheetName = CheckRequiredColumnUtils.BOOKING_REQUIRED_SHEET;
        Sheet orderSheet = workbook.getSheet(sheetName);
        int numRowName = 0;
        if (orderSheet == null)
            throw new MissingSheetException(sheetName, fileUUID);

        if (orderSheet.getLastRowNum() <= 0)
            throw new BlankSheetException(sheetName, fileUUID);

        // prepare data for import
        List<Product> products = productRepository.findAll();
        List<Region> regions = regionRepository.findAll();
        List<AOPMargin> aopMargins = aopMarginRepository.findAll();
        List<Dealer> dealers = dealerRepository.findAll();
        List<Country> countries = countryRepository.findAll();
        List<ImportFailure> importFailures = new ArrayList<>();
        Set<Country> newCountrySet = new HashSet<>();
        Set<Region> newRegionSet = new HashSet<>();

        for (Row row : orderSheet) {
            if (row.getRowNum() == numRowName) {
                getOrderColumnsName(row, ORDER_COLUMNS_NAME);
                CheckRequiredColumnUtils.checkRequiredColumn(new ArrayList<>(ORDER_COLUMNS_NAME.keySet()), CheckRequiredColumnUtils.BOOKING_REQUIRED_COLUMN, fileUUID);
            } else if (!row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().isEmpty() && row.getRowNum() > numRowName) {
                Booking newBooking = mapExcelDataIntoOrderObject(row, ORDER_COLUMNS_NAME, products, regions, aopMargins, dealers, countries, importFailures, newCountrySet, newRegionSet);

                if (newBooking == null)
                    continue;

                // import DN, DNAfterSurcharge
                newBooking = importDNAndDNAfterSurcharge(newBooking);
                if (newBooking == null)
                    continue;

                if (USPlant.contains(newBooking.getProduct().getPlant())) {
                    logInfo("US Plant");
                    // import totalCost when import file totalCost

                    Optional<Booking> orderExisted = bookingRepository.getBookingOrderByOrderNo(newBooking.getOrderNo());
                    if (orderExisted.isPresent()) {
                        Booking oldBooking = orderExisted.get();
                        newBooking.setCurrency(oldBooking.getCurrency());
                        newBooking.setAOPMargin(oldBooking.getAOPMargin());
                        newBooking.setMarginPercentageAfterSurcharge(oldBooking.getMarginPercentageAfterSurcharge());
                        newBooking.setMarginAfterSurcharge(oldBooking.getMarginAfterSurcharge());
                        newBooking.setTotalCost(oldBooking.getTotalCost());
                    }
                } else {
                    newBooking = importCostRMBOfEachParts(newBooking);
                }

                newBooking = calculateMargin(newBooking);
                bookingList.add(newBooking);
            }

        }
        log.info("list booked" + bookingList.size());
        importFailureService.setFileUUIDForListImportFailure(importFailures, fileUUID);
        bookingRepository.saveAll(bookingList);
        importFailureRepository.saveAll(importFailures);
        countryRepository.saveAll(newCountrySet);

        localeUtils.logStatusImportComplete(importFailures, ModelUtil.SHIPMENT);

        return importFailures;

    }

    //get Booking Exist
    private List<Booking> getListBookingExist(List<Booking> booking) {
        List<String> listOrderNo = new ArrayList<>();
        booking.forEach(b -> listOrderNo.add(b.getOrderNo()));
        return bookingRepository.getListBookingExist(listOrderNo);
    }


    public void importOldBookingFileByFile(String pathFile, String month, String year) throws IOException, MissingColumnException {
        //step 1: import fact data from booking-register file
        //step 2: import Margin data from booking-register file
        //step 3: calculate totalCost
        InputStream is = new FileInputStream(pathFile);
        XSSFWorkbook workbook = new XSSFWorkbook(is);
        List<Booking> bookingList = new LinkedList<>();
        HashMap<String, Integer> ORDER_COLUMNS_NAME = new HashMap<>();

        Sheet orderSheet = workbook.getSheet("NOPLDTA.NOPORDP,NOPLDTA.>Sheet1");

        List<MarginDataFile> marginDataFileList = getListMarginDataByMonthAndYear(month, year);

        // prepare data for import
        List<Product> products = productRepository.findAll();
        List<Region> regions = regionRepository.findAll();
        List<AOPMargin> aopMargins = aopMarginRepository.findAll();
        List<Dealer> dealers = dealerRepository.findAll();
        List<Country> countries = countryRepository.findAll();
        List<ImportFailure> importFailures = new ArrayList<>();
        Set<Country> newCountrySet = new HashSet<>();
        Set<Region> newRegionSet = new HashSet<>();

        for (Row row : orderSheet) {
            if (row.getRowNum() == 0) {
                getOrderColumnsName(row, ORDER_COLUMNS_NAME);
                CheckRequiredColumnUtils.checkRequiredColumn(new ArrayList<>(ORDER_COLUMNS_NAME.keySet()), CheckRequiredColumnUtils.BOOKING_REQUIRED_COLUMN, "");// TODO: need check
            } else if (!row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().isEmpty() && row.getRowNum() > 1) {
                // map data from excel file
                Booking newBooking = mapExcelDataIntoOrderObject(row, ORDER_COLUMNS_NAME, products, regions, aopMargins, dealers, countries, importFailures, newCountrySet, newRegionSet);

                if (newBooking == null)
                    continue;

                newBooking = importDNAndDNAfterSurcharge(newBooking);
                if (newBooking == null)
                    continue;
                newBooking = importOldMarginPercentageAndCurrency(newBooking, marginDataFileList);
                newBooking = calculateTotalCostAndMarginAfterSurcharge(newBooking);
                bookingList.add(newBooking);

            }
        }

        bookingRepository.saveAll(bookingList);
    }

    public Booking importOldMarginPercentageAndCurrency(Booking booking, List<MarginDataFile> marginDataFileList) {
        for (MarginDataFile marginDataFile : marginDataFileList) {
            if (marginDataFile.orderNo.equals(booking.getOrderNo())) {
                booking.setMarginPercentageAfterSurcharge(marginDataFile.marginPercentage);
                break;
            }
        }
        return booking;
    }

    public Booking setTotalCost(Booking booking, List<CostDataFile> costDataFileList) {
        for (CostDataFile costDataFile : costDataFileList) {
            if (costDataFile.orderNo.equals(booking.getOrderNo())) {
                booking.setTotalCost(costDataFile.totalCost);
                break;
            }
        }
        return booking;
    }

    public Booking importCostRMBOfEachParts(Booking booking) {
        List<String> listPartNumber = partService.getAllPartNumbersByOrderNo(booking.getOrderNo());
        Currency currency = partService.getCurrencyByOrderNo(booking.getOrderNo());

        LocalDate date = LocalDate.of(booking.getDate().getYear(), booking.getDate().getMonth(), 1);

        if (currency == null)
            return booking;
        logInfo(booking.getOrderNo() + "   " + currency.getCurrency());
        double totalCost = 0;
        if (!booking.getProduct().getPlant().equals("SN")) { // plant is Hysteryale, Maximal, Ruyi, Staxx
            List<MarginAnalystMacro> marginAnalystMacroList = marginAnalystMacroService.getMarginAnalystMacroByHYMPlantAndListPartNumber(
                    booking.getProduct().getModelCode(), listPartNumber, booking.getCurrency().getCurrency(), date);
            for (MarginAnalystMacro marginAnalystMacro : marginAnalystMacroList) {
                totalCost += marginAnalystMacro.getCostRMB();
            }
            // exchange rate
            ExchangeRate exchangeRate = exchangeRateService.getNearestExchangeRate("CNY", booking.getCurrency().getCurrency());
            if (exchangeRate != null) {
                totalCost *= exchangeRate.getRate();
                logInfo("None SN list " + marginAnalystMacroList.size() + "  " + booking.getOrderNo() + "  " + booking.getProduct().getModelCode() + "  " + exchangeRate.getRate());
            }
        } else { // plant is SN
            List<MarginAnalystMacro> marginAnalystMacroList = marginAnalystMacroService.getMarginAnalystMacroByPlantAndListPartNumber(
                    booking.getProduct().getModelCode(), listPartNumber, booking.getCurrency().getCurrency(),
                    booking.getProduct().getPlant(), date);

            for (MarginAnalystMacro marginAnalystMacro : marginAnalystMacroList) {
                totalCost += marginAnalystMacro.getCostRMB();
            }
            ExchangeRate exchangeRate = exchangeRateService.getNearestExchangeRate("USD", booking.getCurrency().getCurrency());
            if (exchangeRate != null) {
                totalCost *= exchangeRate.getRate();
                logInfo(" SN list " + marginAnalystMacroList.size() + "  " + booking.getOrderNo() + "  " + booking.getProduct().getModelCode() + "  " + exchangeRate.getRate());
            }
        }


        booking.setTotalCost(totalCost);
        logInfo(booking.getTotalCost() + "");
        return booking;
    }

    private InputStream getInputStreamForCostData(String month, String year) throws IOException {

        String baseFolder = EnvironmentUtils.getEnvironmentValue("import-files.base-folder");

        List<String> listMonth = Arrays.asList(DateUtils.getAllMonthsAsString());

        String folderPath;

        String targetFolder = EnvironmentUtils.getEnvironmentValue("import-files.total-cost");
        folderPath = baseFolder + targetFolder;
        // Get files in Folder Path
        List<String> fileList = getAllFilesInFolder(folderPath);
        for (String fileName : fileList) {
            // if data is new extract file name Cost_Data_10_09_2023_11_01_37 -> Date -> month,year

            if (fileName.contains(year) && listMonth.get(extractDate(fileName).getMonth()).toLowerCase().contains(month.toLowerCase())) {
                return new FileInputStream(folderPath + "/" + fileName);
            }
        }
        return null;
    }

    private List<CostDataFile> getListCostDataByMonthAndYear(InputStream is) throws IOException, MissingColumnException, MissingSheetException, BlankSheetException {
        List<CostDataFile> result = new ArrayList<>();

        XSSFWorkbook workbook = new XSSFWorkbook(is);
        // if old data -> colect from sheet "Wk - Margins", else -> sheet "Cost Data"

        String sheetName = CheckRequiredColumnUtils.BOOKING_COST_DATA_REQUIRED_SHEET;
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null)
            throw new MissingSheetException(sheetName, "need check"); // TODO: need check

        if (sheet.getLastRowNum() <= 0)
            throw new BlankSheetException(sheetName, "need check"); // TODO: need check

        HashMap<String, Integer> ORDER_COLUMNS_NAME = new HashMap<>();
        for (Row row : sheet) {
            if (row.getRowNum() == 0) {
                getOrderColumnsName(row, ORDER_COLUMNS_NAME);
                CheckRequiredColumnUtils.checkRequiredColumn(new ArrayList<>(ORDER_COLUMNS_NAME.keySet()), CheckRequiredColumnUtils.BOOKING_COST_DATA_REQUIRED_COLUMN, "");// TODO: need check
            } else if (!row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().isEmpty() && row.getRowNum() > 0) {

                // create CostDataFile
                CostDataFile costDataFile = new CostDataFile();

                // get orderNo
                Cell orderNOCell = row.getCell(ORDER_COLUMNS_NAME.get("Order"));
                costDataFile.orderNo = orderNOCell.getStringCellValue();

                // get TotalCost
                Cell totalCostCell = row.getCell(ORDER_COLUMNS_NAME.get("TOTAL MFG COST Going-To"));
                if (totalCostCell.getCellType() == CellType.NUMERIC) {
                    costDataFile.totalCost = totalCostCell.getNumericCellValue();
                } else if (totalCostCell.getCellType() == CellType.STRING) {
                    costDataFile.totalCost = Double.parseDouble(totalCostCell.getStringCellValue());
                }

                result.add(costDataFile);
            }
        }
        return result;
    }


    /**
     * getListMarginDataByMonthAndYear
     *
     * @param month
     * @param year
     */
    private List<MarginDataFile> getListMarginDataByMonthAndYear(String month, String year) throws IOException {
        List<MarginDataFile> result = new ArrayList<>();
        String baseFolder = EnvironmentUtils.getEnvironmentValue("import-files.base-folder");

        String folderPath;

        String targetFolder = EnvironmentUtils.getEnvironmentValue("import-files.booking");
        folderPath = baseFolder + targetFolder;
        // Get files in Folder Path
        List<String> fileList = getAllFilesInFolder(folderPath);
        for (String fileName : fileList) {

            if (fileName.contains(year) && fileName.toLowerCase().contains(month.toLowerCase())) {
                InputStream is = new FileInputStream(folderPath + "/" + fileName);
                XSSFWorkbook workbook = new XSSFWorkbook(is);
                // if old data -> colect from sheet "Wk - Margins"
                Sheet sheet = workbook.getSheet("Wk - Margins");

                HashMap<String, Integer> ORDER_COLUMNS_NAME = new HashMap<>();
                for (Row row : sheet) {
                    if (row.getRowNum() == 1) getOrderColumnsName(row, ORDER_COLUMNS_NAME);
                    else if (!row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().isEmpty() && row.getRowNum() > 1) {

                        // create MarginDataFile
                        MarginDataFile marginDataFile = new MarginDataFile();

                        // get orderNo
                        Cell orderNOCell = row.getCell(ORDER_COLUMNS_NAME.get("Order #"));
                        marginDataFile.orderNo = orderNOCell.getStringCellValue();

                        // get margin
                        Cell marginCell = row.getCell(ORDER_COLUMNS_NAME.get("Margin @ AOP Rate"));
                        if (marginCell.getCellType() == CellType.NUMERIC) {
                            marginDataFile.marginPercentage = marginCell.getNumericCellValue();
                        } else {
                            marginDataFile.marginPercentage = 0;
                        }

                        result.add(marginDataFile);
                    }
                }
            }
        }
        return result;
    }

    public void importCostData(String filePath, String savedFileName) throws IOException, MissingColumnException, MissingSheetException, BlankSheetException {
        InputStream is = new FileInputStream(filePath);
        List<CostDataFile> costDataList = getListCostDataByMonthAndYear(is);
        List<String> listOrderNo = new ArrayList<>();
        costDataList.forEach(c -> listOrderNo.add(c.orderNo));
        List<Booking> listBookingExisted = bookingRepository.getListBookingExist(listOrderNo);
        for (Booking booking : listBookingExisted) {
            for (CostDataFile costData : costDataList) {
                if (booking.getOrderNo().equals(costData.orderNo)) {
                    booking.setTotalCost(costData.totalCost);
                    booking = calculateMargin(booking);

                }
            }
        }
        bookingRepository.saveAll(listBookingExisted);
    }

    public List<ImportFailure> importCostDataNew(String filePath, String fileUUID) throws IOException, MissingSheetException, BlankSheetException, MissingColumnException {

        InputStream is = new FileInputStream(filePath);
        XSSFWorkbook workbook = new XSSFWorkbook(is);

        String sheetName = CheckRequiredColumnUtils.BOOKING_COST_DATA_REQUIRED_SHEET;
        Sheet orderSheet = workbook.getSheet(sheetName);

        if (orderSheet == null)
            throw new MissingSheetException(sheetName, fileUUID);

        if (orderSheet.getLastRowNum() <= 0)
            throw new BlankSheetException(sheetName, fileUUID);

        //get list costData
        List<CostDataFile> listCostData = getListCostDataFromSheet(orderSheet, fileUUID);

        // get list orderNo in list costData
        Set<String> setOrderNo = getListOrderNoFromListCostData(listCostData);

        // get list Booking by list OrderNo
        List<Booking> listBooking = bookingRepository.getListBookingByListOrderNo(setOrderNo);

        // calculate margin
        calculateMarginWithCostData(listBooking, listCostData);
        bookingRepository.saveAll(listBooking);

        return handleNotFindBookingByCostData(listCostData, listBooking, fileUUID);
    }

    private List<ImportFailure> handleNotFindBookingByCostData(List<CostDataFile> listCostData, List<Booking> listBooking, String fileUUID) {
        List<ImportFailure> listImportFailure = new ArrayList<>();

        costDataLoop:
        for (CostDataFile costData : listCostData) {
            for (Booking booking : listBooking) {
                if (costData.orderNo.equals(booking.getOrderNo()))
                    continue costDataLoop;
            }
            importFailureService.addIntoListImportFailure(listImportFailure, costData.orderNo, "not-find-booking-with-orderNo", costData.orderNo, ImportFailureType.WARNING);
        }
        importFailureService.setFileUUIDForListImportFailure(listImportFailure, fileUUID);
        importFailureRepository.saveAll(listImportFailure);
        localeUtils.logStatusImportComplete(listImportFailure, ModelUtil.COST_DATA);
        return listImportFailure;
    }


    private void calculateMarginWithCostData(List<Booking> listBooking, List<CostDataFile> listCostData) {
        for (Booking booking : listBooking) {
            for (CostDataFile costDataFile : listCostData) {
                if (booking.getOrderNo().equals(costDataFile.orderNo)) {
                    booking.setTotalCost(costDataFile.totalCost);
                    calculateMargin(booking);
                    break;
                }
            }
        }
    }

    private Set<String> getListOrderNoFromListCostData(List<CostDataFile> costDataList) {
        Set<String> orderNoSet = new HashSet<>();
        for (CostDataFile costData : costDataList) {
            orderNoSet.add(costData.orderNo);
        }
        return orderNoSet;
    }

    private List<CostDataFile> getListCostDataFromSheet(Sheet sheet, String fileUUID) throws MissingColumnException {
        List<CostDataFile> listCostData = new LinkedList<>();
        HashMap<String, Integer> ORDER_COLUMNS_NAME = new HashMap<>();
        for (Row row : sheet) {
            if (row.getRowNum() == 0) {
                getOrderColumnsName(row, ORDER_COLUMNS_NAME);
                CheckRequiredColumnUtils.checkRequiredColumn(new ArrayList<>(ORDER_COLUMNS_NAME.keySet()), CheckRequiredColumnUtils.BOOKING_COST_DATA_REQUIRED_COLUMN, fileUUID);
            } else if (!row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().isEmpty() && row.getRowNum() > 0) {
                // create CostDataFile
                CostDataFile costDataFile = new CostDataFile();

                // get orderNo
                Cell orderNOCell = row.getCell(ORDER_COLUMNS_NAME.get("Order"));
                costDataFile.orderNo = orderNOCell.getStringCellValue();

                // get TotalCost
                Cell totalCostCell = row.getCell(ORDER_COLUMNS_NAME.get("TOTAL MFG COST Going-To"));
                if (totalCostCell.getCellType() == CellType.NUMERIC) {
                    costDataFile.totalCost = totalCostCell.getNumericCellValue();
                } else if (totalCostCell.getCellType() == CellType.STRING) {
                    costDataFile.totalCost = Double.parseDouble(totalCostCell.getStringCellValue());
                }

                listCostData.add(costDataFile);
            }
        }
        return listCostData;
    }


    private static class CostDataFile {
        String orderNo;
        double totalCost;
    }

    private static class MarginDataFile {
        String orderNo;
        double marginPercentage;
    }


    public boolean checkOldData(String month, String year) {
        return Integer.parseInt(year) < 2023 | (Integer.parseInt(year) == 2023 && !(month.equals("Sep") | month.equals("Oct") | month.equals("Nov") | month.equals("Dec")));
    }

    public Booking importDNAndDNAfterSurcharge(Booking booking) {
        List<Part> parts = partRepository.getPartByOrderNumber(booking.getOrderNo());
        if (parts.isEmpty()) {
            log.info("Not found Part with OrderNo: " + booking.getOrderNo());
            return null;
        }

        double dealerNet = 0;
        for (Part part : parts) {
            dealerNet += part.getNetPriceEach();
        }
        double surcharge = 0;
        booking.setDealerNet(dealerNet);
        booking.setDealerNetAfterSurcharge(dealerNet - surcharge);
        booking.setCurrency(parts.get(0).getCurrency());
        booking.setQuoteNumber(parts.get(0).getQuoteId());
        return booking;
    }

    /**
     * for new data
     */
    private Booking calculateMargin(Booking booking) {
        //need : DNAfterSurcharge, totalCost
        double dealerNetAfterSurcharge = booking.getDealerNetAfterSurcharge();
        double totalCost = booking.getTotalCost();
        double marginAfterSurcharge = dealerNetAfterSurcharge - totalCost;
        double marginPercentageAfterSurcharge = marginAfterSurcharge / dealerNetAfterSurcharge;
        booking.setMarginAfterSurcharge(marginAfterSurcharge);
        booking.setMarginPercentageAfterSurcharge(marginPercentageAfterSurcharge);
        return booking;
    }

    /**
     * old data
     */
    private Booking calculateTotalCostAndMarginAfterSurcharge(Booking booking) {
        // need : Margin% , DNAfterSurcharge
        double dealerNetAfterSurcharge = booking.getDealerNetAfterSurcharge();
        double marginPercentageAfterSurcharge = booking.getMarginPercentageAfterSurcharge();
        double marginAfterSurcharge = dealerNetAfterSurcharge * marginPercentageAfterSurcharge;
        double totalCost = dealerNetAfterSurcharge - marginAfterSurcharge;
        booking.setTotalCost(totalCost);
        booking.setMarginAfterSurcharge(marginAfterSurcharge);
        return booking;
    }

    public Optional<Booking> getBookingOrderByOrderNumber(String orderNumber) {
        return bookingRepository.findById(orderNumber);
    }

    public List<Booking> getListBookingByFilter(FilterModel filterModel) throws ParseException {
        //Get FilterData
        Map<String, Object> filterMap = ConvertDataFilterUtil.loadDataFilterIntoMap(filterModel);

        return bookingRepository.selectAllForBookingOrder(
                (String) filterMap.get("orderNoFilter"), (List<String>) filterMap.get("regionFilter"), (List<String>) filterMap.get("plantFilter"),
                (List<String>) filterMap.get("metaSeriesFilter"), (List<String>) filterMap.get("classFilter"), (List<String>) filterMap.get("modelFilter"),
                (List<String>) filterMap.get("segmentFilter"), (List<String>) filterMap.get("dealerNameFilter"), (String) filterMap.get("aopMarginPercentageFilter"),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((String) ((List) filterMap.get("marginPercentageFilter")).get(0)),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((Double) ((List) filterMap.get("marginPercentageFilter")).get(1)),
                (LocalDate) filterMap.get("fromDateFilter"), (LocalDate) filterMap.get("toDateFilter"), (Pageable) filterMap.get("pageable"));
    }

    public int countBookingsWithFilter(FilterModel filterModel) throws ParseException {
        //Get FilterData
        Map<String, Object> filterMap = ConvertDataFilterUtil.loadDataFilterIntoMap(filterModel);

        return bookingRepository.getCount(
                (String) filterMap.get("orderNoFilter"), (List<String>) filterMap.get("regionFilter"), (List<String>) filterMap.get("plantFilter"),
                (List<String>) filterMap.get("metaSeriesFilter"), (List<String>) filterMap.get("classFilter"), (List<String>) filterMap.get("modelFilter"),
                (List<String>) filterMap.get("segmentFilter"), (List<String>) filterMap.get("dealerNameFilter"), (String) filterMap.get("aopMarginPercentageFilter"),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((String) ((List) filterMap.get("marginPercentageFilter")).get(0)),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((Double) ((List) filterMap.get("marginPercentageFilter")).get(1)),
                (LocalDate) filterMap.get("fromDateFilter"), (LocalDate) filterMap.get("toDateFilter"));
    }

    public List<String> getListOrderNoFromListBooking(List<Booking> bookings) {
        List<String> orderNos = new ArrayList<>();
        bookings.forEach(booking -> orderNos.add(booking.getOrderNo()));
        return orderNos;
    }

    public Map<String, Object> getBookingByFilter(FilterModel filterModel) throws ParseException {
        Map<String, Object> result = new HashMap<>();
        //Get FilterData
        Map<String, Object> filterMap = ConvertDataFilterUtil.loadDataFilterIntoMap(filterModel);

        List<Booking> bookingList = bookingRepository.selectAllForBookingOrder(
                (String) filterMap.get("orderNoFilter"), (List<String>) filterMap.get("regionFilter"), (List<String>) filterMap.get("plantFilter"),
                (List<String>) filterMap.get("metaSeriesFilter"), (List<String>) filterMap.get("classFilter"), (List<String>) filterMap.get("modelFilter"),
                (List<String>) filterMap.get("segmentFilter"), (List<String>) filterMap.get("dealerNameFilter"), (String) filterMap.get("aopMarginPercentageFilter"),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((String) ((List) filterMap.get("marginPercentageFilter")).get(0)),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((Double) ((List) filterMap.get("marginPercentageFilter")).get(1)),
                (LocalDate) filterMap.get("fromDateFilter"), (LocalDate) filterMap.get("toDateFilter"), (Pageable) filterMap.get("pageable")
        );

        // get currency for order -> get exchange_rate
        List<String> listCurrency = new ArrayList<>();
        List<ExchangeRate> exchangeRateList = new ArrayList<>();
        List<String> listTargetCurrency = TargetCurrency.getListTargetCurrency;

        listCurrency.add("USD");
        listCurrency.add("AUD");

        exchangeRateList.add(exchangeRateService.getNearestExchangeRate("USD", "AUD"));
        exchangeRateList.add(exchangeRateService.getNearestExchangeRate("AUD", "USD"));

        for (Booking booking : bookingList) {
            if (booking.getCurrency() != null) {
                String currency = booking.getCurrency().getCurrency();
                if (!listCurrency.contains(currency)) { // get distinct currency in list order
                    listCurrency.add(currency);
                    for (String targetCurrency : listTargetCurrency) {
                        if (!targetCurrency.equals(currency)) { // get exchange_rate FROM current currency of order TO targetCurrency
                            exchangeRateList.add(exchangeRateService.getNearestExchangeRate(currency, targetCurrency));
                        }
                    }
                }
            }
        }


        result.put("listExchangeRate", exchangeRateList);
        result.put("listBookingOrder", bookingList);

        // get data for totalRow
        List<Booking> getTotalBookings = bookingRepository.getTotal(
                (String) filterMap.get("orderNoFilter"), (List<String>) filterMap.get("regionFilter"), (List<String>) filterMap.get("plantFilter"),
                (List<String>) filterMap.get("metaSeriesFilter"), (List<String>) filterMap.get("classFilter"), (List<String>) filterMap.get("modelFilter"),
                (List<String>) filterMap.get("segmentFilter"), (List<String>) filterMap.get("dealerNameFilter"), (String) filterMap.get("aopMarginPercentageFilter"),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((String) ((List) filterMap.get("marginPercentageFilter")).get(0)),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((Double) ((List) filterMap.get("marginPercentageFilter")).get(1)),
                (LocalDate) filterMap.get("fromDateFilter"), (LocalDate) filterMap.get("toDateFilter")
        );

        Booking totalBooking = calculateTotal(getTotalBookings, exchangeRateList);

        result.put("totalItems", getTotalBookings.size());
        result.put("total", List.of(totalBooking));

        // get latest updated time
        Optional<LocalDateTime> latestUpdatedTimeOptional = bookingRepository.getLatestUpdatedTime();
        String latestUpdatedTime = null;
        if (latestUpdatedTimeOptional.isPresent()) {
            latestUpdatedTime = DateUtils.convertLocalDateTimeToString(latestUpdatedTimeOptional.get());
        }

        result.put("latestUpdatedTime", latestUpdatedTime);
        result.put("serverTimeZone", TimeZone.getDefault().getID());
        return result;
    }

    private Booking calculateTotal(List<Booking> bookings, List<ExchangeRate> exchangeRates) {
        double dealerNet = 0;
        double dealerNetAfterSurcharge = 0;

        double totalCost = 0;
        double marginAfterSurcharge = 0;
        double marginPercentageAfterSurcharge;

        for (Booking booking : bookings) {
            if (booking.getCurrency() != null) {
                if (!booking.getCurrency().getCurrency().equals("USD")) {
                    // checking exchange( currency of Booking -> 'USD') in exchangeRates
                    if (!isExistedExchangeRateInList(exchangeRates, createExchangeRateOfBookingToUSD(booking))) {
                        // get ExchangeRate from DB
                        ExchangeRate exchangeRate = exchangeRateService.getNearestExchangeRate(booking.getCurrency().getCurrency(), "USD");
                        exchangeRates.add(exchangeRate);

                        dealerNet += booking.getDealerNet() * exchangeRate.getRate();
                        dealerNetAfterSurcharge += booking.getDealerNetAfterSurcharge() * exchangeRate.getRate();
                        totalCost += booking.getTotalCost() * exchangeRate.getRate();
                    } else {
                        ExchangeRate exchangeRate = getExchangeRateFromList(exchangeRates, booking.getCurrency().getCurrency(), "USD");

                        dealerNet += booking.getDealerNet() * exchangeRate.getRate();
                        dealerNetAfterSurcharge += booking.getDealerNetAfterSurcharge() * exchangeRate.getRate();
                        totalCost += booking.getTotalCost() * exchangeRate.getRate();
                    }
                } else {
                    dealerNet += booking.getDealerNet();
                    dealerNetAfterSurcharge += booking.getDealerNetAfterSurcharge();
                    totalCost += booking.getTotalCost();
                }
            }
        }

        marginAfterSurcharge = dealerNetAfterSurcharge - totalCost;
        marginPercentageAfterSurcharge = marginAfterSurcharge / dealerNetAfterSurcharge;

        return new Booking("Total", new Currency("USD"), bookings.size(), dealerNet, dealerNetAfterSurcharge, totalCost, marginAfterSurcharge, marginPercentageAfterSurcharge);
    }

    private ExchangeRate getExchangeRateFromList(List<ExchangeRate> exchangeRates, String fromCurrency, String toCurrency) {
        for (ExchangeRate exchangeRate : exchangeRates) {
            if (exchangeRate.getFrom().getCurrency().equals(fromCurrency) && exchangeRate.getTo().getCurrency().equals(toCurrency))
                return exchangeRate;
        }
        return null;
    }

    private ExchangeRate createExchangeRateOfBookingToUSD(Booking booking) {
        return new ExchangeRate(booking.getCurrency(), new Currency("USD"));
    }

    private boolean isExistedExchangeRateInList(List<ExchangeRate> exchangeRateList, ExchangeRate exchangeRate) {
        for (ExchangeRate ex : exchangeRateList) {
            if (ex.getFrom().equals(exchangeRate.getFrom()) && ex.getTo().equals(exchangeRate.getTo()))
                return true;
        }
        return false;
    }

    public Booking getBookingByOrderNo(List<Booking> bookings, String orderNo) {
        for (Booking booking : bookings) {
            if (booking.getOrderNo().equals(orderNo)) {
                return booking;
            }
        }
        return null;
    }

    public void convertCurrencyOfBookingToUSD(List<Booking> bookings, List<ExchangeRate> exchangeRates) {
        for (Booking booking : bookings) {
            if (booking.getCurrency().getCurrency().equals("USD"))
                continue;

            for (ExchangeRate exchangeRate : exchangeRates) {
                if (booking.getCurrency().equals(exchangeRate.getFrom())
                        && booking.getDate().getYear() == (exchangeRate.getDate().getYear())
                        && booking.getDate().getMonth().equals(exchangeRate.getDate().getMonth())) {
                    booking.setDealerNet(booking.getDealerNet() * exchangeRate.getRate());
                    booking.setDealerNetAfterSurcharge(booking.getDealerNetAfterSurcharge() * exchangeRate.getRate());
                    booking.setTotalCost(booking.getTotalCost() * exchangeRate.getRate());
                    booking.setMarginAfterSurcharge(booking.getMarginAfterSurcharge() * exchangeRate.getRate());
                    booking.setMarginPercentageAfterSurcharge(booking.getMarginPercentageAfterSurcharge() * exchangeRate.getRate());
                }
            }
        }
    }

}
