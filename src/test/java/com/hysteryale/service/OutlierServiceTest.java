package com.hysteryale.service;

import com.hysteryale.model.BookingOrder;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.repository.bookingorder.BookingOrderRepository;
import com.hysteryale.utils.ConvertDataFilterUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;

import javax.annotation.Resource;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@SpringBootTest
@SuppressWarnings("unchecked")
public class OutlierServiceTest {
    @Resource
    OutlierService outlierService;
    @Resource
    BookingOrderRepository bookingOrderRepository;

    @Test
    public void testGetDataForTable() throws ParseException {
        FilterModel filters = new FilterModel();
        // Expected value
        Map<String, Object> filterMap = ConvertDataFilterUtil.loadDataFilterIntoMap(filters);
        List<BookingOrder> listOrder = bookingOrderRepository.getOrderForOutline(
                (List<String>) filterMap.get("regionFilter"), (List<String>) filterMap.get("plantFilter"), (List<String>) filterMap.get("metaSeriesFilter"),
                (List<String>) filterMap.get("classFilter"), (List<String>) filterMap.get("modelFilter"), (List<String>) filterMap.get("dealerNameFilter"),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((String) ((List) filterMap.get("marginPercentageFilter")).get(0)),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((Double) ((List) filterMap.get("marginPercentageFilter")).get(1)),
                (LocalDate) filterMap.get("fromDateFilter"), (LocalDate) filterMap.get("toDateFilter"),
                (Pageable) filterMap.get("pageable"));

        List<Integer> countAll = bookingOrderRepository.countAllForOutline(
                (List<String>) filterMap.get("regionFilter"), (List<String>) filterMap.get("plantFilter"), (List<String>) filterMap.get("metaSeriesFilter"),
                (List<String>) filterMap.get("classFilter"), (List<String>) filterMap.get("modelFilter"), (List<String>) filterMap.get("dealerNameFilter"),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((String) ((List) filterMap.get("marginPercentageFilter")).get(0)),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((Double) ((List) filterMap.get("marginPercentageFilter")).get(1)),
                (LocalDate) filterMap.get("fromDateFilter"), (LocalDate) filterMap.get("toDateFilter"));

        // Assertions
        Map<String, Object> result = outlierService.getDataForTable(filters);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(listOrder.size(), ((List<Object>) result.get("listOutlier")).size());
        Assertions.assertEquals(countAll.size(), ((Integer) result.get("totalItems")));
    }

    @Test
    public void testGetDataForChart() throws ParseException {
        FilterModel filters = new FilterModel();

        Assertions.assertDoesNotThrow(() -> outlierService.getDataForChart(filters));
        Map<String, Object> result = outlierService.getDataForChart(filters);
        Assertions.assertNotNull(result.get("chartOutliersData"));
    }

}
