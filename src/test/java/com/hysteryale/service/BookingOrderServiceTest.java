package com.hysteryale.service;

import com.hysteryale.model.BookingOrder;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.utils.CurrencyFormatUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@SuppressWarnings("unchecked")
@Slf4j
public class BookingOrderServiceTest {
    @Resource
    BookingOrderService bookingOrderService;
    FilterModel filters;


    /**
     * Reset the filters to initial state
     */
    private void resetFilters() {
        filters = new FilterModel(
                "",
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                "",
                "",
                "",
                "2023-05-01",
                "2023-05-31",
                null,
                1500,
                1,
                "",
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>());
    }
    @BeforeEach
    public void setUp(){
        resetFilters();
    }

    @Test
    void testGetAllFilesInFolder() {
        // GIVEN
        String folderPath = "import_files/booking";
        int expectedListSize = 16;

        List<String> fileList = bookingOrderService.getAllFilesInFolder(folderPath);
        Assertions.assertEquals(expectedListSize, fileList.size());
    }

    @Test
    void checkOldDate() {
        assertTrue(bookingOrderService.checkOldData("Apr", "2023"));
        assertFalse(bookingOrderService.checkOldData("Sep", "2023"));
        assertFalse(bookingOrderService.checkOldData("Nov", "2023"));
    }

    private void assertTotalResultValue(BookingOrder totalResult, long quantity, double totalDealerNet, double totalDNAfterSurcharge,
                                        double totalCost, double totalMarginAfterSurcharge, double totalMarginPercentage) {
        Assertions.assertEquals(quantity, totalResult.getQuantity());

        Assertions.assertEquals(
                CurrencyFormatUtils.formatDoubleValue(totalDealerNet, CurrencyFormatUtils.decimalFormatFourDigits),
                CurrencyFormatUtils.formatDoubleValue(totalResult.getDealerNet(), CurrencyFormatUtils.decimalFormatFourDigits)
        );
        Assertions.assertEquals(
                CurrencyFormatUtils.formatDoubleValue(totalDNAfterSurcharge, CurrencyFormatUtils.decimalFormatFourDigits),
                CurrencyFormatUtils.formatDoubleValue(totalResult.getDealerNetAfterSurCharge(), CurrencyFormatUtils.decimalFormatFourDigits)
        );
        Assertions.assertEquals(
                CurrencyFormatUtils.formatDoubleValue(totalCost, CurrencyFormatUtils.decimalFormatFourDigits),
                CurrencyFormatUtils.formatDoubleValue(totalResult.getTotalCost(), CurrencyFormatUtils.decimalFormatFourDigits)
        );
        Assertions.assertEquals(
                CurrencyFormatUtils.formatDoubleValue(totalMarginAfterSurcharge, CurrencyFormatUtils.decimalFormatFourDigits),
                CurrencyFormatUtils.formatDoubleValue(totalResult.getMarginAfterSurCharge(), CurrencyFormatUtils.decimalFormatFourDigits)
        );
        Assertions.assertEquals(
                CurrencyFormatUtils.formatDoubleValue(totalMarginPercentage, CurrencyFormatUtils.decimalFormatFourDigits),
                CurrencyFormatUtils.formatDoubleValue(totalResult.getMarginPercentageAfterSurCharge(), CurrencyFormatUtils.decimalFormatFourDigits)
        );
    }

