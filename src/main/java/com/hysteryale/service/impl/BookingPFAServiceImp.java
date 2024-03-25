package com.hysteryale.service.impl;

import com.hysteryale.exception.IncorectFormatCellException;
import com.hysteryale.exception.MissingColumnException;
import com.hysteryale.exception.MissingSheetException;
import com.hysteryale.model.Booking;
import com.hysteryale.model.BookingFPA;
import com.hysteryale.model.ExchangeRate;
import com.hysteryale.model.Shipment;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.model.payLoad.BookingMarginTrialTestPayLoad;
import com.hysteryale.repository.BookingFPARepository;
import com.hysteryale.repository.BookingRepository;
import com.hysteryale.repository.ExchangeRateRepository;
import com.hysteryale.repository.ShipmentRepository;
import com.hysteryale.service.*;
import com.hysteryale.utils.CheckRequiredColumnUtils;
import com.hysteryale.utils.ConvertDataFilterUtil;
import com.hysteryale.utils.DateUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class BookingPFAServiceImp extends BasedService implements BookingFPAService {

    @Resource
    private BookingFPARepository bookingFPARepository;

    @Resource
    private BookingService bookingService;

    @Resource
    private ShipmentService shipmentService;

    @Resource
    private ExchangeRateService exchangeRateService;

    @Resource
    private ExchangeRateRepository exchangeRateRepository;

    @Resource
    private ShipmentRepository shipmentRepository;

    @Resource
    private BookingRepository bookingRepository;

    public void getOrderColumnsName(Row row, HashMap<String, Integer> ORDER_COLUMNS_NAME) {
        for (int i = 0; i < 10; i++) {
            if (row.getCell(i) != null) {
                String columnName = row.getCell(i).getStringCellValue().trim();
                if (ORDER_COLUMNS_NAME.containsKey(columnName)) continue;
                ORDER_COLUMNS_NAME.put(columnName, i);
            }
        }
    }

    public void importBookingFPA(InputStream is, String savedFileName) throws IOException, MissingSheetException, MissingColumnException, IncorectFormatCellException {

        XSSFWorkbook workbook = new XSSFWorkbook(is);

        List<BookingFPA> bookingList = new LinkedList<>();
        String sheetName = CheckRequiredColumnUtils.BOOKING_FPA_REQUIRED_SHEET;
        XSSFSheet orderSheet = workbook.getSheet(sheetName);
        if (orderSheet == null)
            throw new MissingSheetException(sheetName, savedFileName);

        HashMap<String, Integer> ORDER_COLUMNS_NAME = new HashMap<>();

        for (Row row : orderSheet) {
            if (row.getRowNum() == 0) {
                getOrderColumnsName(row, ORDER_COLUMNS_NAME);
                CheckRequiredColumnUtils.checkRequiredColumn(new ArrayList<>(ORDER_COLUMNS_NAME.keySet()), CheckRequiredColumnUtils.BOOKING_FPA_REQUIRED_COLUMN, savedFileName);
            } else if (!row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().isEmpty() && row.getRowNum() > 0) {

                BookingFPA newBookingFPA = mappingDataExcelIntoBookingFPA(row, ORDER_COLUMNS_NAME, savedFileName);

                bookingList.add(newBookingFPA);

            }
        }

        bookingFPARepository.saveAll(bookingList);

    }

    @Override
    public Map<String, Object> getBookingMarginTrialTest(FilterModel filter) throws ParseException {
        Map<String, Object> result = new HashMap<>();
        //Get FilterData
        Map<String, Object> filterMap = ConvertDataFilterUtil.loadDataFilterIntoMap(filter);

        // get data booking -> by orderNo to get shipment, bookingFPA
        List<Booking> bookings = bookingService.getListBookingByFilter(filter);

        int totalItemBookings = bookingService.countBookingsWithFilter(filter);

        List<String> orderNos = bookingService.getListOrderNoFromListBooking(bookings);
        List<Shipment> shipments = shipmentService.getListShipmentByOrderNos(orderNos);
        List<BookingFPA> bookingFPAs = bookingFPARepository.findByListOrderNo(orderNos);
        List<ExchangeRate> exchangeRates = exchangeRateRepository.getExchangeRateToUSD();
        bookingService.convertCurrencyOfBookingToUSD(bookings, exchangeRates);

        // time
        result.put("serverTimeZone", TimeZone.getDefault().getID());

        // last update at
        Optional<LocalDateTime> latestUpdatedTimeShipmentOptional = shipmentRepository.getLatestUpdatedTime();
        List<LocalDateTime> listUpdatedTime = new ArrayList<>();
        latestUpdatedTimeShipmentOptional.ifPresent(listUpdatedTime::add);

        Optional<LocalDateTime> latestUpdatedTimeBookingOptional = bookingRepository.getLatestUpdatedTime();
        latestUpdatedTimeBookingOptional.ifPresent(listUpdatedTime::add);

        Optional<LocalDateTime> latestUpdatedTimeBookingFPAOptional = bookingFPARepository.getLatestUpdatedTime();
        latestUpdatedTimeBookingFPAOptional.ifPresent(listUpdatedTime::add);

        result.put("latestUpdatedTime", DateUtils.convertLocalDateTimeToString(DateUtils.getLastUpdatedTime(listUpdatedTime)));

        result.put("listOrder", convertToBookingMarginTrialTest(bookings, shipments, bookingFPAs));
        result.put("totalItems", totalItemBookings);

        return result;
    }

    public List<BookingMarginTrialTestPayLoad> convertToBookingMarginTrialTest(List<Booking> bookings, List<Shipment> shipments, List<BookingFPA> bookingFPAs) {
        List<BookingMarginTrialTestPayLoad> result = new ArrayList<>();
        for (Booking booking : bookings) {
            BookingMarginTrialTestPayLoad bookingMarginTrialTestPayLoad = new BookingMarginTrialTestPayLoad();
            bookingMarginTrialTestPayLoad.setBooking(booking);

            Shipment shipment = shipmentService.getShipmentByOrderNo(shipments, booking.getOrderNo());
            bookingMarginTrialTestPayLoad.setShipment(shipment);

            BookingFPA bookingFPA = getBookingFPAByOrderNo(bookingFPAs, booking.getOrderNo());
            bookingMarginTrialTestPayLoad.setBookingFPA(bookingFPA);
            result.add(bookingMarginTrialTestPayLoad);
        }
        return result;
    }

    @Override
    public List<BookingFPA> getBookingFPAByListOrderNo(List<String> orderNos) {
        return bookingFPARepository.findByListOrderNo(orderNos);
    }

    @Override
    public BookingFPA getBookingFPAByOrderNo(List<BookingFPA> bookingFPAs, String orderNo) {
        for (BookingFPA bookingFPA : bookingFPAs) {
            if (bookingFPA.getOrderNo().equals(orderNo))
                return bookingFPA;
        }
        return null;
    }


    private BookingFPA mappingDataExcelIntoBookingFPA(Row row, HashMap<String, Integer> ORDER_COLUMNS_NAME, String fileName) throws IncorectFormatCellException {

        BookingFPA bookingFPA = new BookingFPA();

        // orderNo
        Cell orderNoCell = row.getCell(ORDER_COLUMNS_NAME.get("Order No."));
        if (orderNoCell.getCellType() != CellType.STRING || orderNoCell.getStringCellValue().isEmpty()) {
            throw new IncorectFormatCellException("Incorrect format of Cell " + (row.getRowNum() + 1) + ":" + (ORDER_COLUMNS_NAME.get("Order No.") + 1), fileName);
        }
        bookingFPA.setOrderNo(orderNoCell.getStringCellValue());

        //CellType
        Cell typeCell = row.getCell(ORDER_COLUMNS_NAME.get("Inc_Cst#"));
        bookingFPA.setType(typeCell.getStringCellValue());

        //dealerNet
        Cell dealerNetCell = row.getCell(ORDER_COLUMNS_NAME.get("Revised Net Sales"));
        if (dealerNetCell.getCellType() != CellType.NUMERIC) {
            throw new IncorectFormatCellException("Incorrect format of Cell " + (row.getRowNum() + 1) + ":" + (ORDER_COLUMNS_NAME.get("Revised Net Sales") + 1),fileName);
        }
        double dealerNet = dealerNetCell.getNumericCellValue();
        bookingFPA.setDealerNet(dealerNet);

        //cost
        Cell costCell = row.getCell(ORDER_COLUMNS_NAME.get("Revised Cost"));
        if (costCell.getCellType() != CellType.NUMERIC) {
            throw new IncorectFormatCellException("Incorrect format of Cell " + (row.getRowNum() + 1) + ":" + (ORDER_COLUMNS_NAME.get("Revised Cost") + 1), fileName);
        }
        double cost = costCell.getNumericCellValue();
        bookingFPA.setTotalCost(cost);

        // margin, margin%
        double margin = dealerNet - cost;
        double marginPercentage = margin / dealerNet;

        bookingFPA.setMargin(margin);
        bookingFPA.setMarginPercentage(marginPercentage);

        return bookingFPA;
    }


}
