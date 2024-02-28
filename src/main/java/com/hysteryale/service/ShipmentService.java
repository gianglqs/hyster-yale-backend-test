package com.hysteryale.service;

import com.hysteryale.model.Booking;
import com.hysteryale.model.Currency;
import com.hysteryale.model.ExchangeRate;
import com.hysteryale.model.Shipment;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.repository.BookingRepository;
import com.hysteryale.repository.ShipmentRepository;
import com.hysteryale.utils.ConvertDataFilterUtil;
import com.hysteryale.utils.DateUtils;
import com.hysteryale.utils.TargetCurrency;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@SuppressWarnings("unchecked")
public class ShipmentService extends BasedService {
    @Resource
    ShipmentRepository shipmentRepository;

    @Resource
    ExchangeRateService exchangeRateService;

    @Resource
    BookingRepository bookingRepository;

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
                (LocalDate) filterMap.get("fromDateFilter"), (LocalDate) filterMap.get("toDateFilter"), (Pageable) filterMap.get("pageable"));
        result.put("listShipment", shipmentList);
        // get currency for order -> get exchange_rate
        List<String> listCurrency = new ArrayList<>();
        List<ExchangeRate> exchangeRateList = new ArrayList<>();
        List<String> listTargetCurrency = TargetCurrency.getListTargetCurrency;

        listCurrency.add("USD");
        listCurrency.add("AUD");

        exchangeRateList.add(exchangeRateService.getNearestExchangeRate("USD", "AUD"));
        exchangeRateList.add(exchangeRateService.getNearestExchangeRate("AUD", "USD"));


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

        // get total
        List<Shipment> totalShipment = shipmentRepository.getTotal(
                (String) filterMap.get("orderNoFilter"), (List<String>) filterMap.get("regionFilter"), (List<String>) filterMap.get("plantFilter"),
                (List<String>) filterMap.get("metaSeriesFilter"), (List<String>) filterMap.get("classFilter"), (List<String>) filterMap.get("modelFilter"),
                (List<String>) filterMap.get("segmentFilter"), (List<String>) filterMap.get("dealerNameFilter"), (String) filterMap.get("aopMarginPercentageFilter"),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((String) ((List) filterMap.get("marginPercentageFilter")).get(0)),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((Double) ((List) filterMap.get("marginPercentageFilter")).get(1)),
                (LocalDate) filterMap.get("fromDateFilter"), (LocalDate) filterMap.get("toDateFilter"));

        result.put("totalItems", totalShipment.size());


        int quantity = 0;
        double dealerNet = 0;
        double dealerNetAfterSurcharge = 0;
        double totalCost = 0;
        double marginAfterSurcharge = 0;
        double marginPercentageAfterSurcharge = 0;
        double netRevenue = 0;

        // booking
        double dealerNetAfterSurchargeBooking = 0;
        double marginAfterSurchargeBooking = 0;

        for (Shipment shipment : totalShipment) {
            quantity++;
            dealerNet += shipment.getDealerNet();
            dealerNetAfterSurcharge += shipment.getDealerNetAfterSurcharge();
            totalCost += shipment.getTotalCost();
            netRevenue += shipment.getNetRevenue();
            if (shipment.getBookingDealerNetAfterSurcharge() != null)
                dealerNetAfterSurchargeBooking += shipment.getBookingDealerNetAfterSurcharge();
            if (shipment.getBookingMarginAfterSurcharge() != null)
                marginAfterSurchargeBooking += shipment.getBookingMarginAfterSurcharge();
        }
        marginAfterSurcharge = dealerNetAfterSurcharge - totalCost;

        marginPercentageAfterSurcharge = marginAfterSurcharge / dealerNetAfterSurcharge;

        Double marginPercentageAfterSurchargeBooking = null;
        if (dealerNetAfterSurchargeBooking != 0)
            marginPercentageAfterSurchargeBooking = marginAfterSurchargeBooking / dealerNetAfterSurchargeBooking;
        Shipment shipment = new Shipment("Total", new Currency("USD"), quantity, dealerNet, dealerNetAfterSurcharge, totalCost, netRevenue, marginAfterSurcharge, marginPercentageAfterSurcharge, marginPercentageAfterSurchargeBooking);
        result.put("total", List.of(shipment));

        Optional<LocalDateTime> latestUpdatedTimeOptional = shipmentRepository.getLatestUpdatedTime();
        String latestUpdatedTime = null;
        if (latestUpdatedTimeOptional.isPresent()) {
            latestUpdatedTime = DateUtils.convertLocalDateTimeToString(latestUpdatedTimeOptional.get());
        }

        result.put("latestUpdatedTime",latestUpdatedTime);
        result.put("serverTimeZone", TimeZone.getDefault().getID());
        return result;
    }

    public Shipment getShipmentByOrderNo(String orderNo) {
        Optional<Shipment> optionalShipment = shipmentRepository.findShipmentByOrderNo(orderNo);
        return optionalShipment.orElse(null);
    }


//    private Time
}