    @Test
    public void testGetBookingByFilter_region() throws ParseException {
        resetFilters();

        String region = "Asia";
        filters.setRegions(Collections.singletonList(region));

        Map<String, Object> result = bookingOrderService.getBookingByFilter(filters);
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertNotNull(result.get("total"));
        Assertions.assertNotNull(result.get("listBookingOrder"));

        BookingOrder totalResult = ((List<BookingOrder>) result.get("total")).get(0);
        List<BookingOrder> listResult = (List<BookingOrder>) result.get("listBookingOrder");
        Assertions.assertFalse(listResult.isEmpty());

        long quantity = 0;
        double totalDealerNet = 0.0;
        double totalDNAfterSurcharge = 0.0;
        double totalCost = 0.0;
        double totalMarginAfterSurcharge = 0.0;

        for(BookingOrder bo : listResult) {
            Assertions.assertEquals(region, bo.getRegion().getRegion());

            quantity += bo.getQuantity();
            totalDealerNet += bo.getDealerNet();
            totalDNAfterSurcharge += bo.getDealerNetAfterSurCharge();
            totalCost += bo.getTotalCost();
            totalMarginAfterSurcharge += bo.getMarginAfterSurCharge();
        }
        double totalMarginPercentage = (totalDealerNet - totalCost) / totalDealerNet;
        assertTotalResultValue(totalResult, quantity, totalDealerNet, totalDNAfterSurcharge, totalCost, totalMarginAfterSurcharge, totalMarginPercentage);
    }

    @Test
    public void testGetBookingByFilter_plant() throws ParseException {
        resetFilters();

        String plant = "Ruyi";
        filters.setPlants(Collections.singletonList(plant));

        Map<String, Object> result = bookingOrderService.getBookingByFilter(filters);
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertNotNull(result.get("total"));
        Assertions.assertNotNull(result.get("listBookingOrder"));

        BookingOrder totalResult = ((List<BookingOrder>) result.get("total")).get(0);
        List<BookingOrder> listResult = (List<BookingOrder>) result.get("listBookingOrder");
        Assertions.assertFalse(listResult.isEmpty());

        long quantity = 0;
        double totalDealerNet = 0.0;
        double totalDNAfterSurcharge = 0.0;
        double totalCost = 0.0;
        double totalMarginAfterSurcharge = 0.0;

        for(BookingOrder bo : listResult) {
            Assertions.assertEquals(plant, bo.getProductDimension().getPlant());

            quantity += bo.getQuantity();
            totalDealerNet += bo.getDealerNet();
            totalDNAfterSurcharge += bo.getDealerNetAfterSurCharge();
            totalCost += bo.getTotalCost();
            totalMarginAfterSurcharge += bo.getMarginAfterSurCharge();
        }
        double totalMarginPercentage = (totalDealerNet - totalCost) / totalDealerNet;
        assertTotalResultValue(totalResult, quantity, totalDealerNet, totalDNAfterSurcharge, totalCost, totalMarginAfterSurcharge, totalMarginPercentage);
    }

    @Test
    public void testGetBookingByFilter_metaSeries() throws ParseException {
        resetFilters();

        String metaSeries = "3C7";
        filters.setMetaSeries(Collections.singletonList(metaSeries));

        Map<String, Object> result = bookingOrderService.getBookingByFilter(filters);
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertNotNull(result.get("total"));
        Assertions.assertNotNull(result.get("listBookingOrder"));

        BookingOrder totalResult = ((List<BookingOrder>) result.get("total")).get(0);
        List<BookingOrder> listResult = (List<BookingOrder>) result.get("listBookingOrder");
        Assertions.assertFalse(listResult.isEmpty());

        long quantity = 0;
        double totalDealerNet = 0.0;
        double totalDNAfterSurcharge = 0.0;
        double totalCost = 0.0;
        double totalMarginAfterSurcharge = 0.0;

        for(BookingOrder bo : listResult) {
            Assertions.assertEquals(metaSeries, bo.getProductDimension().getMetaSeries());

            quantity += bo.getQuantity();
            totalDealerNet += bo.getDealerNet();
            totalDNAfterSurcharge += bo.getDealerNetAfterSurCharge();
            totalCost += bo.getTotalCost();
            totalMarginAfterSurcharge += bo.getMarginAfterSurCharge();
        }
        double totalMarginPercentage = (totalDealerNet - totalCost) / totalDealerNet;
        assertTotalResultValue(totalResult, quantity, totalDealerNet, totalDNAfterSurcharge, totalCost, totalMarginAfterSurcharge, totalMarginPercentage);
    }

