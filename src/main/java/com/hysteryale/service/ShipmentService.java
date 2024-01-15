package com.hysteryale.service;

import com.hysteryale.model.BookingOrder;
import com.hysteryale.model.ExchangeRate;
import com.hysteryale.model.Shipment;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.repository.ShipmentRepository;
import com.hysteryale.repository.bookingorder.BookingOrderRepository;
import com.hysteryale.utils.ConvertDataFilterUtil;
import com.hysteryale.utils.TargetCurrency;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.*;
import java.text.ParseException;

@Service
public class ShipmentService extends BasedService {
    @Resource
    ShipmentRepository shipmentRepository;

    @Resource
    ExchangeRateService exchangeRateService;

    @Resource
    BookingOrderRepository bookingOrderRepository;

    public Map<String, Object> getShipmentByFilter(FilterModel filterModel) throws ParseException {
        Map<String, Object> result = new HashMap<>();
        //Get FilterData
        Map<String, Object> filterMap = ConvertDataFilterUtil.loadDataFilterIntoMap(filterModel);
        logInfo(filterMap.toString());

        List<Shipment> shipmentList = shipmentRepository.findShipmentByFilterForTable(
                (String) filterMap.get("orderNoFilter"), (List<String>) filterMap.get("regionFilter"), (List<String>) filterMap.get("plantFilter"),
                (List<String>) filterMap.get("metaSeriesFilter"), (List<String>) filterMap.get("classFilter"), (List<String>) filterMap.get("modelFilter"),
                (List<String>) filterMap.get("segmentFilter"), (List<String>) filterMap.get("dealerNameFilter"), (String) filterMap.get("aopMarginPercentageFilter"),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((String) ((List) filterMap.get("marginPercentageFilter")).get(0)),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((Double) ((List) filterMap.get("marginPercentageFilter")).get(1)),
                (Calendar) filterMap.get("fromDateFilter"), (Calendar) filterMap.get("toDateFilter"), (Pageable) filterMap.get("pageable"));
        result.put("listShipment", shipmentList);
        // get currency for order -> get exchange_rate
        List<String> listCurrency = new ArrayList<>();
        List<ExchangeRate> exchangeRateList = new ArrayList<>();
        List<String> listTargetCurrency = TargetCurrency.getListTargetCurrency;

        for (Shipment shipment : shipmentList) {
            if (shipment.getCurrency() != null) {
                String currency = shipment.getCurrency().getCurrency();
                if (!listCurrency.contains(currency)) { // get distinct currency in list order
                    listCurrency.add(currency);
                    for (String targetCurrency : listTargetCurrency) {
                        if (!targetCurrency.equals(currency)) { // get exchange_rate FROM current currency of order TO targetCurrency
                            exchangeRateList.add(exchangeRateService.getNearestExchangeRate(currency, targetCurrency));
                        }
                    }
                }
            }
        }
        result.put("listExchangeRate", exchangeRateList);

        //get total Recode
        int countAll = shipmentRepository.getCount(
                (String) filterMap.get("orderNoFilter"), (List<String>) filterMap.get("regionFilter"), (List<String>) filterMap.get("plantFilter"),
                (List<String>) filterMap.get("metaSeriesFilter"), (List<String>) filterMap.get("classFilter"), (List<String>) filterMap.get("modelFilter"),
                (List<String>) filterMap.get("segmentFilter"), (List<String>) filterMap.get("dealerNameFilter"), (String) filterMap.get("aopMarginPercentageFilter"),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((String) ((List) filterMap.get("marginPercentageFilter")).get(0)),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((Double) ((List) filterMap.get("marginPercentageFilter")).get(1)),
                (Calendar) filterMap.get("fromDateFilter"), (Calendar) filterMap.get("toDateFilter"));
        result.put("totalItems", countAll);

        // get total
        List<Shipment> getTotal = shipmentRepository.getTotalRowForShipmentPage(
                (String) filterMap.get("orderNoFilter"),
                filterMap.get("regionFilter") == null ? Collections.emptyList() : (List<String>) filterMap.get("regionFilter"),
                filterMap.get("plantFilter") == null ? Collections.emptyList() : (List<String>) filterMap.get("plantFilter"),
                filterMap.get("metaSeriesFilter") == null ? Collections.emptyList() : (List<String>) filterMap.get("metaSeriesFilter"),
                filterMap.get("classFilter") == null ? Collections.emptyList() : (List<String>) filterMap.get("classFilter"),
                filterMap.get("modelFilter") == null ? Collections.emptyList() : (List<String>) filterMap.get("modelFilter"),
                filterMap.get("segmentFilter") == null ? Collections.emptyList() : (List<String>) filterMap.get("segmentFilter"),
                filterMap.get("dealerNameFilter") == null ? Collections.emptyList() : (List<String>) filterMap.get("dealerNameFilter"),
                (String) filterMap.get("aopMarginPercentageFilter"),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((String) ((List) filterMap.get("marginPercentageFilter")).get(0)),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((Double) ((List) filterMap.get("marginPercentageFilter")).get(1)),
                filterMap.get("fromDateFilter") == null ? new GregorianCalendar(1996, 10, 23) : (Calendar) filterMap.get("fromDateFilter"),
                filterMap.get("toDateFilter") == null ? new GregorianCalendar(2996, 10, 23) : (Calendar) filterMap.get("toDateFilter")
        );
        result.put("total", getTotal);

        // get totalMarginPercentage of bookingOrder
        double getTotalBookingMarginPercentage = bookingOrderRepository.getTotalMarginPercentage(
                (String) filterMap.get("orderNoFilter"), (List<String>) filterMap.get("regionFilter"), (List<String>) filterMap.get("plantFilter"),
                (List<String>) filterMap.get("metaSeriesFilter"), (List<String>) filterMap.get("classFilter"), (List<String>) filterMap.get("modelFilter"),
                (List<String>) filterMap.get("segmentFilter"), (List<String>) filterMap.get("dealerNameFilter"), (String) filterMap.get("aopMarginPercentageFilter"),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((String) ((List) filterMap.get("marginPercentageFilter")).get(0)),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((Double) ((List) filterMap.get("marginPercentageFilter")).get(1)),
                (Calendar) filterMap.get("fromDateFilter"), (Calendar) filterMap.get("toDateFilter"));
        getTotal.get(0).setBookingMarginPercentageAfterSurCharge(getTotalBookingMarginPercentage);

        return result;
    }

    public Shipment getShipmentByOrderNo(String orderNo) {
        Optional<Shipment> optionalShipment = shipmentRepository.findShipmentByOrderNo(orderNo);
        if (optionalShipment.isPresent())
            return optionalShipment.get();
        return null;
    }



//    private Time
}
