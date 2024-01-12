package com.hysteryale.service;

import com.hysteryale.exception.MissingColumnException;
import com.hysteryale.model.Currency;
import com.hysteryale.model.*;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.model.marginAnalyst.MarginAnalystMacro;
import com.hysteryale.repository.PartRepository;
import com.hysteryale.repository.bookingorder.BookingOrderRepository;
import com.hysteryale.service.marginAnalyst.MarginAnalystMacroService;
import com.hysteryale.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class BookingOrderService extends BasedService {
    @Resource
    BookingOrderRepository bookingOrderRepository;
    @Resource
    ProductDimensionService productDimensionService;

    @Resource
    AOPMarginService aopMarginService;

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


    BookingOrder mapExcelDataIntoOrderObject(Row row, HashMap<String, Integer> ORDER_COLUMNS_NAME) throws MissingColumnException {
        BookingOrder bookingOrder = new BookingOrder();

        //set OrderNo
        if (ORDER_COLUMNS_NAME.get("ORDERNO") != null) {
            String orderNo = row.getCell(ORDER_COLUMNS_NAME.get("ORDERNO")).getStringCellValue();
            bookingOrder.setOrderNo(orderNo);
        } else {
            throw new MissingColumnException("Missing column 'ORDERNO'!");
        }

        // Series
        if (ORDER_COLUMNS_NAME.get("SERIES") != null) {
            String series = row.getCell(ORDER_COLUMNS_NAME.get("SERIES")).getStringCellValue();
            bookingOrder.setSeries(series);

            //set ProductDimension
            ProductDimension productDimension = productDimensionService.getProductDimensionByMetaseries(series);
            if (productDimension != null) {
                bookingOrder.setProductDimension(productDimension);
            } else {
                logWarning("Not found ProductDimension with OrderNo" + bookingOrder.getOrderNo());
            }
        } else {
            throw new MissingColumnException("Missing column 'SERIES'!");
        }

        // set billToCost
        if (ORDER_COLUMNS_NAME.get("BILLTO") != null) {
            Cell billtoCell = row.getCell(ORDER_COLUMNS_NAME.get("BILLTO"));
            bookingOrder.setBillTo(billtoCell.getStringCellValue());
        } else {
            throw new MissingColumnException("Missing column 'BILLTO'!");
        }

        //set model
        if (ORDER_COLUMNS_NAME.get("MODEL") != null) {
            Cell modelCell = row.getCell(ORDER_COLUMNS_NAME.get("MODEL"));
            bookingOrder.setModel(modelCell.getStringCellValue());
        } else {
            throw new MissingColumnException("Missing column 'MODEL'!");
        }

        //set region
        if (ORDER_COLUMNS_NAME.get("REGION") != null) {
            Cell regionCell = row.getCell(ORDER_COLUMNS_NAME.get("REGION"));
            Region region = regionService.getRegionByShortName(regionCell.getStringCellValue());
            if (region != null) {
                bookingOrder.setRegion(region);
            } else {
                logWarning("Not found Region with OrderNo" + bookingOrder.getOrderNo());
            }
        } else {
            throw new MissingColumnException("Missing column 'REGION'!");
        }

        //set date
        if (ORDER_COLUMNS_NAME.get("DATE") != null) {
            String strDate = String.valueOf(row.getCell(ORDER_COLUMNS_NAME.get("DATE")).getNumericCellValue());
            Pattern pattern = Pattern.compile("^\\d(\\d\\d)(\\d\\d)(\\d\\d)");
            Matcher matcher = pattern.matcher(strDate);
            int year, month, day;

            if (matcher.find()) {
                year = Integer.parseInt(matcher.group(1)) + 2000;
                month = Integer.parseInt(matcher.group(2));
                day = Integer.parseInt(matcher.group(3));

                GregorianCalendar orderDate = new GregorianCalendar();
                // {month - 1} is the index to get value in List of month {Jan, Feb, March, April, May, ...}
                orderDate.set(year, month - 1, day);
                orderDate.add(Calendar.DATE, 1);
                bookingOrder.setDate(orderDate);
            }
        } else {
            throw new MissingColumnException("Missing column 'DATE'!");
        }

        // dealerName
        if (ORDER_COLUMNS_NAME.get("DEALERNAME") != null) {
            Cell dealerNameCell = row.getCell(ORDER_COLUMNS_NAME.get("DEALERNAME"));
            bookingOrder.setDealerName(dealerNameCell.getStringCellValue());
        } else {
            throw new MissingColumnException("Missing column 'DEALERNAME'!");
        }

        // country code
        if (ORDER_COLUMNS_NAME.get("CTRYCODE") != null) {
            Cell ctryCodeCell = row.getCell(ORDER_COLUMNS_NAME.get("CTRYCODE"));
            bookingOrder.setCtryCode(ctryCodeCell.getStringCellValue());
        } else {
            throw new MissingColumnException("Missing column 'CTRYCODE'!");
        }

        return bookingOrder;
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
    public void importOrder() throws IOException, IllegalAccessException, MissingColumnException {

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

    public void importNewBookingFileByFile(String filePath, InputStream isListCostData) throws IOException, MissingColumnException {
        InputStream is = new FileInputStream(filePath);
        XSSFWorkbook workbook = new XSSFWorkbook(is);
        List<BookingOrder> bookingOrderList = new LinkedList<>();
        HashMap<String, Integer> ORDER_COLUMNS_NAME = new HashMap<>();
        List<String> USPlant = PlantUtil.getUSPlant();
        List<CostDataFile> listCostDataByMonthAndYear = getListCostDataByMonthAndYear(isListCostData);
        Sheet orderSheet = workbook.getSheet("NOPLDTA.NOPORDP,NOPLDTA.>Sheet1");
        int numRowName = 0;
        if (orderSheet == null) {
            orderSheet = workbook.getSheet("Input - Bookings");
            numRowName = 1;
        }
        //get list cost data from month and year

        for (Row row : orderSheet) {
            if (row.getRowNum() == numRowName) getOrderColumnsName(row, ORDER_COLUMNS_NAME);
            else if (!row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().isEmpty() && row.getRowNum() > numRowName) {
                BookingOrder newBookingOrder = mapExcelDataIntoOrderObject(row, ORDER_COLUMNS_NAME);
                // import DN, DNAfterSurcharge
                newBookingOrder = importDNAndDNAfterSurcharge(newBookingOrder);

                if (USPlant.contains(newBookingOrder.getProductDimension().getPlant())) {
                    newBookingOrder = setTotalCostAndCurrency(newBookingOrder, listCostDataByMonthAndYear);
                    logInfo("US Plant");
                } else {
                    newBookingOrder = importCostRMBOfEachParts(newBookingOrder);
                }

                newBookingOrder = calculateMargin(newBookingOrder);
                newBookingOrder = importAOPMargin(newBookingOrder);
                bookingOrderList.add(newBookingOrder);
            }
        }
        bookingOrderRepository.saveAll(bookingOrderList);

    }

    public void importNewBookingFileByFile(String filePath) throws IOException, MissingColumnException {

        InputStream is = new FileInputStream(filePath);
        XSSFWorkbook workbook = new XSSFWorkbook(is);
        List<BookingOrder> bookingOrderList = new LinkedList<>();
        HashMap<String, Integer> ORDER_COLUMNS_NAME = new HashMap<>();
        List<String> USPlant = PlantUtil.getUSPlant();
        Sheet orderSheet = workbook.getSheet("NOPLDTA.NOPORDP,NOPLDTA.>Sheet1");
        int numRowName = 0;
        if (orderSheet == null) {
            orderSheet = workbook.getSheet("Input - Bookings");
            numRowName = 1;
        }

        for (Row row : orderSheet) {
            if (row.getRowNum() == numRowName) getOrderColumnsName(row, ORDER_COLUMNS_NAME);
            else if (!row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().isEmpty() && row.getRowNum() > numRowName) {
                BookingOrder newBookingOrder = mapExcelDataIntoOrderObject(row, ORDER_COLUMNS_NAME);
                // import DN, DNAfterSurcharge
                newBookingOrder = importDNAndDNAfterSurcharge(newBookingOrder);

                if (USPlant.contains(newBookingOrder.getProductDimension().getPlant())) {
                    logInfo("US Plant");
                    // import totalCost when import file totalCost

                    Optional<BookingOrder> orderExisted = bookingOrderRepository.getBookingOrderByOrderNo(newBookingOrder.getOrderNo());
                    if (orderExisted.isPresent()) {
                        BookingOrder oldBooking = orderExisted.get();
                        newBookingOrder.setCurrency(oldBooking.getCurrency());
                        newBookingOrder.setAOPMarginPercentage(oldBooking.getAOPMarginPercentage());
                        newBookingOrder.setMarginPercentageAfterSurCharge(oldBooking.getMarginPercentageAfterSurCharge());
                        newBookingOrder.setMarginAfterSurCharge(oldBooking.getMarginAfterSurCharge());
                        newBookingOrder.setTotalCost(oldBooking.getTotalCost());
                    }
                } else {
                    newBookingOrder = importCostRMBOfEachParts(newBookingOrder);
                }

                newBookingOrder = calculateMargin(newBookingOrder);
                newBookingOrder = importAOPMargin(newBookingOrder);
                bookingOrderList.add(newBookingOrder);
            }
        }
        logInfo("list booked" + bookingOrderList.size());
        bookingOrderRepository.saveAll(bookingOrderList);

    }

    //get Booking Exist
    private List<BookingOrder> getListBookingExist(List<BookingOrder> booking) {
        List<String> listOrderNo = new ArrayList<>();
        booking.forEach(b -> listOrderNo.add(b.getOrderNo()));
        return bookingOrderRepository.getListBookingExist(listOrderNo);
    }


    public void importOldBookingFileByFile(String pathFile, String month, String year) throws IOException, MissingColumnException {
        //step 1: import fact data from booking-register file
        //step 2: import Margin data from booking-register file
        //step 3: calculate totalCost
        InputStream is = new FileInputStream(pathFile);
        XSSFWorkbook workbook = new XSSFWorkbook(is);
        List<BookingOrder> bookingOrderList = new LinkedList<>();
        HashMap<String, Integer> ORDER_COLUMNS_NAME = new HashMap<>();

        Sheet orderSheet = workbook.getSheet("NOPLDTA.NOPORDP,NOPLDTA.>Sheet1");

        List<MarginDataFile> marginDataFileList = getListMarginDataByMonthAndYear(month, year);

        for (Row row : orderSheet) {
            if (row.getRowNum() == 0) getOrderColumnsName(row, ORDER_COLUMNS_NAME);
            else if (!row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().isEmpty() && row.getRowNum() > 1) {
                BookingOrder newBookingOrder = mapExcelDataIntoOrderObject(row, ORDER_COLUMNS_NAME);

                newBookingOrder = importDNAndDNAfterSurcharge(newBookingOrder);
                newBookingOrder = importOldMarginPercentageAndCurrency(newBookingOrder, marginDataFileList);
                newBookingOrder = calculateTotalCostAndMarginAfterSurcharge(newBookingOrder);
                newBookingOrder = importAOPMargin(newBookingOrder);
                bookingOrderList.add(newBookingOrder);
            }
        }

        bookingOrderRepository.saveAll(bookingOrderList);
    }

    public BookingOrder importOldMarginPercentageAndCurrency(BookingOrder booking, List<MarginDataFile> marginDataFileList) {
        for (MarginDataFile marginDataFile : marginDataFileList) {
            if (marginDataFile.orderNo.equals(booking.getOrderNo())) {
                booking.setMarginPercentageAfterSurCharge(marginDataFile.marginPercentage);
                Currency currency = currencyService.getCurrenciesByName(marginDataFile.currency);
                booking.setCurrency(currency);
                break;
            }
        }
        return booking;
    }

    public BookingOrder setTotalCostAndCurrency(BookingOrder booking, List<CostDataFile> costDataFileList) {
        for (CostDataFile costDataFile : costDataFileList) {
            if (costDataFile.orderNo.equals(booking.getOrderNo())) {
                booking.setTotalCost(costDataFile.totalCost);
                Currency currency = currencyService.getCurrencies(costDataFile.currency);
                booking.setCurrency(currency);
                break;
            }
        }
        return booking;
    }

    private BookingOrder importAOPMargin(BookingOrder booking) {
        Double aopMargin = aopMarginService.getAOPMargin(booking.getSeries(), booking.getRegion().getRegion(), booking.getProductDimension().getPlant());
        if (aopMargin != null)
            booking.setAOPMarginPercentage(aopMargin);
        return booking;
    }

    public BookingOrder importCostRMBOfEachParts(BookingOrder bookingOrder) {
        List<String> listPartNumber = partService.getAllPartNumbersByOrderNo(bookingOrder.getOrderNo());
        Currency currency = partService.getCurrencyByOrderNo(bookingOrder.getOrderNo());

        Calendar orderDate = bookingOrder.getDate();
        Calendar date = Calendar.getInstance();
        date.set(orderDate.get(Calendar.YEAR), orderDate.get(Calendar.MONTH), 1);

        if (currency == null)
            return bookingOrder;
        bookingOrder.setCurrency(currency);
        logInfo(bookingOrder.getOrderNo() + "   " + currency.getCurrency());
        double totalCost = 0;
        if (!bookingOrder.getProductDimension().getPlant().equals("SN")) { // plant is Hysteryale, Maximal, Ruyi, Staxx
            List<MarginAnalystMacro> marginAnalystMacroList = marginAnalystMacroService.getMarginAnalystMacroByHYMPlantAndListPartNumber(
                    bookingOrder.getModel(), listPartNumber, bookingOrder.getCurrency().getCurrency(), date);
            for (MarginAnalystMacro marginAnalystMacro : marginAnalystMacroList) {
                totalCost += marginAnalystMacro.getCostRMB();
            }
            // exchange rate
            ExchangeRate exchangeRate = exchangeRateService.getNearestExchangeRate("CNY", bookingOrder.getCurrency().getCurrency());
            if (exchangeRate != null) {
                totalCost *= exchangeRate.getRate();
                logInfo("None SN list " + marginAnalystMacroList.size() + "  " + bookingOrder.getOrderNo() + "  " + bookingOrder.getModel() + "  " + exchangeRate.getRate());
            }
        } else { // plant is SN
            List<MarginAnalystMacro> marginAnalystMacroList = marginAnalystMacroService.getMarginAnalystMacroByPlantAndListPartNumber(
                    bookingOrder.getModel(), listPartNumber, bookingOrder.getCurrency().getCurrency(),
                    bookingOrder.getProductDimension().getPlant(), date);

            for (MarginAnalystMacro marginAnalystMacro : marginAnalystMacroList) {
                totalCost += marginAnalystMacro.getCostRMB();
            }
            ExchangeRate exchangeRate = exchangeRateService.getNearestExchangeRate("USD", bookingOrder.getCurrency().getCurrency());
            if (exchangeRate != null) {
                totalCost *= exchangeRate.getRate();
                logInfo(" SN list " + marginAnalystMacroList.size() + "  " + bookingOrder.getOrderNo() + "  " + bookingOrder.getModel() + "  " + exchangeRate.getRate());
            }
        }


        bookingOrder.setTotalCost(totalCost);
        logInfo(bookingOrder.getTotalCost() + "");
        return bookingOrder;
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
                InputStream is = new FileInputStream(folderPath + "/" + fileName);
                return is;
            }
        }
        return null;
    }

    private List<CostDataFile> getListCostDataByMonthAndYear(InputStream is) throws IOException {
        List<CostDataFile> result = new ArrayList<>();

        XSSFWorkbook workbook = new XSSFWorkbook(is);
        // if old data -> colect from sheet "Wk - Margins", else -> sheet "Cost Data"
        Sheet sheet = workbook.getSheet("Cost Data");

        HashMap<String, Integer> ORDER_COLUMNS_NAME = new HashMap<>();
        for (Row row : sheet) {
            if (row.getRowNum() == 0) getOrderColumnsName(row, ORDER_COLUMNS_NAME);
            else if (!row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().isEmpty() && row.getRowNum() > 0) {

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

                //get Currency
                Cell currencyCell = row.getCell(ORDER_COLUMNS_NAME.get("Curr"));
                costDataFile.currency = currencyCell.getStringCellValue();

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

                        //get Currency
                        Cell currencyCell = row.getCell(ORDER_COLUMNS_NAME.get("Currency"));
                        marginDataFile.currency = currencyCell.getStringCellValue();

                        result.add(marginDataFile);
                    }
                }
            }
        }
        return result;
    }

    public void importCostData(String filePath) throws IOException {
        InputStream is = new FileInputStream(filePath);
        List<CostDataFile> costDataList = getListCostDataByMonthAndYear(is);
        List<String> listOrderNo = new ArrayList<>();
        costDataList.forEach(c -> listOrderNo.add(c.orderNo));
        List<BookingOrder> listBookingExisted = bookingOrderRepository.getListBookingExist(listOrderNo);
        for (BookingOrder bookingOrder : listBookingExisted) {
            for (CostDataFile costData : costDataList) {
                if (bookingOrder.getOrderNo().equals(costData.orderNo)) {
                    bookingOrder.setTotalCost(costData.totalCost);
                    Currency currency = currencyService.getCurrencies(costData.currency);
                    bookingOrder.setCurrency(currency);
                    bookingOrder = calculateMargin(bookingOrder);

                }
            }
        }
        bookingOrderRepository.saveAll(listBookingExisted);
    }


    private static class CostDataFile {
        String orderNo;
        double totalCost;
        String currency;
    }

    private static class MarginDataFile {
        String orderNo;
        double marginPercentage;
        String currency;
    }


    public boolean checkOldData(String month, String year) {
        return Integer.parseInt(year) < 2023 | (Integer.parseInt(year) == 2023 && !(month.equals("Sep") | month.equals("Oct") | month.equals("Nov") | month.equals("Dec")));
    }

    public List<BookingOrder> getAllBookingOrders() {
        return bookingOrderRepository.findAll();
    }


    public BookingOrder importDNAndDNAfterSurcharge(BookingOrder booking) {
        Set<Part> newParts = partRepository.getPartByOrderNumber(booking.getOrderNo());
        double dealerNet = 0;
        for (Part part : newParts) {
            dealerNet += part.getNetPriceEach();
        }
        double surcharge = 0;
        booking.setDealerNet(dealerNet);
        booking.setDealerNetAfterSurCharge(dealerNet - surcharge);
        return booking;
    }

    /**
     * for new data
     */
    private BookingOrder calculateMargin(BookingOrder booking) {
        //need : DNAfterSurcharge, totalCost
        double dealerNetAfterSurcharge = booking.getDealerNetAfterSurCharge();
        double totalCost = booking.getTotalCost();
        double marginAfterSurcharge = dealerNetAfterSurcharge - totalCost;
        double marginPercentageAfterSurcharge = marginAfterSurcharge / dealerNetAfterSurcharge;
        booking.setMarginAfterSurCharge(marginAfterSurcharge);
        booking.setMarginPercentageAfterSurCharge(marginPercentageAfterSurcharge);
        return booking;
    }

    /**
     * old data
     */
    private BookingOrder calculateTotalCostAndMarginAfterSurcharge(BookingOrder booking) {
        // need : Margin% , DNAfterSurcharge
        double dealerNetAfterSurcharge = booking.getDealerNetAfterSurCharge();
        double marginPercentageAfterSurcharge = booking.getMarginPercentageAfterSurCharge();
        double marginAfterSurcharge = dealerNetAfterSurcharge * marginPercentageAfterSurcharge;
        double totalCost = dealerNetAfterSurcharge - marginAfterSurcharge;
        booking.setTotalCost(totalCost);
        booking.setMarginAfterSurCharge(marginAfterSurcharge);
        return booking;
    }


    public Optional<BookingOrder> getDistinctBookingOrderByModelCode(String modelCode) {
        return bookingOrderRepository.getDistinctBookingOrderByModelCode(modelCode);
    }

    public Optional<BookingOrder> getBookingOrderByOrderNumber(String orderNumber) {
        return bookingOrderRepository.findById(orderNumber);
    }

    public Map<String, Object> getBookingByFilter(FilterModel filterModel) throws java.text.ParseException {
        Map<String, Object> result = new HashMap<>();
        //Get FilterData
        Map<String, Object> filterMap = ConvertDataFilterUtil.loadDataFilterIntoMap(filterModel);
        logInfo(filterMap.toString());

        List<BookingOrder> bookingOrderList = bookingOrderRepository.selectAllForBookingOrder(
                (String) filterMap.get("orderNoFilter"), (List<String>) filterMap.get("regionFilter"), (List<String>) filterMap.get("plantFilter"),
                (List<String>) filterMap.get("metaSeriesFilter"), (List<String>) filterMap.get("classFilter"), (List<String>) filterMap.get("modelFilter"),
                (List<String>) filterMap.get("segmentFilter"), (List<String>) filterMap.get("dealerNameFilter"), (String) filterMap.get("aopMarginPercentageFilter"),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((String) ((List) filterMap.get("marginPercentageFilter")).get(0)),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((Double) ((List) filterMap.get("marginPercentageFilter")).get(1)),
                (Calendar) filterMap.get("fromDateFilter"), (Calendar) filterMap.get("toDateFilter"), (Pageable) filterMap.get("pageable")
        );

        // get currency for order -> get exchange_rate
        List<String> listCurrency = new ArrayList<>();
        List<ExchangeRate> exchangeRateList = new ArrayList<>();
        List<String> listTargetCurrency = TargetCurrency.getListTargetCurrency;
        BookingOrder rowTotal = new BookingOrder();
        List<BookingOrder> listRowTotal = List.of(rowTotal);
        String defaultCurrency = "USD";
        rowTotal.setCurrency(new Currency(defaultCurrency));
        rowTotal.setOrderNo("Total");


        for (BookingOrder bookingOrder : bookingOrderList) {
            if (bookingOrder.getCurrency() != null) {
                String currency = bookingOrder.getCurrency().getCurrency();
                if (!listCurrency.contains(currency)) { // get distinct currency in list order
                    listCurrency.add(currency);
                    for (String targetCurrency : listTargetCurrency) {
                        if (!targetCurrency.equals(currency)) { // get exchange_rate FROM current currency of order TO targetCurrency
                            exchangeRateList.add(exchangeRateService.getNearestExchangeRate(currency, targetCurrency));
                        }
                    }
                }
                // calculate RowTotal : Default currency : USD
                if (currency.equals(defaultCurrency)) { // USD: don't exchange
                    rowTotal.setDealerNet(rowTotal.getDealerNet() + bookingOrder.getDealerNet());
                    rowTotal.setDealerNetAfterSurCharge(rowTotal.getDealerNetAfterSurCharge() + bookingOrder.getDealerNetAfterSurCharge());
                    rowTotal.setTotalCost(rowTotal.getTotalCost() + bookingOrder.getTotalCost());
                    rowTotal.setMarginAfterSurCharge(rowTotal.getDealerNetAfterSurCharge() - rowTotal.getTotalCost());
                    rowTotal.setMarginPercentageAfterSurCharge(rowTotal.getMarginAfterSurCharge() / rowTotal.getDealerNetAfterSurCharge());
                } else {// <> USD: exchange to USD
                    double rate = exchangeRateService.getNearestExchangeRate(currency, defaultCurrency).getRate();
                    rowTotal.setDealerNet(rowTotal.getDealerNet() + bookingOrder.getDealerNet() * rate);
                    rowTotal.setDealerNetAfterSurCharge(rowTotal.getDealerNetAfterSurCharge() + bookingOrder.getDealerNetAfterSurCharge() * rate);
                    rowTotal.setTotalCost(rowTotal.getTotalCost() + bookingOrder.getTotalCost() * rate);
                    rowTotal.setMarginAfterSurCharge(rowTotal.getDealerNetAfterSurCharge() - rowTotal.getTotalCost());
                    rowTotal.setMarginPercentageAfterSurCharge(rowTotal.getMarginAfterSurCharge() / rowTotal.getDealerNetAfterSurCharge());
                }
            }
        }


        result.put("listExchangeRate", exchangeRateList);
        result.put("listBookingOrder", bookingOrderList);


        //get total Recode
        int countAll = bookingOrderRepository.getCount((String) filterMap.get("orderNoFilter"), (List<String>) filterMap.get("regionFilter"), (List<String>) filterMap.get("plantFilter"),
                (List<String>) filterMap.get("metaSeriesFilter"), (List<String>) filterMap.get("classFilter"), (List<String>) filterMap.get("modelFilter"),
                (List<String>) filterMap.get("segmentFilter"), (List<String>) filterMap.get("dealerNameFilter"), (String) filterMap.get("aopMarginPercentageFilter"),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((String) ((List) filterMap.get("marginPercentageFilter")).get(0)),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((Double) ((List) filterMap.get("marginPercentageFilter")).get(1)),
                (Calendar) filterMap.get("fromDateFilter"), (Calendar) filterMap.get("toDateFilter"));
        result.put("totalItems", countAll);

        // get data for totalRow
        List<BookingOrder> getTotal = bookingOrderRepository.getTotal((String) filterMap.get("orderNoFilter"), (List<String>) filterMap.get("regionFilter"), (List<String>) filterMap.get("plantFilter"),
                (List<String>) filterMap.get("metaSeriesFilter"), (List<String>) filterMap.get("classFilter"), (List<String>) filterMap.get("modelFilter"),
                (List<String>) filterMap.get("segmentFilter"), (List<String>) filterMap.get("dealerNameFilter"), (String) filterMap.get("aopMarginPercentageFilter"),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((String) ((List) filterMap.get("marginPercentageFilter")).get(0)),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((Double) ((List) filterMap.get("marginPercentageFilter")).get(1)),
                (Calendar) filterMap.get("fromDateFilter"), (Calendar) filterMap.get("toDateFilter"));
        result.put("total", listRowTotal);


        return result;
    }
}