    @Test
    public void testGetBookingByFilter_dealer() throws ParseException {
        resetFilters();

        String dealer = "DILOK AND SONS CO.,LTD.";
        filters.setDealers(Collections.singletonList(dealer));

        Map<String, Object> result = bookingOrderService.getBookingByFilter(filters);
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertNotNull(result.get("total"));
        Assertions.assertNotNull(result.get("listBookingOrder"));

        BookingOrder totalResult = ((List<BookingOrder>) result.get("total")).get(0);
        List<BookingOrder> listResult = (List<BookingOrder>) result.get("listBookingOrder");
        Assertions.assertFalse(listResult.isEmpty());

        long quantity = 0;
        double totalDealerNet = 0.0;
        double totalDNAfterSurcharge = 0.0;
        double totalCost = 0.0;
        double totalMarginAfterSurcharge = 0.0;

        for(BookingOrder bo : listResult) {
            Assertions.assertEquals(dealer, bo.getDealerName());

            quantity += bo.getQuantity();
            totalDealerNet += bo.getDealerNet();
            totalDNAfterSurcharge += bo.getDealerNetAfterSurCharge();
            totalCost += bo.getTotalCost();
            totalMarginAfterSurcharge += bo.getMarginAfterSurCharge();
        }
        double totalMarginPercentage = (totalDealerNet - totalCost) / totalDealerNet;
        assertTotalResultValue(totalResult, quantity, totalDealerNet, totalDNAfterSurcharge, totalCost, totalMarginAfterSurcharge, totalMarginPercentage);
    }

    @Test
    public void testGetBookingByFilter_class() throws ParseException {
        resetFilters();

        String clazz = "Class 3";
        filters.setClasses(Collections.singletonList(clazz));

        Map<String, Object> result = bookingOrderService.getBookingByFilter(filters);
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertNotNull(result.get("total"));
        Assertions.assertNotNull(result.get("listBookingOrder"));

        BookingOrder totalResult = ((List<BookingOrder>) result.get("total")).get(0);
        List<BookingOrder> listResult = (List<BookingOrder>) result.get("listBookingOrder");
        Assertions.assertFalse(listResult.isEmpty());

        long quantity = 0;
        double totalDealerNet = 0.0;
        double totalDNAfterSurcharge = 0.0;
        double totalCost = 0.0;
        double totalMarginAfterSurcharge = 0.0;

        for(BookingOrder bo : listResult) {
            Assertions.assertEquals(clazz, bo.getProductDimension().getClazz());

            quantity += bo.getQuantity();
            totalDealerNet += bo.getDealerNet();
            totalDNAfterSurcharge += bo.getDealerNetAfterSurCharge();
            totalCost += bo.getTotalCost();
            totalMarginAfterSurcharge += bo.getMarginAfterSurCharge();
        }
        double totalMarginPercentage = (totalDealerNet - totalCost) / totalDealerNet;
        assertTotalResultValue(totalResult, quantity, totalDealerNet, totalDNAfterSurcharge, totalCost, totalMarginAfterSurcharge, totalMarginPercentage);
    }

