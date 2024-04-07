package com.hysteryale.service;

import com.hysteryale.model.Booking;
import com.hysteryale.model.Shipment;
import com.hysteryale.model.TrendData;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.utils.CurrencyFormatUtils;
import com.hysteryale.utils.DateUtils;
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

@SpringBootTest
@Slf4j
@SuppressWarnings("unchecked")
public class TrendsServiceTest {
    @Resource
    TrendsService trendsService;
    @Resource
    BookingService bookingService;
    @Resource
    ShipmentService shipmentService;
    FilterModel filters;

    @BeforeEach
    public void setUp() {
        resetFilters();
    }

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
                "",
                "",
                2023,
                1500,
                1,
                "",
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                "",
                null);
    }

    private void assertCostValue(List<TrendData> bookingData, List<TrendData> shipmentData) throws ParseException {
        for(TrendData data : bookingData) {
            filters.setFromDate("2023-0" + data.getMonth() + "-01");
            int daysOfMonth = DateUtils.getMonth(data.getMonth()).maxLength();
            if(data.getMonth() == 2)
                daysOfMonth = DateUtils.getMonth(data.getMonth()).maxLength() -1;
            filters.setToDate("2023-0" + data.getMonth() + "-" + daysOfMonth);

            List<Booking> bookingOrdersList = (List<Booking>) bookingService.getBookingByFilter(filters).get("listBookingOrder");
            double totalCost = 0.0;
            double marginPercentage = 0.0;

            for(Booking bo : bookingOrdersList) {
                totalCost += bo.getTotalCost();
                marginPercentage += bo.getMarginPercentageAfterSurcharge();
            }
            totalCost = totalCost / bookingOrdersList.size();
            marginPercentage = marginPercentage / bookingOrdersList.size();

            Assertions.assertEquals(
                    CurrencyFormatUtils.formatDoubleValue(totalCost, CurrencyFormatUtils.decimalFormatFourDigits),
                    CurrencyFormatUtils.formatDoubleValue(data.getCost(), CurrencyFormatUtils.decimalFormatFourDigits));
            Assertions.assertEquals(
                    CurrencyFormatUtils.formatDoubleValue(marginPercentage, CurrencyFormatUtils.decimalFormatTwoDigits),
                    CurrencyFormatUtils.formatDoubleValue(data.getMarginPercentage(), CurrencyFormatUtils.decimalFormatTwoDigits));
        }

        for(TrendData data : shipmentData) {
            filters.setFromDate("2023-0" + data.getMonth() + "-01");
            filters.setToDate("2023-0" + data.getMonth() + "-" + DateUtils.getMonth(data.getMonth()).maxLength());

            List<Shipment> shipmentList = (List<Shipment>) shipmentService.getShipmentByFilter(filters).get("listShipment");
            double totalCost = 0.0;
            double marginPercentage = 0.0;

            for(Shipment sm : shipmentList) {
                totalCost += sm.getTotalCost();
                marginPercentage += sm.getMarginPercentageAfterSurcharge();
            }
            totalCost = totalCost / shipmentList.size();
            marginPercentage = marginPercentage / shipmentList.size();

            Assertions.assertEquals(
                    CurrencyFormatUtils.formatDoubleValue(totalCost, CurrencyFormatUtils.decimalFormatFourDigits),
                    CurrencyFormatUtils.formatDoubleValue(data.getCost(), CurrencyFormatUtils.decimalFormatFourDigits));
            Assertions.assertEquals(
                    CurrencyFormatUtils.formatDoubleValue(marginPercentage, CurrencyFormatUtils.decimalFormatTwoDigits),
                    CurrencyFormatUtils.formatDoubleValue(data.getMarginPercentage(), CurrencyFormatUtils.decimalFormatTwoDigits));
        }
    }

    private void assertDealerNetValue(List<TrendData> bookingData, List<TrendData> shipmentData) throws ParseException {
        for(TrendData data : bookingData) {
            filters.setFromDate("2023-0" + data.getMonth() + "-01");
            int daysOfMonth = DateUtils.getMonth(data.getMonth()).maxLength();
            if(data.getMonth() == 2)
                daysOfMonth = DateUtils.getMonth(data.getMonth()).maxLength() -1;
            filters.setToDate("2023-0" + data.getMonth() + "-" + daysOfMonth);

            List<Booking> bookingOrdersList = (List<Booking>) bookingService.getBookingByFilter(filters).get("listBookingOrder");
            double dealerNet = 0.0;
            double marginPercentage = 0.0;

            for(Booking bo : bookingOrdersList) {
                dealerNet += bo.getDealerNet();
                marginPercentage += bo.getMarginPercentageAfterSurcharge();
            }
            dealerNet = dealerNet / bookingOrdersList.size();
            marginPercentage = marginPercentage / bookingOrdersList.size();

            Assertions.assertEquals(
                    CurrencyFormatUtils.formatDoubleValue(dealerNet, CurrencyFormatUtils.decimalFormatTwoDigits),
                    CurrencyFormatUtils.formatDoubleValue(data.getCost(), CurrencyFormatUtils.decimalFormatTwoDigits));
            Assertions.assertEquals(
                    CurrencyFormatUtils.formatDoubleValue(marginPercentage, CurrencyFormatUtils.decimalFormatTwoDigits),
                    CurrencyFormatUtils.formatDoubleValue(data.getMarginPercentage(), CurrencyFormatUtils.decimalFormatTwoDigits));
        }

        for(TrendData data : shipmentData) {
            filters.setFromDate("2023-0" + data.getMonth() + "-01");
            filters.setToDate("2023-0" + data.getMonth() + "-" + DateUtils.getMonth(data.getMonth()).maxLength());

            List<Shipment> shipmentList = (List<Shipment>) shipmentService.getShipmentByFilter(filters).get("listShipment");
            double dealerNet = 0.0;
            double marginPercentage = 0.0;

            for(Shipment sm : shipmentList) {
                dealerNet += sm.getDealerNet();
                marginPercentage += sm.getMarginPercentageAfterSurcharge();
            }
            dealerNet = dealerNet / shipmentList.size();
            marginPercentage = marginPercentage / shipmentList.size();

            Assertions.assertEquals(
                    CurrencyFormatUtils.formatDoubleValue(dealerNet, CurrencyFormatUtils.decimalFormatFourDigits),
                    CurrencyFormatUtils.formatDoubleValue(data.getCost(), CurrencyFormatUtils.decimalFormatFourDigits));
            Assertions.assertEquals(
                    CurrencyFormatUtils.formatDoubleValue(marginPercentage, CurrencyFormatUtils.decimalFormatTwoDigits),
                    CurrencyFormatUtils.formatDoubleValue(data.getMarginPercentage(), CurrencyFormatUtils.decimalFormatTwoDigits));
        }
    }

    @Test
    public void testGetMarginVsCostData_region() throws ParseException {
        resetFilters();

        String region = "Asia";
        filters.setRegions(Collections.singletonList(region));

        Map<String, List<TrendData>> result = trendsService.getMarginVsCostData(filters);
        Assertions.assertNotNull(result.get("bookingData"));
        Assertions.assertNotNull(result.get("shipmentData"));

        List<TrendData> bookingData = result.get("bookingData");
        List<TrendData> shipmentData = result.get("shipmentData");

        assertCostValue(bookingData, shipmentData);
    }

    @Test
    public void testGetMarginVsCostData_plant() throws ParseException {
        resetFilters();

        String plant = "Ruyi";
        filters.setPlants(Collections.singletonList(plant));

        // Margin vs Cost data
        Map<String, List<TrendData>> result = trendsService.getMarginVsCostData(filters);
        Assertions.assertNotNull(result.get("bookingData"));
        Assertions.assertNotNull(result.get("shipmentData"));

        List<TrendData> bookingData = result.get("bookingData");
        List<TrendData> shipmentData = result.get("shipmentData");
        assertCostValue(bookingData, shipmentData);

        // Margin vs DN data
        result = trendsService.getMarginVsDNData(filters);
        Assertions.assertNotNull(result.get("bookingData"));
        Assertions.assertNotNull(result.get("shipmentData"));

        bookingData = result.get("bookingData");
        shipmentData = result.get("shipmentData");
        assertDealerNetValue(bookingData, shipmentData);
    }

    @Test
    public void testGetMarginVsCostData_metaSeries() throws ParseException {
        resetFilters();

        String metaSeries = "3C9";
        filters.setMetaSeries(Collections.singletonList(metaSeries));

        // Margin vs Cost data
        Map<String, List<TrendData>> result = trendsService.getMarginVsCostData(filters);
        Assertions.assertNotNull(result.get("bookingData"));
        Assertions.assertNotNull(result.get("shipmentData"));

        List<TrendData> bookingData = result.get("bookingData");
        List<TrendData> shipmentData = result.get("shipmentData");
        assertCostValue(bookingData, shipmentData);

        // Margin vs DN data
        result = trendsService.getMarginVsDNData(filters);
        Assertions.assertNotNull(result.get("bookingData"));
        Assertions.assertNotNull(result.get("shipmentData"));

        bookingData = result.get("bookingData");
        shipmentData = result.get("shipmentData");
        assertDealerNetValue(bookingData, shipmentData);
    }

    @Test
    public void testGetMarginVsCostData_class() throws ParseException {
        resetFilters();

        String clazz = "Class 3";
        filters.setClasses(Collections.singletonList(clazz));

        // Margin vs Cost data
        Map<String, List<TrendData>> result = trendsService.getMarginVsCostData(filters);
        Assertions.assertNotNull(result.get("bookingData"));
        Assertions.assertNotNull(result.get("shipmentData"));

        List<TrendData> bookingData = result.get("bookingData");
        List<TrendData> shipmentData = result.get("shipmentData");
        assertCostValue(bookingData, shipmentData);

        // Margin vs DN data
        result = trendsService.getMarginVsDNData(filters);
        Assertions.assertNotNull(result.get("bookingData"));
        Assertions.assertNotNull(result.get("shipmentData"));

        bookingData = result.get("bookingData");
        shipmentData = result.get("shipmentData");
        assertDealerNetValue(bookingData, shipmentData);
    }

    @Test
    public void testGetMarginVsCostData_model() throws ParseException {
        resetFilters();

        String modelCode = "PC1.5";
        filters.setModels(Collections.singletonList(modelCode));

        // Margin vs Cost data
        Map<String, List<TrendData>> result = trendsService.getMarginVsCostData(filters);
        Assertions.assertNotNull(result.get("bookingData"));
        Assertions.assertNotNull(result.get("shipmentData"));

        List<TrendData> bookingData = result.get("bookingData");
        List<TrendData> shipmentData = result.get("shipmentData");
        assertCostValue(bookingData, shipmentData);

        // Margin vs DN data
        result = trendsService.getMarginVsDNData(filters);
        Assertions.assertNotNull(result.get("bookingData"));
        Assertions.assertNotNull(result.get("shipmentData"));

        bookingData = result.get("bookingData");
        shipmentData = result.get("shipmentData");
        assertDealerNetValue(bookingData, shipmentData);
    }

    @Test
    public void testGetMarginVsCostData_multiFilters() throws ParseException {
        resetFilters();

        // Margin vs Cost data
        Map<String, List<TrendData>> result = trendsService.getMarginVsCostData(filters);
        Assertions.assertNotNull(result.get("bookingData"));
        Assertions.assertNotNull(result.get("shipmentData"));

        List<TrendData> bookingData = result.get("bookingData");
        List<TrendData> shipmentData = result.get("shipmentData");
        assertCostValue(bookingData, shipmentData);

        // Margin vs DN data
        result = trendsService.getMarginVsDNData(filters);
        Assertions.assertNotNull(result.get("bookingData"));
        Assertions.assertNotNull(result.get("shipmentData"));

        bookingData = result.get("bookingData");
        shipmentData = result.get("shipmentData");
        assertDealerNetValue(bookingData, shipmentData);
    }
}
