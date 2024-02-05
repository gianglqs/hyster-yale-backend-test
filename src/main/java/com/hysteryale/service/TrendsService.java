package com.hysteryale.service;

import com.hysteryale.model.TrendData;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.repository.ShipmentRepository;
import com.hysteryale.repository.BookingRepository;
import com.hysteryale.utils.ConvertDataFilterUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class TrendsService {
    @Resource
    BookingRepository bookingRepository;
    @Resource
    ShipmentRepository shipmentRepository;

    public Map<String, List<TrendData>> getMarginVsCostData(FilterModel filters) throws ParseException {

        Map<String, Object> filterMap = ConvertDataFilterUtil.loadDataFilterIntoMap(filters);

        List<TrendData> bookingData = bookingRepository.getMarginVsCostData(
                (List<String>) filterMap.get("regionFilter"),  (List<String>)  filterMap.get("plantFilter"),  (List<String>)  filterMap.get("metaSeriesFilter"),
                (List<String>)  filterMap.get("classFilter"),  (List<String>)  filterMap.get("modelFilter"),  (List<String>)  filterMap.get("segmentFilter"),
                (List<String>)  filterMap.get("dealerNameFilter"), (Integer) filterMap.get("year")
        );

        List<TrendData> shipmentData = shipmentRepository.getMarginVsCostData(
                (List<String>) filterMap.get("regionFilter"),  (List<String>)  filterMap.get("plantFilter"),  (List<String>)  filterMap.get("metaSeriesFilter"),
                (List<String>)  filterMap.get("classFilter"),  (List<String>)  filterMap.get("modelFilter"),  (List<String>)  filterMap.get("segmentFilter"),
                (List<String>)  filterMap.get("dealerNameFilter"), (Integer) filterMap.get("year")
        );

        for(int i = 0; i < 12; i++) {
            if(i < bookingData.size())
                bookingData.get(i).setMonthYear(parseMonth(bookingData.get(i).getMonth(), (Integer) filterMap.get("year")));
            if(i < shipmentData.size())
                shipmentData.get(i).setMonthYear(parseMonth(shipmentData.get(i).getMonth(), (Integer) filterMap.get("year")));
        }
        return Map.of(
                "bookingData", bookingData,
                "shipmentData", shipmentData
        );
    }

    public Map<String, List<TrendData>> getMarginVsDNData(FilterModel filters) throws ParseException {

        Map<String, Object> filterMap = ConvertDataFilterUtil.loadDataFilterIntoMap(filters);

        List<TrendData> bookingData = bookingRepository.getMarginVsDNData(
                (List<String>) filterMap.get("regionFilter"),  (List<String>)  filterMap.get("plantFilter"),  (List<String>)  filterMap.get("metaSeriesFilter"),
                (List<String>)  filterMap.get("classFilter"),  (List<String>)  filterMap.get("modelFilter"),  (List<String>)  filterMap.get("segmentFilter"),
                (List<String>)  filterMap.get("dealerNameFilter"), (Integer) filterMap.get("year")
        );

        List<TrendData> shipmentData = shipmentRepository.getMarginVsDNData(
                (List<String>) filterMap.get("regionFilter"),  (List<String>)  filterMap.get("plantFilter"),  (List<String>)  filterMap.get("metaSeriesFilter"),
                (List<String>)  filterMap.get("classFilter"),  (List<String>)  filterMap.get("modelFilter"),  (List<String>)  filterMap.get("segmentFilter"),
                (List<String>)  filterMap.get("dealerNameFilter"), (Integer) filterMap.get("year")
        );

        for(int i = 0; i < 12; i++) {
            if(i < bookingData.size())
                bookingData.get(i).setMonthYear(parseMonth(bookingData.get(i).getMonth(), (Integer) filterMap.get("year")));
            if(i < shipmentData.size())
                shipmentData.get(i).setMonthYear(parseMonth(shipmentData.get(i).getMonth(), (Integer) filterMap.get("year")));
        }
        return Map.of(
                "bookingData", bookingData,
                "shipmentData", shipmentData
        );
    }

    private String parseMonth(int month, int year) {
        switch (month) {
            case 1:
                return "Jan " + year;
            case 2:
                return "Feb " + year;
            case 3:
                return "Mar " + year;
            case 4:
                return "Apr " + year;
            case 5:
                return "May " + year;
            case 6:
                return "Jun " + year;
            case 7:
                return "Jul " + year;
            case 8:
                return "Aug " + year;
            case 9:
                return "Sep " + year;
            case 10:
                return "Oct " + year;
            case 11:
                return "Nov " + year;
            default:
                return "Dec " + year;
        }
    }
}