    @Test
    public void testGetBookingByFilter_model() throws ParseException {
        resetFilters();

        String modelCode = "T6.0UT";
        filters.setModels(Collections.singletonList(modelCode));

        Map<String, Object> result = bookingOrderService.getBookingByFilter(filters);
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertNotNull(result.get("total"));
        Assertions.assertNotNull(result.get("listBookingOrder"));

        BookingOrder totalResult = ((List<BookingOrder>) result.get("total")).get(0);
        List<BookingOrder> listResult = (List<BookingOrder>) result.get("listBookingOrder");
        Assertions.assertFalse(listResult.isEmpty());

        long quantity = 0;
        double totalDealerNet = 0.0;
        double totalDNAfterSurcharge = 0.0;
        double totalCost = 0.0;
        double totalMarginAfterSurcharge = 0.0;

        for(BookingOrder bo : listResult) {
            Assertions.assertEquals(modelCode, bo.getProductDimension().getModelCode());

            quantity += bo.getQuantity();
            totalDealerNet += bo.getDealerNet();
            totalDNAfterSurcharge += bo.getDealerNetAfterSurCharge();
            totalCost += bo.getTotalCost();
            totalMarginAfterSurcharge += bo.getMarginAfterSurCharge();
        }
        double totalMarginPercentage = (totalDealerNet - totalCost) / totalDealerNet;
        assertTotalResultValue(totalResult, quantity, totalDealerNet, totalDNAfterSurcharge, totalCost, totalMarginAfterSurcharge, totalMarginPercentage);
    }

    @Test
    public void testGetBookingByFilter_segment() throws ParseException {
        resetFilters();

        String segment = "C3 - Low Intensity";
        filters.setSegments(Collections.singletonList(segment));

        Map<String, Object> result = bookingOrderService.getBookingByFilter(filters);
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertNotNull(result.get("total"));
        Assertions.assertNotNull(result.get("listBookingOrder"));

        BookingOrder totalResult = ((List<BookingOrder>) result.get("total")).get(0);
        List<BookingOrder> listResult = (List<BookingOrder>) result.get("listBookingOrder");
        Assertions.assertFalse(listResult.isEmpty());

        long quantity = 0;
        double totalDealerNet = 0.0;
        double totalDNAfterSurcharge = 0.0;
        double totalCost = 0.0;
        double totalMarginAfterSurcharge = 0.0;

        for(BookingOrder bo : listResult) {
            Assertions.assertEquals(segment, bo.getProductDimension().getSegment());

            quantity += bo.getQuantity();
            totalDealerNet += bo.getDealerNet();
            totalDNAfterSurcharge += bo.getDealerNetAfterSurCharge();
            totalCost += bo.getTotalCost();
            totalMarginAfterSurcharge += bo.getMarginAfterSurCharge();
        }
        double totalMarginPercentage = (totalDealerNet - totalCost) / totalDealerNet;
        assertTotalResultValue(totalResult, quantity, totalDealerNet, totalDNAfterSurcharge, totalCost, totalMarginAfterSurcharge, totalMarginPercentage);
    }

    @Test
    public void testGetBookingByFilter_marginPercentage() throws ParseException {
        resetFilters();

        String marginPercentage = "<20% Margin";
        filters.setMarginPercentage(marginPercentage);

        Map<String, Object> result = bookingOrderService.getBookingByFilter(filters);
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertNotNull(result.get("total"));
        Assertions.assertNotNull(result.get("listBookingOrder"));

        BookingOrder totalResult = ((List<BookingOrder>) result.get("total")).get(0);
        List<BookingOrder> listResult = (List<BookingOrder>) result.get("listBookingOrder");
        Assertions.assertFalse(listResult.isEmpty());

        long quantity = 0;
        double totalDealerNet = 0.0;
        double totalDNAfterSurcharge = 0.0;
        double totalCost = 0.0;
        double totalMarginAfterSurcharge = 0.0;

        for(BookingOrder bo : listResult) {
            Assertions.assertTrue(bo.getMarginPercentageAfterSurCharge() < 0.2);

            quantity += bo.getQuantity();
            totalDealerNet += bo.getDealerNet();
            totalDNAfterSurcharge += bo.getDealerNetAfterSurCharge();
            totalCost += bo.getTotalCost();
            totalMarginAfterSurcharge += bo.getMarginAfterSurCharge();
        }
        double totalMarginPercentage = (totalDealerNet - totalCost) / totalDealerNet;
        assertTotalResultValue(totalResult, quantity, totalDealerNet, totalDNAfterSurcharge, totalCost, totalMarginAfterSurcharge, totalMarginPercentage);
    }
}
