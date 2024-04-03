package com.hysteryale.service;

import com.hysteryale.model.Booking;
import com.hysteryale.model.Product;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.model.filters.PriceVolSensitivityFilterModel;
import com.hysteryale.model.payLoad.PriceVolSensitivityPayLoad;
import com.hysteryale.service.impl.PriceVolumeSensitivityServiceImp;
import com.hysteryale.utils.CurrencyFormatUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootTest
@Slf4j
public class PriceVolumeSensitivityServiceTest {

    PriceVolSensitivityFilterModel filters;

    @Resource
    private PriceVolumeSensitivityServiceImp priceVolumeSensitivityService;

    private void resetFilters() {
        FilterModel filterModel = new FilterModel(
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
                new ArrayList<>(),
                new ArrayList<>(),
                "", null);
        filters = new PriceVolSensitivityFilterModel();
        filters.setDataFilter(filterModel);

    }

    @BeforeEach
    public void setUp() {
        resetFilters();

    }

    @Test
    public void testGetPriceVolumeSensitivity_WithNotFilter() throws ParseException {
        resetFilters();
        Map<String, Object> result = priceVolumeSensitivityService.getDataByFilter(filters);
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertEquals((long) result.get("totalItems"), 8);

    }

    @Test
    public void testGetPriceVolumeSensitivity_WithOneSegment() throws ParseException {
        resetFilters();
        filters.getDataFilter().setSegments(List.of("C1 1-3.5T - Low Intensity"));
        // if filter with one semgnet -> group booking by Series -> result has 3 record
        Map<String, Object> result = priceVolumeSensitivityService.getDataByFilter(filters);
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertEquals((long) result.get("totalItems"), 3);
        List<PriceVolSensitivityPayLoad> listOrder = (ArrayList<PriceVolSensitivityPayLoad>) result.get("listOrder");
        Assertions.assertEquals(listOrder.size(), 3);
    }

    @Test
    public void testGetPriceVolumeSensitivity_WithOneSegmentAndMetaSeries() throws ParseException {
        resetFilters();
        filters.getDataFilter().setSegments(List.of("C1 1-3.5T - Low Intensity"));
        filters.getDataFilter().setMetaSeries(List.of("3C4", "3C5", "543"));

        // if filter with one semgnet -> group  booking by Series, has 2 metaSeries valid -> result has 2 record
        Map<String, Object> result = priceVolumeSensitivityService.getDataByFilter(filters);
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertEquals((long) result.get("totalItems"), 2);
        List<PriceVolSensitivityPayLoad> listOrder = (ArrayList<PriceVolSensitivityPayLoad>) result.get("listOrder");
        Assertions.assertEquals(listOrder.size(), 2);
    }

    @Test
    public void testGetPriceVolumeSensitivity_WithMetaSeries() throws ParseException {
        resetFilters();
        filters.getDataFilter().setMetaSeries(List.of("3C4", "3C5", "543"));

        // if filter with Series -> group  booking by Series, has 2 metaSeries valid -> result has 2 record
        Map<String, Object> result = priceVolumeSensitivityService.getDataByFilter(filters);
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertEquals((long) result.get("totalItems"), 2);
        List<PriceVolSensitivityPayLoad> listOrder = (ArrayList<PriceVolSensitivityPayLoad>) result.get("listOrder");
        Assertions.assertEquals(listOrder.size(), 2);
    }

    @Test
    public void testGetPriceVolumeSensitivity_WithManySegment() throws ParseException {
        resetFilters();
        filters.getDataFilter().setSegments(List.of("C1 1-3.5T - Low Intensity", "C1 1-3.5T - Standard and Premium", "C1 4-9T - Standard and Premium", "C2 - Standard and Premium"));

        // if filter with many Segment and NO MetaSeries -> group  booking by Segment, has 3 metaSeries valid -> 3 record
        Map<String, Object> result = priceVolumeSensitivityService.getDataByFilter(filters);
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertEquals((long) result.get("totalItems"), 3);
        List<PriceVolSensitivityPayLoad> listOrder = (ArrayList<PriceVolSensitivityPayLoad>) result.get("listOrder");
        Assertions.assertEquals(listOrder.size(), 3);
    }

