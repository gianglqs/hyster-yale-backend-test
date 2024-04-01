package com.hysteryale.service.impl;

import com.hysteryale.model.Booking;
import com.hysteryale.model.filters.PriceVolSensitivityFilterModel;
import com.hysteryale.model.payLoad.PriceVolSensitivityPayLoad;
import com.hysteryale.repository.BookingRepository;
import com.hysteryale.service.PriceVolumeSensitivityService;
import com.hysteryale.utils.ConvertDataFilterUtil;
import com.hysteryale.utils.DateUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PriceVolumeSensitivityServiceImp implements PriceVolumeSensitivityService {

    @Resource
    private BookingRepository bookingRepository;

    @Override
    public Map<String, Object> getDataByFilter(PriceVolSensitivityFilterModel filters) throws ParseException {
        Map<String, Object> result = new HashMap<>();

        double discountPercent = filters.getDiscountPercent() / 100;
        Map<String, Object> filterMap = ConvertDataFilterUtil.loadDataFilterIntoMap(filters.getDataFilter());

        List<String> segmentFilter = (List<String>) filterMap.get("segmentFilter");
        List<String> metaSeriesFilter = (List<String>) filterMap.get("metaSeriesFilter");
        List<Booking> getListBookingByFilter;
        long countAll = 0;
        if (metaSeriesFilter == null) {
            if (segmentFilter != null && segmentFilter.size() == 1) {
                getListBookingByFilter = bookingRepository.getBookingForPriceVolumeSensitivityGroupBySeries(segmentFilter, metaSeriesFilter);
                countAll = bookingRepository.countAllForPriceVolSensitivityGroupBySeries(segmentFilter, new ArrayList<>());
            } else {
                getListBookingByFilter = bookingRepository.getBookingForPriceVolumeSensitivityGroupBySegment(segmentFilter);
                countAll = bookingRepository.countAllForPriceVolSensitivityGroupBySegment(segmentFilter == null ? new ArrayList<>() : segmentFilter);
            }
        } else {
            getListBookingByFilter = bookingRepository.getBookingForPriceVolumeSensitivityGroupBySeries(segmentFilter, metaSeriesFilter);
            countAll = bookingRepository.countAllForPriceVolSensitivityGroupBySeries(segmentFilter == null ? new ArrayList<>() : segmentFilter, metaSeriesFilter);
        }
        List<PriceVolSensitivityPayLoad> priceVolSensitivityPayLoadList = calculatePriceVolSensitivity(getListBookingByFilter, discountPercent, filters.isWithMarginVolumeRecovery());
        result.put("listOrder", priceVolSensitivityPayLoadList);
        result.put("totalItems", countAll);

        // get latest updated time
        Optional<LocalDateTime> latestUpdatedTimeOptional = bookingRepository.getLatestUpdatedTime();
        String latestUpdatedTime = null;
        if (latestUpdatedTimeOptional.isPresent()) {
            latestUpdatedTime = DateUtils.convertLocalDateTimeToString(latestUpdatedTimeOptional.get());
        }

        result.put("latestUpdatedTime", latestUpdatedTime);
        result.put("serverTimeZone", TimeZone.getDefault().getID());

        return result;
    }

    private List<PriceVolSensitivityPayLoad> calculatePriceVolSensitivity(List<Booking> bookings, double discountPercent, boolean withMarginVolumeRecovery) {
        List<PriceVolSensitivityPayLoad> result = new ArrayList<>();
        int id = 1;
        for (Booking booking : bookings) {
            PriceVolSensitivityPayLoad priceVolSensitivityPayLoad = new PriceVolSensitivityPayLoad();
            priceVolSensitivityPayLoad.setSegment(booking.getProduct().getSegment());
            priceVolSensitivityPayLoad.setSeries(booking.getSeries());
            priceVolSensitivityPayLoad.setId(id);
            id++;

            priceVolSensitivityPayLoad.setDiscountPercent(discountPercent);

            double revenue = booking.getDealerNetAfterSurcharge();
            long volume = booking.getQuantity();
            double COGS = booking.getTotalCost();
            double margin = revenue - COGS;
            double marginPercent = margin / revenue;

            double ASP = revenue / volume;
            double ACP = COGS / volume;
            double AvSM = ASP - ACP;

            // discountPercent

            double newDN = ASP * (1 - discountPercent);
            double marginErosion = Math.abs((newDN - ASP) * volume);
            double revisedMargin = newDN - ACP;
            double revisedMarginPercent = revisedMargin / newDN;
            int unitVolumeOffset = 0;

            if (withMarginVolumeRecovery)
                unitVolumeOffset = (int) Math.ceil(marginErosion / AvSM);

            double incrementalRevenueRecovery = unitVolumeOffset * newDN;

            long volumeAfterOffset = unitVolumeOffset + volume;
            double revenueAfterOffset = volume * newDN;
            if (withMarginVolumeRecovery)
                revenueAfterOffset = incrementalRevenueRecovery + revenue;
            double COGSAfterOffset = volumeAfterOffset * ACP;
            double marginAfterOffset = revenueAfterOffset - COGSAfterOffset;
            double marginPercentAfterOffset = marginAfterOffset / revenueAfterOffset;

            priceVolSensitivityPayLoad.setVolume(volume);
            priceVolSensitivityPayLoad.setRevenue(revenue);
            priceVolSensitivityPayLoad.setCOGS(COGS);
            priceVolSensitivityPayLoad.setMargin(margin);
            priceVolSensitivityPayLoad.setMarginPercent(marginPercent);

            priceVolSensitivityPayLoad.setNewDN(newDN);
            priceVolSensitivityPayLoad.setUnitVolumeOffset(unitVolumeOffset);
            priceVolSensitivityPayLoad.setNewVolume(volumeAfterOffset);
            priceVolSensitivityPayLoad.setNewRevenue(revenueAfterOffset);
            priceVolSensitivityPayLoad.setNewCOGS(COGSAfterOffset);
            priceVolSensitivityPayLoad.setNewMargin(marginAfterOffset);
            priceVolSensitivityPayLoad.setNewMarginPercent(marginPercentAfterOffset);

            result.add(priceVolSensitivityPayLoad);
        }

        return result;
    }

    public static void main(String[] args) {
        double revenue = 2106000;
        long volume = 89;
        double COGS = 1814000;
        double margin = revenue - COGS;
        double marginPercent = margin / revenue;

        double ASP = revenue / volume;
        double ACP = COGS / volume;
        double AvSM = ASP - ACP;

        // discountPercent
        double discountPercent = 0.01;
        boolean withMarginVolumeRecovery = false;

        double newDN = ASP * (1 - discountPercent);
        double marginErosion = Math.abs((newDN - ASP) * volume);
        double revisedMargin = newDN - ACP;
        double revisedMarginPercent = revisedMargin / newDN;
        int unitVolumeOffset = 0;

        if (withMarginVolumeRecovery)
            unitVolumeOffset = (int) Math.ceil(marginErosion / AvSM);

        double incrementalRevenueRecovery = unitVolumeOffset * newDN;

        long volumeAfterOffset = unitVolumeOffset + volume;
        double revenueAfterOffset = volume * newDN;
        if (withMarginVolumeRecovery)
            revenueAfterOffset = incrementalRevenueRecovery + revenue;
        double COGSAfterOffset = volumeAfterOffset * ACP;
        double marginAfterOffset = revenueAfterOffset - COGSAfterOffset;
        double marginPercentAfterOffset = marginAfterOffset / revenueAfterOffset;
    }
}
