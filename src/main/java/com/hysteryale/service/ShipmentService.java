package com.hysteryale.service;

import com.hysteryale.model.Booking;
import com.hysteryale.model.Currency;
import com.hysteryale.model.ExchangeRate;
import com.hysteryale.model.Shipment;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.repository.ShipmentRepository;
import com.hysteryale.repository.BookingOrderRepository;
import com.hysteryale.utils.ConvertDataFilterUtil;
import com.hysteryale.utils.TargetCurrency;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;

@Service
@SuppressWarnings("unchecked")
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

        //get total Recode
        int countAll = shipmentRepository.getCount(
                (String) filterMap.get("orderNoFilter"), (List<String>) filterMap.get("regionFilter"), (List<String>) filterMap.get("plantFilter"),
                (List<String>) filterMap.get("metaSeriesFilter"), (List<String>) filterMap.get("classFilter"), (List<String>) filterMap.get("modelFilter"),
                (List<String>) filterMap.get("segmentFilter"), (List<String>) filterMap.get("dealerNameFilter"), (String) filterMap.get("aopMarginPercentageFilter"),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((String) ((List) filterMap.get("marginPercentageFilter")).get(0)),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((Double) ((List) filterMap.get("marginPercentageFilter")).get(1)),
                (LocalDate) filterMap.get("fromDateFilter"), (LocalDate) filterMap.get("toDateFilter"));
        result.put("totalItems", countAll);

        // get total
        List<Shipment> totalShipment = shipmentRepository.getTotal(
                (String) filterMap.get("orderNoFilter"), (List<String>) filterMap.get("regionFilter"), (List<String>) filterMap.get("plantFilter"),
                (List<String>) filterMap.get("metaSeriesFilter"), (List<String>) filterMap.get("classFilter"), (List<String>) filterMap.get("modelFilter"),
                (List<String>) filterMap.get("segmentFilter"), (List<String>) filterMap.get("dealerNameFilter"), (String) filterMap.get("aopMarginPercentageFilter"),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((String) ((List) filterMap.get("marginPercentageFilter")).get(0)),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null : ((Double) ((List) filterMap.get("marginPercentageFilter")).get(1)),
                (LocalDate) filterMap.get("fromDateFilter"), (LocalDate) filterMap.get("toDateFilter"));


        int quantity = 0;
        double dealerNet = 0;
        double dealerNetAfterSurcharge = 0;
        double totalCost = 0;
        double marginAfterSurcharge = 0;
        double marginPercentageAfterSurcharge = 0;
        double netRevenue = 0;
        List<String> listOrderNo = new ArrayList<>();

        for (Shipment shipment : totalShipment) {
            quantity++;
            dealerNet += shipment.getDealerNet();
            dealerNetAfterSurcharge += shipment.getDealerNetAfterSurcharge();
            totalCost += shipment.getTotalCost();
            netRevenue += shipment.getNetRevenue();
            listOrderNo.add(shipment.getOrderNo());
        }
        marginAfterSurcharge = dealerNetAfterSurcharge - totalCost;
        marginPercentageAfterSurcharge = marginAfterSurcharge / dealerNetAfterSurcharge;

        double bookingMargin = bookingOrderRepository.getTotalMarginPercentage(listOrderNo);

        Shipment shipment = new Shipment("Total", new Currency("USD"), quantity, dealerNet, dealerNetAfterSurcharge, totalCost, netRevenue, marginAfterSurcharge, marginPercentageAfterSurcharge, bookingMargin);
        result.put("total", List.of(shipment));

        return result;
    }

    public Shipment getShipmentByOrderNo(String orderNo) {
        Optional<Shipment> optionalShipment = shipmentRepository.findShipmentByOrderNo(orderNo);
        return optionalShipment.orElse(null);
    }


//    private Time
}