    @Test
    public void testCalculatePriceVolSensitivity() throws ParseException {
        resetFilters();

        Booking booking = new Booking();
        booking.setDealerNetAfterSurcharge(76286.15);
        booking.setTotalCost(63198.22290412977);
        booking.setQuantity(5);
        booking.setSeries("A3C4");
        Product product = new Product();
        product.setSegment("C1 1-3.5T - Standard and Premium");
        booking.setProduct(product);

        double discountPercent = 0.01;
        boolean withMarginVolumeRecovery = true;
        PriceVolSensitivityPayLoad result = priceVolumeSensitivityService.calculatePriceVolSensitivity(List.of(booking), discountPercent, withMarginVolumeRecovery).get(0);
        Assertions.assertEquals(result.getVolume(), 5);
        Assertions.assertEquals(result.getSegment(), "C1 1-3.5T - Standard and Premium");
        Assertions.assertEquals(result.getSeries(), "A3C4");
        Assertions.assertEquals(CurrencyFormatUtils.formatDoubleValue(result.getRevenue(), CurrencyFormatUtils.decimalFormatThreeDigits), CurrencyFormatUtils.formatDoubleValue(76286.15000000001, CurrencyFormatUtils.decimalFormatThreeDigits));
        Assertions.assertEquals(CurrencyFormatUtils.formatDoubleValue(result.getCOGS(), CurrencyFormatUtils.decimalFormatThreeDigits), CurrencyFormatUtils.formatDoubleValue(63198.22290412977, CurrencyFormatUtils.decimalFormatThreeDigits));
        Assertions.assertEquals(CurrencyFormatUtils.formatDoubleValue(result.getMarginPercent(), CurrencyFormatUtils.decimalFormatThreeDigits), CurrencyFormatUtils.formatDoubleValue(0.1716, CurrencyFormatUtils.decimalFormatThreeDigits));
        Assertions.assertEquals(result.getDiscountPercent(), 0.01);
        Assertions.assertEquals(CurrencyFormatUtils.formatDoubleValue(result.getNewDN(), CurrencyFormatUtils.decimalFormatFourDigits),
                CurrencyFormatUtils.formatDoubleValue(15104.657700000002, CurrencyFormatUtils.decimalFormatFourDigits));
        Assertions.assertEquals(CurrencyFormatUtils.formatDoubleValue(result.getNewDN(), CurrencyFormatUtils.decimalFormatFourDigits),
                CurrencyFormatUtils.formatDoubleValue(15104.657700000002, CurrencyFormatUtils.decimalFormatFourDigits));
        Assertions.assertEquals(CurrencyFormatUtils.formatDoubleValue(result.getNewDN(), CurrencyFormatUtils.decimalFormatFourDigits),
                CurrencyFormatUtils.formatDoubleValue(15104.657700000002, CurrencyFormatUtils.decimalFormatFourDigits));

        Assertions.assertEquals(result.getUnitVolumeOffset(), 1);
        Assertions.assertEquals(result.getNewVolume(), 6);
        Assertions.assertEquals(CurrencyFormatUtils.formatDoubleValue(result.getNewRevenue(), CurrencyFormatUtils.decimalFormatFourDigits),
                CurrencyFormatUtils.formatDoubleValue(91390.8077, CurrencyFormatUtils.decimalFormatFourDigits));
        Assertions.assertEquals(CurrencyFormatUtils.formatDoubleValue(result.getNewCOGS(), CurrencyFormatUtils.decimalFormatFourDigits),
                CurrencyFormatUtils.formatDoubleValue(75837.86748495573, CurrencyFormatUtils.decimalFormatFourDigits));
        Assertions.assertEquals(CurrencyFormatUtils.formatDoubleValue(result.getNewMargin(), CurrencyFormatUtils.decimalFormatFourDigits),
                CurrencyFormatUtils.formatDoubleValue(15552.940215044277, CurrencyFormatUtils.decimalFormatFourDigits));
        Assertions.assertEquals(CurrencyFormatUtils.formatDoubleValue(result.getNewMarginPercent(), CurrencyFormatUtils.decimalFormatFourDigits),
                CurrencyFormatUtils.formatDoubleValue(0.1701805751197478, CurrencyFormatUtils.decimalFormatFourDigits));
    }

}
