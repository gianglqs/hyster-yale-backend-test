package com.hysteryale.service;

import com.hysteryale.exception.BlankSheetException;
import com.hysteryale.exception.MissingColumnException;
import com.hysteryale.exception.MissingSheetException;
import com.hysteryale.model.Currency;
import com.hysteryale.model.*;
import com.hysteryale.model.competitor.CompetitorColor;
import com.hysteryale.model.competitor.CompetitorPricing;
import com.hysteryale.model.competitor.ForeCastValue;
import com.hysteryale.repository.*;
import com.hysteryale.utils.CheckRequiredColumnUtils;
import com.hysteryale.utils.EnvironmentUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class ImportService extends BasedService {

    @Resource
    CompetitorPricingRepository competitorPricingRepository;
    @Resource
    RegionService regionService;
    @Resource
    PartService partService;

    @Resource
    ShipmentRepository shipmentRepository;

    @Resource
    IndicatorService indicatorService;

    @Resource
    CountryService countryService;

    @Resource
    ProductService productService;

    @Resource
    ProductRepository productRepository;

    @Resource
    DealerRepository dealerRepository;
    @Resource
    ClazzRepository clazzRepository;

    @Resource
    AOPMarginRepository aopMarginRepository;

    @Resource
    CurrencyRepository currencyRepository;

    @Resource
    BookingService bookingService;

    @Resource
    BookingRepository bookingRepository;

    @Resource
    DealerService dealerService;

    @Resource
    AOPMarginService aopMarginService;

    @Resource
    CountryRepository countryRepository;

    public void getOrderColumnsName(Row row, HashMap<String, Integer> ORDER_COLUMNS_NAME) {
        for (int i = 0; i < 50; i++) {
            if (row.getCell(i) != null) {
                String columnName = "";

                if (row.getCell(i).getCellType() == CellType.STRING) {
                    columnName = row.getCell(i).getStringCellValue().trim();
                } else {
                    columnName = String.valueOf(row.getCell(i).getNumericCellValue());

                }

                if (ORDER_COLUMNS_NAME.containsKey(columnName))
                    continue;
                ORDER_COLUMNS_NAME.put(columnName, i);
            }
        }
    }

    public List<String> getAllFilesInFolder(String folderPath, int state) {
        Pattern pattern;

        switch (state) {
            case 1:
                pattern = Pattern.compile(".*Final.*(.xlsx)$");
                break;
            case 2:
                pattern = Pattern.compile("^01.*(.xlsx)$");
                break;
            case 3:
                pattern = Pattern.compile("^Competitor.*(.xlsx)$");
                break;
            case 4:
                pattern = Pattern.compile("^SAP.*(.xlsx)$");
                break;
            default:
                pattern = Pattern.compile(".*(.xlsx)$");
                break;
        }

        List<String> fileList = new ArrayList<>();
        Matcher matcher;
        try {
            DirectoryStream<Path> folder = Files.newDirectoryStream(Paths.get(folderPath));
            for (Path path : folder) {
                matcher = pattern.matcher(path.getFileName().toString());
                if (matcher.matches())
                    fileList.add(path.getFileName().toString());
                else
                    logError("Wrong formatted file's name: " + path.getFileName().toString());
            }
        } catch (Exception e) {
            logInfo(e.getMessage());

        }
        return fileList;
    }

    public List<CompetitorPricing> mapExcelDataIntoCompetitorObject(Row row, HashMap<String, Integer> ORDER_COLUMNS_NAME) {
        List<CompetitorPricing> competitorPricingList = new ArrayList<>();

        Cell cellRegion = row.getCell(ORDER_COLUMNS_NAME.get("Region"));

        Cell cellCompetitorName = row.getCell(ORDER_COLUMNS_NAME.get("Brand"));
        String competitorName = cellCompetitorName.getStringCellValue().strip();

        boolean isChineseBrand = competitorName.contains("Heli") || competitorName.contains("HeLi") || competitorName.contains("Hangcha") || competitorName.contains("Hang Cha");
        Cell cellClass = row.getCell(ORDER_COLUMNS_NAME.get("Class"));
        String strClazz = cellClass.getStringCellValue();
        Optional<Clazz> optionalClazz = clazzRepository.getClazzByClazzName(strClazz.equals("Class 5 non BT") ? "Class 5 NOT BT" : strClazz);
        if (optionalClazz.isEmpty())
            return competitorPricingList;
        Clazz clazz = optionalClazz.get();

        Double leadTime = null;
        Cell cellLeadTime = row.getCell(ORDER_COLUMNS_NAME.get("Lead Time"));
        if (cellLeadTime != null && cellLeadTime.getCellType() == CellType.NUMERIC) {
            leadTime = cellLeadTime.getNumericCellValue();
        }

        double competitorPricing = row.getCell(ORDER_COLUMNS_NAME.get("Price (USD)")).getNumericCellValue();
        double marketShare = row.getCell(ORDER_COLUMNS_NAME.get("Normalized Market Share")).getNumericCellValue();

        // 2 fields below are hard-coded, will be modified later
        double percentageDealerPremium = 0.1;
        double dealerNet = 10000;

        String category = row.getCell(ORDER_COLUMNS_NAME.get("Category")).getStringCellValue();

        String strCountry = row.getCell(ORDER_COLUMNS_NAME.get("Country"), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
        String strRegion = cellRegion.getStringCellValue();
        Country country = getCountry(strCountry, strRegion);

        String group = row.getCell(ORDER_COLUMNS_NAME.get("Group"), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
        CompetitorColor competitorColor = indicatorService.getCompetitorColor(group);
        String model = row.getCell(ORDER_COLUMNS_NAME.get("Model"), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();


        // assigning values for CompetitorPricing
        CompetitorPricing cp = new CompetitorPricing();
        cp.setCompetitorName(competitorName);
        cp.setCategory(category);

        cp.setCountry(country);
        cp.setClazz(clazz);
        cp.setCompetitorLeadTime(leadTime);
        cp.setDealerNet(dealerNet);
        cp.setChineseBrand(isChineseBrand);

        cp.setCompetitorPricing(competitorPricing);
        cp.setDealerPremiumPercentage(percentageDealerPremium);
        cp.setSeries("");
        cp.setMarketShare(marketShare);
        cp.setColor(competitorColor);
        cp.setModel(model);

        // separate seriesString (for instances: A3C4/A7S4)
        String seriesString;
        Cell cellSeries = row.getCell(ORDER_COLUMNS_NAME.get("HYG Series"));
        if (cellSeries != null && cellSeries.getCellType() == CellType.STRING) {
            seriesString = cellSeries.getStringCellValue();
            StringTokenizer stk = new StringTokenizer(seriesString, "/");
            while (stk.hasMoreTokens()) {
                String series = stk.nextToken();
                CompetitorPricing cp1 = new CompetitorPricing();
                cp1.setCompetitorName(competitorName);
                cp1.setCategory(category);
                cp1.setCountry(country);
                cp1.setClazz(clazz);
                cp1.setCompetitorLeadTime(leadTime);
                cp1.setDealerNet(partService.getAverageDealerNet(strRegion, clazz.getClazzName(), series));
                cp1.setChineseBrand(isChineseBrand);

                cp1.setCompetitorPricing(competitorPricing);
                cp1.setDealerPremiumPercentage(percentageDealerPremium);
                cp1.setSeries(series);
                cp1.setMarketShare(marketShare);
//                cp1.setModel(productDimensionService.getModelFromMetaSeries(series.substring(1)));
                cp1.setModel(model);
                cp1.setColor(competitorColor);

                competitorPricingList.add(cp1);
            }
        } else
            competitorPricingList.add(cp);
        return competitorPricingList;
    }

    /**
     * Get Country and create new Country if not existed
     */
    public Country getCountry(String countryName, String strRegion) {
        Optional<Country> optional = countryService.getCountryByName(countryName);
        if (optional.isPresent())
            return optional.get();
        else {
            Region region = regionService.getRegionByName(strRegion);
            return countryService.addCountry(new Country(countryName, region));
        }
    }

    public void importCompetitorPricing() throws IOException {

        String baseFolder = EnvironmentUtils.getEnvironmentValue("import-files.base-folder");
        String folderPath = baseFolder + EnvironmentUtils.getEnvironmentValue("import-files.competitor-pricing");

        // Get files in Folder Path
        List<String> fileList = getAllFilesInFolder(folderPath, 3);
        List<ForeCastValue> foreCastValues = loadForecastForCompetitorPricingFromFile();

        for (String fileName : fileList) {
            String pathFile = folderPath + "/" + fileName;

            InputStream is = new FileInputStream(pathFile);
            XSSFWorkbook workbook = new XSSFWorkbook(is);
            HashMap<String, Integer> COMPETITOR_COLUMNS_NAME = new HashMap<>();
            Sheet competitorSheet = workbook.getSheet("Competitor Pricing Database");

            List<CompetitorPricing> competitorPricingList = new ArrayList<>();

            for (Row row : competitorSheet) {
                if (row.getRowNum() == 0) getOrderColumnsName(row, COMPETITOR_COLUMNS_NAME);
                else if (!row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().isEmpty() && row.getRowNum() > 0) {
                    List<CompetitorPricing> competitorPricings = mapExcelDataIntoCompetitorObject(row, COMPETITOR_COLUMNS_NAME);
                    for (CompetitorPricing competitorPricing : competitorPricings) {
                        // if it has series -> assign ForeCastValue
                        if (!competitorPricing.getSeries().isEmpty()) {
                            String strRegion = competitorPricing.getCountry().getRegion().getRegionName();
                            String metaSeries = competitorPricing.getSeries().substring(1); // extract metaSeries from series

                            int currentYear = LocalDate.now().getYear();

                            ForeCastValue actualForeCast = findForeCastValue(foreCastValues, strRegion, metaSeries, currentYear - 1);
                            ForeCastValue AOPFForeCast = findForeCastValue(foreCastValues, strRegion, metaSeries, currentYear);
                            ForeCastValue LRFFForeCast = findForeCastValue(foreCastValues, strRegion, metaSeries, currentYear + 1);

                            competitorPricing.setActual(actualForeCast == null ? 0 : actualForeCast.getQuantity());
                            competitorPricing.setAOPF(AOPFForeCast == null ? 0 : AOPFForeCast.getQuantity());
                            competitorPricing.setLRFF(LRFFForeCast == null ? 0 : LRFFForeCast.getQuantity());
                            competitorPricing.setPlant(LRFFForeCast == null ? "" : LRFFForeCast.getPlant());

                        }
                        competitorPricingList.add(competitorPricing);
                    }
                }
            }
            competitorPricingRepository.saveAll(competitorPricingList);
            assigningCompetitorValues();
        }
    }

    /**
     * Find a forecast value by Region and Series, year is an option if year is empty then we get all years
     */
    public ForeCastValue findForeCastValue(List<ForeCastValue> foreCastValues, String strRegion, String metaSeries, int year) {
        for (ForeCastValue foreCastValue : foreCastValues) {
            if (foreCastValue.getRegion().getRegionName().equals(strRegion) && foreCastValue.getMetaSeries().equals(metaSeries) && foreCastValue.getYear() == year)
                return foreCastValue;
        }
        return null;
    }

    public List<ForeCastValue> loadForecastForCompetitorPricingFromFile() throws IOException {

        String baseFolder = EnvironmentUtils.getEnvironmentValue("public-folder");
        String baseFolderUploaded = EnvironmentUtils.getEnvironmentValue("upload_files.base-folder");
        String folderPath = baseFolder + baseFolderUploaded + EnvironmentUtils.getEnvironmentValue("upload_files.forecast_pricing");
        List<String> fileList = getAllFilesInFolder(folderPath, -1);
        if (fileList.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Missing Forecast Dynamic Pricing Excel file");


        List<ForeCastValue> foreCastValues = new ArrayList<>();

        for (String fileName : fileList) {
            String pathFile = folderPath + "/" + fileName;

            InputStream is = new FileInputStream(pathFile);
            XSSFWorkbook workbook = new XSSFWorkbook(is);


            List<Integer> years = new ArrayList<>();
            HashMap<Integer, Integer> YEARS_COLUMN = new HashMap<>();
            HashMap<String, Integer> FORECAST_ORDER_COLUMN = new HashMap<>();

            for (Sheet sheet : workbook) {
                Region region = getRegionBySheetName(sheet.getSheetName());
                List<String> titleColumnFileCompetitor = List.of("Series /Segments", "Description", "Plant", "Brand", "Planform", "Qty", "DN", "M % ", "Book Rev", "Book Margin $");
                for (Row row : sheet) {
                    if (row.getRowNum() == 0) {
                        getYearsInForeCast(YEARS_COLUMN, row, years);

                    } else if (row.getRowNum() == 1)
                        getOrderColumnsName(row, FORECAST_ORDER_COLUMN);
                    else if (!row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().isEmpty() &&       // checking null
                            row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().length() == 3 &&    // checking cell is whether metaSeries or not
                            row.getRowNum() > 1) {

                        // get all quantity value from 2021 to 2027
                        for (int year : years) {
                            String metaSeries = row.getCell(FORECAST_ORDER_COLUMN.get("Series /Segments")).getStringCellValue();
                            String plant = row.getCell(FORECAST_ORDER_COLUMN.get("Plant"), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
                            int quantity = (int) row.getCell(YEARS_COLUMN.get(year)).getNumericCellValue();
                            // setting values
                            ForeCastValue foreCastValue = new ForeCastValue(region, year, metaSeries, quantity, plant);
                            foreCastValues.add(foreCastValue);
                        }
                    }
                }


            }
        }
        log.info("Number of ForeCastValue: " + foreCastValues.size());
        return foreCastValues;
    }

    private void getYearsInForeCast(HashMap<Integer, Integer> YEARS_COLUMN, Row row, List<Integer> years) {
        for (Cell cell : row) {
            if (cell.getCellType() == CellType.NUMERIC) {
                int year = (int) cell.getNumericCellValue();
                if (YEARS_COLUMN.get(year) == null) {
                    YEARS_COLUMN.put(year, cell.getColumnIndex());
                    years.add(year);
                }
            }
        }
    }

    /**
     * Find the region based on sheetName in Forecast Value
     */
    private Region getRegionBySheetName(String sheetName) {
        String strRegion;
        switch (sheetName) {
            case "Asia_Fin":
                strRegion = "Asia";
                break;
            case "Pac_Fin":
                strRegion = "Pacific";
                break;
            default:
                strRegion = "India";
        }
        return regionService.getRegionByName(strRegion);
    }

    /**
     * Get CompetitorGroup based on country, class, category, series --> for assigning HYGLeadTime, DealerStreetPricing and calculating Variance %
     */
    private List<String[]> getCompetitorGroup() {
        return competitorPricingRepository.getCompetitorGroup();
    }

    /**
     * Using {country, clazz, category, series} to specify a group of CompetitorPricing -> then can use for calculating values later
     */
    private List<CompetitorPricing> getListOfCompetitorInGroup(String country, String clazz, String category, String series) {
        return competitorPricingRepository.getListOfCompetitorInGroup(country, clazz, category, series);
    }

    /**
     * Assign hygLeadTime, averageDealerNet, dealerStreetPremium and calculating variancePercentage for CompetitorPricing
     * after save based data into DB
     */
    @Transactional
    public void assigningCompetitorValues() {
        List<String[]> competitorGroups = getCompetitorGroup();
        for (String[] competitorGroup : competitorGroups) {
            String country = competitorGroup[0];
            String clazz = competitorGroup[1];
            String category = competitorGroup[2];
            String series = competitorGroup[3];

            List<CompetitorPricing> competitorPricingList = getListOfCompetitorInGroup(country, clazz, category, series);
            double hygLeadTime = 0;
            double totalDealerNet = 0;
            double dealerStreetPricing = 0;
            for (CompetitorPricing cp : competitorPricingList) {

                // Find HYG Brand to assign hygLeadTime and dealerStreetPricing for other brand in a group {country, class, category, series}
                String competitorName = cp.getCompetitorName();
                if (competitorName.contains("HYG") || competitorName.contains("Hyster") || competitorName.contains("Yale") || competitorName.contains("HYM")) {
                    hygLeadTime = cp.getCompetitorLeadTime();
                    dealerStreetPricing = cp.getCompetitorPricing();
                }
                totalDealerNet += cp.getDealerNet();
            }
            double averageDealerNet = totalDealerNet / competitorPricingList.size();

            // Assigning hygLeadTime, averageDealerNet, dealerStreetPremium
            for (CompetitorPricing cp : competitorPricingList) {
                cp.setHYGLeadTime(hygLeadTime);
                cp.setDealerStreetPricing(dealerStreetPricing);
                cp.setAverageDN(averageDealerNet);
                double handlingCost = dealerStreetPricing - cp.getDealerNet() * (1 + cp.getDealerPremiumPercentage());
                double dealerPricingPremium = dealerStreetPricing - (cp.getDealerNet() + handlingCost);
                double dealerPricingPremiumPercentage = dealerPricingPremium / dealerStreetPricing;
                cp.setDealerHandlingCost(handlingCost);
                cp.setDealerPricingPremium(dealerPricingPremium);
                cp.setDealerPricingPremiumPercentage(dealerPricingPremiumPercentage);

                // calculate Variance % = competitorPricing - (dealerStreetPricing + dealerPricingPremium)
                double variancePercentage = (cp.getCompetitorPricing() - (cp.getDealerStreetPricing() + cp.getDealerPricingPremium())) / cp.getCompetitorPricing();
                cp.setVariancePercentage(variancePercentage);
            }
            competitorPricingRepository.saveAll(competitorPricingList);
        }
    }


    public void importShipmentFileOneByOne(InputStream is, String savedFileName) throws IOException, MissingColumnException, MissingSheetException, BlankSheetException {
        XSSFWorkbook workbook = new XSSFWorkbook(is);
        HashMap<String, Integer> SHIPMENT_COLUMNS_NAME = new HashMap<>();
        String sheetName = CheckRequiredColumnUtils.SHIPMENT_REQUIRED_SHEET;
        XSSFSheet shipmentSheet = workbook.getSheet(sheetName);
        if (shipmentSheet == null)
            throw new MissingSheetException(sheetName, savedFileName);

        if (shipmentSheet.getLastRowNum() <= 0)
            throw new BlankSheetException(sheetName, savedFileName);

        logInfo("import shipment");
        List<Shipment> shipmentList = new ArrayList<>();

        //prepare data for import
        List<Product> prepareProducts = productRepository.findAll();
        List<Dealer> prepareDealers = dealerRepository.findAll();
        List<AOPMargin> prepareAOPMargin = aopMarginRepository.findAll();
        Currency prepareCurrency = currencyRepository.findByCurrency("USD");
        List<Booking> prepareBookings = bookingRepository.findAll();
        List<Country> prepareCountries = countryRepository.findAll();


        for (Row row : shipmentSheet) {
            if (row.getRowNum() == 0) {
                getOrderColumnsName(row, SHIPMENT_COLUMNS_NAME);
                CheckRequiredColumnUtils.checkRequiredColumn(new ArrayList<>(SHIPMENT_COLUMNS_NAME.keySet()), CheckRequiredColumnUtils.SHIPMENT_REQUIRED_COLUMN, savedFileName);
            } else if (!row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().isEmpty() && row.getRowNum() > 0) {
                Shipment newShipment = mapExcelDataIntoShipmentObject(
                        row, SHIPMENT_COLUMNS_NAME, prepareProducts, prepareAOPMargin, prepareDealers, prepareCurrency, prepareBookings, prepareCountries);

                // check it has BookingOrder
                if (newShipment == null)
                    continue;

                shipmentList.add(newShipment);

            }
        }

        List<Shipment> shipmentListAfterCalculate = new ArrayList<>();

        for (Shipment shipment : shipmentList) {
            // check orderNo is existed in shipmentListAfterCalculate\
            Shipment s = checkExistOrderNo(shipmentListAfterCalculate, shipment.getOrderNo());
            if (s != null) {
                updateShipment(s, shipment);
            } else {
                // add into shipmentListAfterCalculate to save new
                shipmentListAfterCalculate.add(shipment);
            }
        }

        shipmentRepository.saveAll(shipmentListAfterCalculate);

        logInfo("import shipment successfully");
    }


    private Shipment checkExistOrderNo(List<Shipment> list, String orderNo) {
        for (Shipment s : list) {
            if (s.getOrderNo().equals(orderNo))
                return s;
        }
        return null;
    }

    public void importShipment() throws IOException, MissingColumnException, MissingSheetException, BlankSheetException {
        String baseFolder = EnvironmentUtils.getEnvironmentValue("import-files.base-folder");
        String folderPath = baseFolder + EnvironmentUtils.getEnvironmentValue("import-files.shipment");

        // Get files in Folder Path
        List<String> fileList = getAllFilesInFolder(folderPath, 4);
        for (String fileName : fileList) {
            String pathFile = folderPath + "/" + fileName;
            //check file has been imported ?
            if (isImported(pathFile)) {
                logWarning("file '" + fileName + "' has been imported");
                continue;
            }
            logInfo("{ Start importing file: '" + fileName + "'");

            InputStream is = new FileInputStream(pathFile);

            importShipmentFileOneByOne(is, "");// TODO: need check

            updateStateImportFile(pathFile);
        }
    }


    /**
     * reset revenue, totalCost, Margin$, Margin%
     */
    private Shipment updateShipment(Shipment s1, Shipment s2) {
        s1.setDealerNet(s1.getDealerNet() + s2.getDealerNet());
        s1.setDealerNetAfterSurcharge(s1.getDealerNetAfterSurcharge() + s2.getDealerNetAfterSurcharge());
        s1.setNetRevenue(s1.getNetRevenue() + s2.getNetRevenue());
        s1.setTotalCost(s1.getTotalCost() + s2.getTotalCost());
        Double margin = s1.getDealerNetAfterSurcharge() - s1.getTotalCost();
        Double marginPercentage = margin / s1.getDealerNetAfterSurcharge();
        s1.setMarginAfterSurcharge(margin);
        s1.setMarginPercentageAfterSurcharge(marginPercentage);
        s1.setQuantity(s1.getQuantity() + s2.getQuantity());
        return s1;
    }


    private Shipment mapExcelDataIntoShipmentObject(Row row, HashMap<String, Integer> shipmentColumnsName,
                                                    List<Product> prepareProducts, List<AOPMargin> prepareAOPMargins,
                                                    List<Dealer> prepareDealers, Currency USDCurrency,
                                                    List<Booking> prepareBookings, List<Country> prepareCountries) {
        Shipment shipment = new Shipment();

        // Set orderNo
        String orderNo = row.getCell(shipmentColumnsName.get("Order number")).getStringCellValue();
        if (orderNo.isEmpty())
            return null;
        shipment.setOrderNo(orderNo);

        // series
        String series = row.getCell(shipmentColumnsName.get("Series")).getStringCellValue();
        shipment.setSeries(series);

        //modelCode
        String modelCode = row.getCell(shipmentColumnsName.get("Model")).getStringCellValue();

        //product
        Product product = productService.findProductByModelCodeAndSeries(prepareProducts, modelCode, series);
        if (product == null)
            return null;
        shipment.setProduct(product);

        // Set serialNUmber
        String serialNumber = row.getCell(shipmentColumnsName.get("Serial Number")).getStringCellValue();
        shipment.setSerialNumber(serialNumber);

        //quantity
        int quantity = (int) row.getCell(shipmentColumnsName.get("Quantity")).getNumericCellValue();
        shipment.setQuantity(quantity);

        // dealerNet = 'Revenue' + 'Revenue - Other'
        double revenue = row.getCell(shipmentColumnsName.get("Revenue")).getNumericCellValue();
        double revenueOther = row.getCell(shipmentColumnsName.get("Revenue - Other")).getNumericCellValue();
        double dealerNet = revenue + revenueOther;
        shipment.setDealerNet(dealerNet);

        // surcharge
        double discounts = row.getCell(shipmentColumnsName.get("Discounts")).getNumericCellValue();
        double additionalDiscounts = row.getCell(shipmentColumnsName.get("Additional Discounts")).getNumericCellValue();
        double cashDiscounts = row.getCell(shipmentColumnsName.get("Cash Discounts")).getNumericCellValue();
        double surcharge = discounts + additionalDiscounts + cashDiscounts;

        //dealerNetAfterSurcharge
        double dealerNetAfterSurcharge = dealerNet - surcharge;
        shipment.setDealerNetAfterSurcharge(dealerNetAfterSurcharge);

        // totalCost
        double costOfSales = row.getCell(shipmentColumnsName.get("Cost of Sales")).getNumericCellValue();
        double dealerCommisions = row.getCell(shipmentColumnsName.get("Dealer Commisions")).getNumericCellValue();
        double warranty = row.getCell(shipmentColumnsName.get("Warranty")).getNumericCellValue();
        double COSOther = row.getCell(shipmentColumnsName.get("COS - Other")).getNumericCellValue();
        double totalCost = costOfSales + dealerCommisions + warranty + COSOther;
        shipment.setTotalCost(totalCost);

        // MarginAfterSurcharge = dealerNetAfterSurcharge - totalCost
        double marginAfterSurcharge = dealerNetAfterSurcharge - totalCost;
        shipment.setMarginAfterSurcharge(marginAfterSurcharge);

        // NetRevenue
        shipment.setNetRevenue(marginAfterSurcharge);

        // MarginPercentageAfterSurcharge
        double marginPercentageAfterSurcharge = marginAfterSurcharge / dealerNetAfterSurcharge;
        shipment.setMarginPercentageAfterSurcharge(marginPercentageAfterSurcharge);

        // date
        Date date = row.getCell(shipmentColumnsName.get("Created On")).getDateCellValue();
        LocalDate calendar = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        shipment.setDate(calendar);

        // booking
        Booking booking = bookingService.getBookingByOrderNo(prepareBookings, orderNo);
        if (booking != null) {
            shipment.setBookingMarginPercentageAfterSurcharge(booking.getMarginPercentageAfterSurcharge());
            shipment.setBookingMarginAfterSurcharge(booking.getMarginAfterSurcharge());
            shipment.setBookingDealerNetAfterSurcharge(booking.getDealerNetAfterSurcharge());

            // quote number
            shipment.setQuoteNumber(booking.getQuoteNumber());
        }

        //Dealer
        String dealerName = row.getCell(shipmentColumnsName.get("End Customer Name")).getStringCellValue();
        Dealer dealer = dealerService.getDealerByName(prepareDealers, dealerName);
        if (dealer == null) {
            log.error("Not found Dealer with dealerName: " + dealerName);
            return null;
        }
        shipment.setDealer(dealer);

        // region
        String ctryCode = row.getCell(shipmentColumnsName.get("Ship-to Country Code")).getStringCellValue();
        Country country = countryService.findByCountryCode(prepareCountries, ctryCode);
        if (country == null) {
            log.error("Not found Country with countryCode: " + ctryCode);
            return null;
        }
        shipment.setCountry(country);

        // currency
        shipment.setCurrency(USDCurrency);

        AOPMargin aopMargin = aopMarginService.getAOPMargin(prepareAOPMargins, shipment.getCountry().getRegion(), shipment.getSeries(), shipment.getProduct().getPlant(), shipment.getDate());
        if (aopMargin == null) {
            log.error("Not found AOPMargin with orderNo: " + orderNo);
            return null;
        }
        shipment.setAOPMargin(aopMargin);

        return shipment;
    }


}
