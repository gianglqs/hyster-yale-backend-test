package com.hysteryale.service;

import com.hysteryale.model.Booking;
import com.hysteryale.model.ChartOutlier;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.repository.BookingRepository;
import com.hysteryale.utils.ConvertDataFilterUtil;
import com.hysteryale.utils.DateUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@SuppressWarnings("unchecked")
public class OutlierService extends BasedService {

    @Resource
    BookingRepository bookingRepository;

    public Map<String, Object> getDataForTable(FilterModel filterModel) throws ParseException {
        Map<String, Object> result = new HashMap<>();
        //convert data filter
        Map<String, Object> filterMap = ConvertDataFilterUtil.loadDataFilterIntoMap(filterModel);
        List<Booking> listOrder = bookingRepository.getOrderForOutlier(
                (List<String>) filterMap.get("regionFilter"), (List<String>) filterMap.get("plantFilter"), (List<String>) filterMap.get("metaSeriesFilter"),
                (List<String>) filterMap.get("classFilter"), (List<String>) filterMap.get("modelFilter"), (List<String>) filterMap.get("dealerNameFilter"),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((String) ((List) filterMap.get("marginPercentageFilter")).get(0)),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((Double) ((List) filterMap.get("marginPercentageFilter")).get(1)),
                (LocalDate) filterMap.get("fromDateFilter"), (LocalDate) filterMap.get("toDateFilter"),
                (Pageable) filterMap.get("pageable"));

        // count
        List<Integer> countAll = bookingRepository.countAllForOutlier(
                (List<String>) filterMap.get("regionFilter"), (List<String>) filterMap.get("plantFilter"), (List<String>) filterMap.get("metaSeriesFilter"),
                (List<String>) filterMap.get("classFilter"), (List<String>) filterMap.get("modelFilter"), (List<String>) filterMap.get("dealerNameFilter"),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((String) ((List) filterMap.get("marginPercentageFilter")).get(0)),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((Double) ((List) filterMap.get("marginPercentageFilter")).get(1)),
                (LocalDate) filterMap.get("fromDateFilter"), (LocalDate) filterMap.get("toDateFilter"));

        List<Booking> getSumAllOrder = bookingRepository.getSumAllOrderForOutlier(
                (List<String>) filterMap.get("regionFilter"), (List<String>) filterMap.get("plantFilter"), (List<String>) filterMap.get("metaSeriesFilter"),
                (List<String>) filterMap.get("classFilter"), (List<String>) filterMap.get("modelFilter"), (List<String>) filterMap.get("dealerNameFilter"),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((String) ((List) filterMap.get("marginPercentageFilter")).get(0)),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((Double) ((List) filterMap.get("marginPercentageFilter")).get(1)),
                (LocalDate) filterMap.get("fromDateFilter"), (LocalDate) filterMap.get("toDateFilter"));

        result.put("total", getSumAllOrder);
        result.put("totalItems", countAll.size());
        result.put("listOutlier", setIdForData(listOrder));
        // get latest updated time
        Optional<LocalDateTime> latestUpdatedTimeOptional = bookingRepository.getLatestUpdatedTime();
        String latestUpdatedTime = null;
        if (latestUpdatedTimeOptional.isPresent()) {
            latestUpdatedTime = DateUtils.convertLocalDateTimeToString(latestUpdatedTimeOptional.get());
        }

        result.put("latestUpdatedTime",latestUpdatedTime);
        result.put("serverTimeZone", TimeZone.getDefault().getID());
        return result;
    }

    public List<Booking> setIdForData(List<Booking> bookings) {
        long i = 0;
        for (Booking booking : bookings) {
            booking.setOrderNo(String.valueOf(i));
            booking.setMarginPercentageAfterSurcharge(booking.getMarginAfterSurcharge() / booking.getDealerNetAfterSurcharge());
            i++;
        }
        return bookings;
    }

    public Map<String, Object> getDataForChart(FilterModel filters) throws ParseException {
        Map<String, Object> outliersData = new HashMap<>();

        Map<String, Object> filterMap = ConvertDataFilterUtil.loadDataFilterIntoMap(filters);
        List<Booking> listOrder = bookingRepository.getOrderForOutlier(
                (List<String>) filterMap.get("regionFilter"), (List<String>) filterMap.get("plantFilter"), (List<String>) filterMap.get("metaSeriesFilter"),
                (List<String>) filterMap.get("classFilter"), (List<String>) filterMap.get("modelFilter"), (List<String>) filterMap.get("dealerNameFilter"),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((String) ((List) filterMap.get("marginPercentageFilter")).get(0)),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((Double) ((List) filterMap.get("marginPercentageFilter")).get(1)),
                (LocalDate) filterMap.get("fromDateFilter"), (LocalDate) filterMap.get("toDateFilter"), null);

        List<Object> listRegionData = getListRegionData(listOrder);

        outliersData.put("chartOutliersData", listRegionData);
        return outliersData;
    }

    private List<Object> getListRegionData(List<Booking> listOrder) {
        List<ChartOutlier> asiachartOutlierList = new ArrayList<>();
        List<ChartOutlier> pacificchartOutlierList = new ArrayList<>();
        List<ChartOutlier> chinachartOutlierList = new ArrayList<>();
        List<ChartOutlier> indiachartOutlierList = new ArrayList<>();

        for (Booking order : listOrder) {
            ChartOutlier chartOutlier = new ChartOutlier(
                    order.getCountry().getRegion().getRegionName(),
                    order.getDealerNet(),
                    order.getDealerNetAfterSurcharge() == 0 ? 0 : order.getMarginAfterSurcharge() / order.getDealerNetAfterSurcharge(),
                    order.getProduct().getModelCode()
            );
            switch (order.getCountry().getRegion().getRegionName()) {
                case "Asia":
                    asiachartOutlierList.add(chartOutlier);
                    break;
                case "Pacific":
                    pacificchartOutlierList.add(chartOutlier);
                    break;
                case "China":
                    chinachartOutlierList.add(chartOutlier);
                    break;
                case "India":
                    indiachartOutlierList.add(chartOutlier);
                    break;
            }
        }
        List<Object> listRegionData = new ArrayList<>();
        listRegionData.add(asiachartOutlierList);
        listRegionData.add(pacificchartOutlierList);
        listRegionData.add(chinachartOutlierList);
        listRegionData.add(indiachartOutlierList);
        return listRegionData;
    }

}
