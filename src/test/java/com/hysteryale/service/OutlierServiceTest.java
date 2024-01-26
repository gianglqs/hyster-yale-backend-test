package com.hysteryale.service;

import com.hysteryale.model.BookingOrder;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.utils.CurrencyFormatUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@SpringBootTest
@SuppressWarnings("unchecked")
public class OutlierServiceTest {
    @Resource
    OutlierService outlierService;
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
                null,
                1500,
                1,
                "",
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>());
    }

    private void assertResultValue(BookingOrder totalResult, long quantity, double totalCost, double totalDealerNet, double totalDNAfterSurcharge, double totalMargin) {
        Assertions.assertEquals(
                CurrencyFormatUtils.formatDoubleValue(quantity, CurrencyFormatUtils.decimalFormatFourDigits),
                CurrencyFormatUtils.formatDoubleValue(totalResult.getQuantity(), CurrencyFormatUtils.decimalFormatFourDigits)
        );
        Assertions.assertEquals(
                CurrencyFormatUtils.formatDoubleValue(totalCost, CurrencyFormatUtils.decimalFormatFourDigits),
                CurrencyFormatUtils.formatDoubleValue(totalResult.getTotalCost(), CurrencyFormatUtils.decimalFormatFourDigits)
        );
        Assertions.assertEquals(
                CurrencyFormatUtils.formatDoubleValue(totalDealerNet, CurrencyFormatUtils.decimalFormatFourDigits),
                CurrencyFormatUtils.formatDoubleValue(totalResult.getDealerNet(), CurrencyFormatUtils.decimalFormatFourDigits)
        );
        Assertions.assertEquals(
                CurrencyFormatUtils.formatDoubleValue(totalDNAfterSurcharge, CurrencyFormatUtils.decimalFormatFourDigits),
                CurrencyFormatUtils.formatDoubleValue(totalResult.getDealerNetAfterSurCharge(), CurrencyFormatUtils.decimalFormatFourDigits)
        );
        Assertions.assertEquals(
                CurrencyFormatUtils.formatDoubleValue(totalMargin, CurrencyFormatUtils.decimalFormatFourDigits),
                CurrencyFormatUtils.formatDoubleValue(totalResult.getMarginAfterSurCharge(), CurrencyFormatUtils.decimalFormatFourDigits)
        );
    }

    @Test
    public void testGetDataForTable_region() throws ParseException {
        resetFilters();

        String region = "Asia";
        filters.setRegions(Collections.singletonList(region));

        Map<String, Object> result = outlierService.getDataForTable(filters);
        Assertions.assertNotNull(result.get("total"));
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertNotNull(result.get("listOutlier"));

        BookingOrder totalResult = ((List<BookingOrder>) result.get("total")).get(0);
        List<BookingOrder> outliersList = (List<BookingOrder>) result.get("listOutlier");
        Assertions.assertFalse(outliersList.isEmpty());

        long quantity = 0;
        double totalCost = 0.0;
        double totalDealerNet = 0.0;
        double totalDNAfterSurcharge = 0.0;
        double totalMargin = 0.0;

        for(BookingOrder outlier : outliersList) {
            Assertions.assertEquals(region, outlier.getRegion().getRegion());

            quantity += outlier.getQuantity();
            totalCost += outlier.getTotalCost();
            totalDealerNet += outlier.getDealerNet();
            totalDNAfterSurcharge += outlier.getDealerNetAfterSurCharge();
            totalMargin += outlier.getMarginAfterSurCharge();
        }
        assertResultValue(totalResult, quantity, totalCost, totalDealerNet, totalDNAfterSurcharge, totalMargin);
    }

    @Test
    public void testGetDataForTable_plant() throws ParseException {
        resetFilters();

        String plant = "Ruyi";
        filters.setPlants(Collections.singletonList(plant));

        Map<String, Object> result = outlierService.getDataForTable(filters);
        Assertions.assertNotNull(result.get("total"));
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertNotNull(result.get("listOutlier"));

        BookingOrder totalResult = ((List<BookingOrder>) result.get("total")).get(0);
        List<BookingOrder> outliersList = (List<BookingOrder>) result.get("listOutlier");
        Assertions.assertFalse(outliersList.isEmpty());

        long quantity = 0;
        double totalCost = 0.0;
        double totalDealerNet = 0.0;
        double totalDNAfterSurcharge = 0.0;
        double totalMargin = 0.0;

        for(BookingOrder outlier : outliersList) {
            Assertions.assertEquals(plant, outlier.getProductDimension().getPlant());

            quantity += outlier.getQuantity();
            totalCost += outlier.getTotalCost();
            totalDealerNet += outlier.getDealerNet();
            totalDNAfterSurcharge += outlier.getDealerNetAfterSurCharge();
            totalMargin += outlier.getMarginAfterSurCharge();
        }
        assertResultValue(totalResult, quantity, totalCost, totalDealerNet, totalDNAfterSurcharge, totalMargin);
    }

    @Test
    public void testGetDataForTable_metaSeries() throws ParseException {
        resetFilters();

        String metaSeries = "3C9";
        filters.setMetaSeries(Collections.singletonList(metaSeries));

        Map<String, Object> result = outlierService.getDataForTable(filters);
        Assertions.assertNotNull(result.get("total"));
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertNotNull(result.get("listOutlier"));

        BookingOrder totalResult = ((List<BookingOrder>) result.get("total")).get(0);
        List<BookingOrder> outliersList = (List<BookingOrder>) result.get("listOutlier");
        Assertions.assertFalse(outliersList.isEmpty());

        long quantity = 0;
        double totalCost = 0.0;
        double totalDealerNet = 0.0;
        double totalDNAfterSurcharge = 0.0;
        double totalMargin = 0.0;

        for(BookingOrder outlier : outliersList) {
            Assertions.assertEquals(metaSeries, outlier.getSeries().substring(1));

            quantity += outlier.getQuantity();
            totalCost += outlier.getTotalCost();
            totalDealerNet += outlier.getDealerNet();
            totalDNAfterSurcharge += outlier.getDealerNetAfterSurCharge();
            totalMargin += outlier.getMarginAfterSurCharge();
        }
        assertResultValue(totalResult, quantity, totalCost, totalDealerNet, totalDNAfterSurcharge, totalMargin);
    }

    @Test
    public void testGetDataForTable_class() throws ParseException {
        resetFilters();

        String clazz = "Class 3";
        filters.setClasses(Collections.singletonList(clazz));

        Map<String, Object> result = outlierService.getDataForTable(filters);
        Assertions.assertNotNull(result.get("total"));
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertNotNull(result.get("listOutlier"));

        BookingOrder totalResult = ((List<BookingOrder>) result.get("total")).get(0);
        List<BookingOrder> outliersList = (List<BookingOrder>) result.get("listOutlier");
        Assertions.assertFalse(outliersList.isEmpty());

        long quantity = 0;
        double totalCost = 0.0;
        double totalDealerNet = 0.0;
        double totalDNAfterSurcharge = 0.0;
        double totalMargin = 0.0;

        for(BookingOrder outlier : outliersList) {
            Assertions.assertEquals(clazz, outlier.getProductDimension().getClazz());

            quantity += outlier.getQuantity();
            totalCost += outlier.getTotalCost();
            totalDealerNet += outlier.getDealerNet();
            totalDNAfterSurcharge += outlier.getDealerNetAfterSurCharge();
            totalMargin += outlier.getMarginAfterSurCharge();
        }
        assertResultValue(totalResult, quantity, totalCost, totalDealerNet, totalDNAfterSurcharge, totalMargin);
    }

    @Test
    public void testGetDataForTable_multipleFilters() throws ParseException {
        resetFilters();

        String region = "Asia";
        String plant = "Ruyi";
        String clazz = "Class 3";
        filters.setRegions(Collections.singletonList(region));
        filters.setPlants(Collections.singletonList(plant));
        filters.setClasses(Collections.singletonList(clazz));

        Map<String, Object> result = outlierService.getDataForTable(filters);
        Assertions.assertNotNull(result.get("total"));
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertNotNull(result.get("listOutlier"));

        BookingOrder totalResult = ((List<BookingOrder>) result.get("total")).get(0);
        List<BookingOrder> outliersList = (List<BookingOrder>) result.get("listOutlier");
        Assertions.assertFalse(outliersList.isEmpty());

        long quantity = 0;
        double totalCost = 0.0;
        double totalDealerNet = 0.0;
        double totalDNAfterSurcharge = 0.0;
        double totalMargin = 0.0;

        for(BookingOrder outlier : outliersList) {
            Assertions.assertEquals(region, outlier.getRegion().getRegion());
            Assertions.assertEquals(plant, outlier.getProductDimension().getPlant());
            Assertions.assertEquals(clazz, outlier.getProductDimension().getClazz());

            quantity += outlier.getQuantity();
            totalCost += outlier.getTotalCost();
            totalDealerNet += outlier.getDealerNet();
            totalDNAfterSurcharge += outlier.getDealerNetAfterSurCharge();
            totalMargin += outlier.getMarginAfterSurCharge();
        }
        assertResultValue(totalResult, quantity, totalCost, totalDealerNet, totalDNAfterSurcharge, totalMargin);
    }

    @Test
    public void testGetDataForChart() throws ParseException {
        FilterModel filters = new FilterModel();

        Assertions.assertDoesNotThrow(() -> outlierService.getDataForChart(filters));
        Map<String, Object> result = outlierService.getDataForChart(filters);
        Assertions.assertNotNull(result.get("chartOutliersData"));
    }

}