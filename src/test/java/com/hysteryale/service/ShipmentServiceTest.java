package com.hysteryale.service;

import com.hysteryale.model.Shipment;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.utils.CurrencyFormatUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@SpringBootTest
@SuppressWarnings("unchecked")
@Slf4j
public class ShipmentServiceTest {
    @Resource
    ShipmentService shipmentService;
    FilterModel filters;
    @Resource
    ExchangeRateService exchangeRateService;
    double rate;

    @BeforeEach
    public void setUp() {
        resetFilters();
        rate = exchangeRateService.getNearestExchangeRate("AUD", "USD").getRate();
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
                "2023-10-01",
                "2023-10-10",
                null,
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

    private void assertTotalValue(Shipment totalResult, long totalQuantity, double totalDealerNet,
                                  double totalDNAfterSurcharge, double totalCost, double totalNetRevenue,
                                  double totalMarginAfterSurcharge, double totalMarginPercentageAfterSurcharge) {
        Assertions.assertEquals(
                CurrencyFormatUtils.formatDoubleValue(totalQuantity, CurrencyFormatUtils.decimalFormatFourDigits),
                CurrencyFormatUtils.formatDoubleValue(totalResult.getQuantity(), CurrencyFormatUtils.decimalFormatFourDigits)
        );
        Assertions.assertEquals(
                CurrencyFormatUtils.formatDoubleValue(totalDealerNet, CurrencyFormatUtils.decimalFormatFourDigits),
                CurrencyFormatUtils.formatDoubleValue(totalResult.getDealerNet(), CurrencyFormatUtils.decimalFormatFourDigits)
        );
        Assertions.assertEquals(
                CurrencyFormatUtils.formatDoubleValue(totalDNAfterSurcharge, CurrencyFormatUtils.decimalFormatFourDigits),
                CurrencyFormatUtils.formatDoubleValue(totalResult.getDealerNetAfterSurcharge(), CurrencyFormatUtils.decimalFormatFourDigits)
        );
        Assertions.assertEquals(
                CurrencyFormatUtils.formatDoubleValue(totalCost, CurrencyFormatUtils.decimalFormatFourDigits),
                CurrencyFormatUtils.formatDoubleValue(totalResult.getTotalCost(), CurrencyFormatUtils.decimalFormatFourDigits)
        );
        Assertions.assertEquals(
                CurrencyFormatUtils.formatDoubleValue(totalNetRevenue, CurrencyFormatUtils.decimalFormatFourDigits),
                CurrencyFormatUtils.formatDoubleValue(totalResult.getNetRevenue(), CurrencyFormatUtils.decimalFormatFourDigits)
        );
        Assertions.assertEquals(
                CurrencyFormatUtils.formatDoubleValue(totalMarginAfterSurcharge, CurrencyFormatUtils.decimalFormatFourDigits),
                CurrencyFormatUtils.formatDoubleValue(totalResult.getMarginAfterSurcharge(), CurrencyFormatUtils.decimalFormatFourDigits)
        );
        Assertions.assertEquals(
                CurrencyFormatUtils.formatDoubleValue(totalMarginPercentageAfterSurcharge, CurrencyFormatUtils.decimalFormatTwoDigits),
                CurrencyFormatUtils.formatDoubleValue(totalResult.getMarginPercentageAfterSurcharge(), CurrencyFormatUtils.decimalFormatTwoDigits)
        );
    }

    @Test
    public void testGetShipmentByFilter_orderNumber() throws ParseException {
        resetFilters();

        String orderNumber = "H08833";
        filters.setOrderNo(orderNumber);

        Map<String, Object> result = shipmentService.getShipmentByFilter(filters);
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertNotNull(result.get("total"));
        Assertions.assertNotNull(result.get("listShipment"));
        Assertions.assertNotNull(result.get("listExchangeRate"));

        List<Shipment> shipmentList = (List<Shipment>) result.get("listShipment");
        Shipment totalResult = ((List<Shipment>) result.get("total")).get(0);
        Assertions.assertFalse(shipmentList.isEmpty());

        long totalQuantity = 0;
        double totalDealerNet = 0.0;
        double totalDNAfterSurcharge = 0.0;
        double totalCost = 0.0;
        double totalNetRevenue = 0.0;
        double totalMarginAfterSurcharge = 0.0;

        for(Shipment sm : shipmentList) {
            Assertions.assertEquals(orderNumber, sm.getOrderNo());

            totalQuantity += sm.getQuantity();
            totalDealerNet += sm.getDealerNet();
            totalDNAfterSurcharge += sm.getDealerNetAfterSurcharge();
            totalCost += sm.getTotalCost();
            totalNetRevenue += sm.getNetRevenue();
            totalMarginAfterSurcharge += sm.getMarginAfterSurcharge();
        }
        double totalMarginPercentageAfterSurcharge = (totalDNAfterSurcharge - totalCost) / totalDNAfterSurcharge;
        assertTotalValue(totalResult, totalQuantity, totalDealerNet, totalDNAfterSurcharge, totalCost, totalNetRevenue, totalMarginAfterSurcharge, totalMarginPercentageAfterSurcharge);
    }

    @Test
    public void testGetShipmentByFilter_region() throws ParseException {
        resetFilters();

        String region = "Asia";
        filters.setRegions(Collections.singletonList(region));

        Map<String, Object> result = shipmentService.getShipmentByFilter(filters);
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertNotNull(result.get("total"));
        Assertions.assertNotNull(result.get("listShipment"));
        Assertions.assertNotNull(result.get("listExchangeRate"));

        List<Shipment> shipmentList = (List<Shipment>) result.get("listShipment");
        Shipment totalResult = ((List<Shipment>) result.get("total")).get(0);
        Assertions.assertFalse(shipmentList.isEmpty());

        long totalQuantity = 0;
        double totalDealerNet = 0.0;
        double totalDNAfterSurcharge = 0.0;
        double totalCost = 0.0;
        double totalNetRevenue = 0.0;
        double totalMarginAfterSurcharge = 0.0;

        for(Shipment sm : shipmentList) {
            Assertions.assertEquals(region, sm.getCountry().getRegion().getRegionName());

            totalQuantity += sm.getQuantity();
            if(sm.getCurrency().getCurrency().equals("AUD")) {
                totalDealerNet += sm.getDealerNet() * rate;
                totalDNAfterSurcharge += sm.getDealerNetAfterSurcharge() * rate;
                totalCost += sm.getTotalCost() * rate;
                totalNetRevenue += sm.getNetRevenue() * rate;
                totalMarginAfterSurcharge += sm.getMarginAfterSurcharge() * rate;
            }
            else {
                totalDealerNet += sm.getDealerNet();
                totalDNAfterSurcharge += sm.getDealerNetAfterSurcharge();
                totalCost += sm.getTotalCost();
                totalNetRevenue += sm.getNetRevenue();
                totalMarginAfterSurcharge += sm.getMarginAfterSurcharge();
            }
        }
        double totalMarginPercentageAfterSurcharge = (totalDNAfterSurcharge - totalCost) / totalDNAfterSurcharge;
        assertTotalValue(totalResult, totalQuantity, totalDealerNet, totalDNAfterSurcharge, totalCost, totalNetRevenue, totalMarginAfterSurcharge, totalMarginPercentageAfterSurcharge);
    }

    @Test
    public void testGetShipmentByFilter_plant() throws ParseException {
        resetFilters();

        String plant = "Ruyi";
        filters.setPlants(Collections.singletonList(plant));

        Map<String, Object> result = shipmentService.getShipmentByFilter(filters);
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertNotNull(result.get("total"));
        Assertions.assertNotNull(result.get("listShipment"));
        Assertions.assertNotNull(result.get("listExchangeRate"));

        List<Shipment> shipmentList = (List<Shipment>) result.get("listShipment");
        Shipment totalResult = ((List<Shipment>) result.get("total")).get(0);
        Assertions.assertFalse(shipmentList.isEmpty());

        long totalQuantity = 0;
        double totalDealerNet = 0.0;
        double totalDNAfterSurcharge = 0.0;
        double totalCost = 0.0;
        double totalNetRevenue = 0.0;
        double totalMarginAfterSurcharge = 0.0;

        for(Shipment sm : shipmentList) {
            Assertions.assertEquals(plant, sm.getProduct().getPlant());

            totalQuantity += sm.getQuantity();
            if(sm.getCurrency().getCurrency().equals("AUD")) {
                totalDealerNet += sm.getDealerNet() * rate;
                totalDNAfterSurcharge += sm.getDealerNetAfterSurcharge() * rate;
                totalCost += sm.getTotalCost() * rate;
                totalNetRevenue += sm.getNetRevenue() * rate;
                totalMarginAfterSurcharge += sm.getMarginAfterSurcharge() * rate;
            }
            else {
                totalDealerNet += sm.getDealerNet();
                totalDNAfterSurcharge += sm.getDealerNetAfterSurcharge();
                totalCost += sm.getTotalCost();
                totalNetRevenue += sm.getNetRevenue();
                totalMarginAfterSurcharge += sm.getMarginAfterSurcharge();
            }
        }
        double totalMarginPercentageAfterSurcharge = (totalDNAfterSurcharge - totalCost) / totalDNAfterSurcharge;
        assertTotalValue(totalResult, totalQuantity, totalDealerNet, totalDNAfterSurcharge, totalCost, totalNetRevenue, totalMarginAfterSurcharge, totalMarginPercentageAfterSurcharge);
    }

    @Test
    public void testGetShipmentByFilter_metaSeries() throws ParseException {
        resetFilters();

        String metaSeries = "3C9";
        filters.setMetaSeries(Collections.singletonList(metaSeries));

        Map<String, Object> result = shipmentService.getShipmentByFilter(filters);
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertNotNull(result.get("total"));
        Assertions.assertNotNull(result.get("listShipment"));
        Assertions.assertNotNull(result.get("listExchangeRate"));

        List<Shipment> shipmentList = (List<Shipment>) result.get("listShipment");
        Shipment totalResult = ((List<Shipment>) result.get("total")).get(0);
        Assertions.assertFalse(shipmentList.isEmpty());

        long totalQuantity = 0;
        double totalDealerNet = 0.0;
        double totalDNAfterSurcharge = 0.0;
        double totalCost = 0.0;
        double totalNetRevenue = 0.0;
        double totalMarginAfterSurcharge = 0.0;

        for(Shipment sm : shipmentList) {
            Assertions.assertEquals(metaSeries, sm.getSeries().substring(1));

            totalQuantity += sm.getQuantity();
            if(sm.getCurrency().getCurrency().equals("AUD")) {
                totalDealerNet += sm.getDealerNet() * rate;
                totalDNAfterSurcharge += sm.getDealerNetAfterSurcharge() * rate;
                totalCost += sm.getTotalCost() * rate;
                totalNetRevenue += sm.getNetRevenue() * rate;
                totalMarginAfterSurcharge += sm.getMarginAfterSurcharge() * rate;
            }
            else {
                totalDealerNet += sm.getDealerNet();
                totalDNAfterSurcharge += sm.getDealerNetAfterSurcharge();
                totalCost += sm.getTotalCost();
                totalNetRevenue += sm.getNetRevenue();
                totalMarginAfterSurcharge += sm.getMarginAfterSurcharge();
            }
        }
        double totalMarginPercentageAfterSurcharge = (totalDNAfterSurcharge - totalCost) / totalDNAfterSurcharge;
        assertTotalValue(totalResult, totalQuantity, totalDealerNet, totalDNAfterSurcharge, totalCost, totalNetRevenue, totalMarginAfterSurcharge, totalMarginPercentageAfterSurcharge);
    }

    @Test
    public void testGetShipmentByFilter_class() throws ParseException {
        resetFilters();

        String clazz = "Class 3";
        filters.setClasses(Collections.singletonList(clazz));

        Map<String, Object> result = shipmentService.getShipmentByFilter(filters);
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertNotNull(result.get("total"));
        Assertions.assertNotNull(result.get("listShipment"));
        Assertions.assertNotNull(result.get("listExchangeRate"));

        List<Shipment> shipmentList = (List<Shipment>) result.get("listShipment");
        Shipment totalResult = ((List<Shipment>) result.get("total")).get(0);
        Assertions.assertFalse(shipmentList.isEmpty());

        long totalQuantity = 0;
        double totalDealerNet = 0.0;
        double totalDNAfterSurcharge = 0.0;
        double totalCost = 0.0;
        double totalNetRevenue = 0.0;
        double totalMarginAfterSurcharge = 0.0;

        for(Shipment sm : shipmentList) {
            Assertions.assertEquals(clazz, sm.getProduct().getClazz().getClazzName());

            totalQuantity += sm.getQuantity();
            if(sm.getCurrency().getCurrency().equals("AUD")) {
                totalDealerNet += sm.getDealerNet() * rate;
                totalDNAfterSurcharge += sm.getDealerNetAfterSurcharge() * rate;
                totalCost += sm.getTotalCost() * rate;
                totalNetRevenue += sm.getNetRevenue() * rate;
                totalMarginAfterSurcharge += sm.getMarginAfterSurcharge() * rate;
            }
            else {
                totalDealerNet += sm.getDealerNet();
                totalDNAfterSurcharge += sm.getDealerNetAfterSurcharge();
                totalCost += sm.getTotalCost();
                totalNetRevenue += sm.getNetRevenue();
                totalMarginAfterSurcharge += sm.getMarginAfterSurcharge();
            }
        }
        double totalMarginPercentageAfterSurcharge = (totalDNAfterSurcharge - totalCost) / totalDNAfterSurcharge;
        assertTotalValue(totalResult, totalQuantity, totalDealerNet, totalDNAfterSurcharge, totalCost, totalNetRevenue, totalMarginAfterSurcharge, totalMarginPercentageAfterSurcharge);
    }

    @Test
    public void testGetShipmentByFilter_dealerName() throws ParseException {
        resetFilters();

        String dealerName = "PACIFIC FEDERAL";
        filters.setDealers(Collections.singletonList(dealerName));

        Map<String, Object> result = shipmentService.getShipmentByFilter(filters);
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertNotNull(result.get("total"));
        Assertions.assertNotNull(result.get("listShipment"));
        Assertions.assertNotNull(result.get("listExchangeRate"));

        List<Shipment> shipmentList = (List<Shipment>) result.get("listShipment");
        Shipment totalResult = ((List<Shipment>) result.get("total")).get(0);
        Assertions.assertFalse(shipmentList.isEmpty());

        long totalQuantity = 0;
        double totalDealerNet = 0.0;
        double totalDNAfterSurcharge = 0.0;
        double totalCost = 0.0;
        double totalNetRevenue = 0.0;
        double totalMarginAfterSurcharge = 0.0;

        for(Shipment sm : shipmentList) {
            Assertions.assertEquals(dealerName, sm.getDealer().getName());

            totalQuantity += sm.getQuantity();
            if(sm.getCurrency().getCurrency().equals("AUD")) {
                totalDealerNet += sm.getDealerNet() * rate;
                totalDNAfterSurcharge += sm.getDealerNetAfterSurcharge() * rate;
                totalCost += sm.getTotalCost() * rate;
                totalNetRevenue += sm.getNetRevenue() * rate;
                totalMarginAfterSurcharge += sm.getMarginAfterSurcharge() * rate;
            }
            else {
                totalDealerNet += sm.getDealerNet();
                totalDNAfterSurcharge += sm.getDealerNetAfterSurcharge();
                totalCost += sm.getTotalCost();
                totalNetRevenue += sm.getNetRevenue();
                totalMarginAfterSurcharge += sm.getMarginAfterSurcharge();
            }
        }
        double totalMarginPercentageAfterSurcharge = (totalDNAfterSurcharge - totalCost) / totalDNAfterSurcharge;
        assertTotalValue(totalResult, totalQuantity, totalDealerNet, totalDNAfterSurcharge, totalCost, totalNetRevenue, totalMarginAfterSurcharge, totalMarginPercentageAfterSurcharge);
    }

    @Test
    public void testGetShipmentByFilter_modelCode() throws ParseException {
        resetFilters();

        String modelCode = "T3.0UT";
        filters.setModels(Collections.singletonList(modelCode));

        Map<String, Object> result = shipmentService.getShipmentByFilter(filters);
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertNotNull(result.get("total"));
        Assertions.assertNotNull(result.get("listShipment"));
        Assertions.assertNotNull(result.get("listExchangeRate"));

        List<Shipment> shipmentList = (List<Shipment>) result.get("listShipment");
        Shipment totalResult = ((List<Shipment>) result.get("total")).get(0);
        Assertions.assertFalse(shipmentList.isEmpty());

        long totalQuantity = 0;
        double totalDealerNet = 0.0;
        double totalDNAfterSurcharge = 0.0;
        double totalCost = 0.0;
        double totalNetRevenue = 0.0;
        double totalMarginAfterSurcharge = 0.0;

        for(Shipment sm : shipmentList) {
            Assertions.assertEquals(modelCode, sm.getProduct().getModelCode());

            totalQuantity += sm.getQuantity();
            if(sm.getCurrency().getCurrency().equals("AUD")) {
                totalDealerNet += sm.getDealerNet() * rate;
                totalDNAfterSurcharge += sm.getDealerNetAfterSurcharge() * rate;
                totalCost += sm.getTotalCost() * rate;
                totalNetRevenue += sm.getNetRevenue() * rate;
                totalMarginAfterSurcharge += sm.getMarginAfterSurcharge() * rate;
            }
            else {
                totalDealerNet += sm.getDealerNet();
                totalDNAfterSurcharge += sm.getDealerNetAfterSurcharge();
                totalCost += sm.getTotalCost();
                totalNetRevenue += sm.getNetRevenue();
                totalMarginAfterSurcharge += sm.getMarginAfterSurcharge();
            }
        }
        double totalMarginPercentageAfterSurcharge = (totalDNAfterSurcharge - totalCost) / totalDNAfterSurcharge;
        assertTotalValue(totalResult, totalQuantity, totalDealerNet, totalDNAfterSurcharge, totalCost, totalNetRevenue, totalMarginAfterSurcharge, totalMarginPercentageAfterSurcharge);
    }

    @Test
    public void testGetShipmentByFilter_marginPercentage() throws ParseException {
        resetFilters();

        String marginPercentage = "<10% Margin";
        filters.setMarginPercentage(marginPercentage);

        Map<String, Object> result = shipmentService.getShipmentByFilter(filters);
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertNotNull(result.get("total"));
        Assertions.assertNotNull(result.get("listShipment"));
        Assertions.assertNotNull(result.get("listExchangeRate"));

        List<Shipment> shipmentList = (List<Shipment>) result.get("listShipment");
        Shipment totalResult = ((List<Shipment>) result.get("total")).get(0);
        Assertions.assertFalse(shipmentList.isEmpty());

        long totalQuantity = 0;
        double totalDealerNet = 0.0;
        double totalDNAfterSurcharge = 0.0;
        double totalCost = 0.0;
        double totalNetRevenue = 0.0;
        double totalMarginAfterSurcharge = 0.0;

        for(Shipment sm : shipmentList) {
            Assertions.assertTrue(sm.getMarginPercentageAfterSurcharge() < 0.1);

            totalQuantity += sm.getQuantity();
            if(sm.getCurrency().getCurrency().equals("AUD")) {
                totalDealerNet += sm.getDealerNet() * rate;
                totalDNAfterSurcharge += sm.getDealerNetAfterSurcharge() * rate;
                totalCost += sm.getTotalCost() * rate;
                totalNetRevenue += sm.getNetRevenue() * rate;
                totalMarginAfterSurcharge += sm.getMarginAfterSurcharge() * rate;
            }
            else {
                totalDealerNet += sm.getDealerNet();
                totalDNAfterSurcharge += sm.getDealerNetAfterSurcharge();
                totalCost += sm.getTotalCost();
                totalNetRevenue += sm.getNetRevenue();
                totalMarginAfterSurcharge += sm.getMarginAfterSurcharge();
            }
        }
        double totalMarginPercentageAfterSurcharge = (totalDNAfterSurcharge - totalCost) / totalDNAfterSurcharge;
        assertTotalValue(totalResult, totalQuantity, totalDealerNet, totalDNAfterSurcharge, totalCost, totalNetRevenue, totalMarginAfterSurcharge, totalMarginPercentageAfterSurcharge);
    }

    @Test
    public void testGetShipmentByFilter_date() throws ParseException {
        resetFilters();

        String fromDate = "2023-05-01";
        String toDate = "2023-05-10";
        filters.setFromDate(fromDate);
        filters.setToDate(toDate);

        Map<String, Object> result = shipmentService.getShipmentByFilter(filters);
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertNotNull(result.get("total"));
        Assertions.assertNotNull(result.get("listShipment"));
        Assertions.assertNotNull(result.get("listExchangeRate"));

        List<Shipment> shipmentList = (List<Shipment>) result.get("listShipment");
        Shipment totalResult = ((List<Shipment>) result.get("total")).get(0);
        Assertions.assertFalse(shipmentList.isEmpty());

        long totalQuantity = 0;
        double totalDealerNet = 0.0;
        double totalDNAfterSurcharge = 0.0;
        double totalCost = 0.0;
        double totalNetRevenue = 0.0;
        double totalMarginAfterSurcharge = 0.0;

        for(Shipment sm : shipmentList) {
            Assertions.assertTrue(sm.getDate().isAfter(LocalDate.of(2023, Month.APRIL, 30)));
            Assertions.assertTrue(sm.getDate().isBefore(LocalDate.of(2023, Month.JUNE, 1)));

            totalQuantity += sm.getQuantity();
            if(sm.getCurrency().getCurrency().equals("AUD")) {
                totalDealerNet += sm.getDealerNet() * rate;
                totalDNAfterSurcharge += sm.getDealerNetAfterSurcharge() * rate;
                totalCost += sm.getTotalCost() * rate;
                totalNetRevenue += sm.getNetRevenue() * rate;
                totalMarginAfterSurcharge += sm.getMarginAfterSurcharge() * rate;
            }
            else {
                totalDealerNet += sm.getDealerNet();
                totalDNAfterSurcharge += sm.getDealerNetAfterSurcharge();
                totalCost += sm.getTotalCost();
                totalNetRevenue += sm.getNetRevenue();
                totalMarginAfterSurcharge += sm.getMarginAfterSurcharge();
            }
        }
        double totalMarginPercentageAfterSurcharge = (totalDNAfterSurcharge - totalCost) / totalDNAfterSurcharge;
        assertTotalValue(totalResult, totalQuantity, totalDealerNet, totalDNAfterSurcharge, totalCost, totalNetRevenue, totalMarginAfterSurcharge, totalMarginPercentageAfterSurcharge);
    }

    @Test
    public void testGetShipmentByOrderNo() {
        String orderNo = "F90210";

        Shipment shipment = shipmentService.getShipmentByOrderNo(orderNo);
        Assertions.assertNotNull(shipment);
        Assertions.assertEquals(orderNo, shipment.getOrderNo());
    }

    @Test
    public void testGetShipmentByOrderNo_notFoundOrderNo() {
        Shipment shipment = shipmentService.getShipmentByOrderNo("123123123");
        Assertions.assertNull(shipment);
    }

}
