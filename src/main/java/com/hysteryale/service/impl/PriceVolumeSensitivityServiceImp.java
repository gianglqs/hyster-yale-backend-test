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
        List<Booking> getListBookingByFilter=null;


        long countAll = 0;
        if(metaSeriesFilter==null&segmentFilter==null){
            getListBookingByFilter = bookingRepository.getBookingForPriceVolumeSensitivityGroupBySeriesAndSegmentDefault();
            countAll = bookingRepository.countAllForPriceVolSensitivityGroupBySeriesAndSegmentDefault();
        }
        if(segmentFilter!=null&&metaSeriesFilter==null){
            getListBookingByFilter = bookingRepository.getBookingForPriceVolumeSensitivityGroupBySegment(segmentFilter);
            countAll = bookingRepository.countAllForPriceVolSensitivityGroupBySegment( segmentFilter);
        }
        if(metaSeriesFilter!=null && segmentFilter==null){
            getListBookingByFilter = bookingRepository.getBookingForPriceVolumeSensitivityGroupBySeries(metaSeriesFilter);
            countAll = bookingRepository.countAllForPriceVolSensitivityGroupBySeries(metaSeriesFilter);
        }
        if(metaSeriesFilter!=null && segmentFilter!=null){
            getListBookingByFilter = bookingRepository.getBookingForPriceVolumeSensitivityGroupBySeriesAndSegment(segmentFilter, metaSeriesFilter);
            countAll = bookingRepository.countAllForPriceVolSensitivityGroupBySeriesAndSegment(segmentFilter, metaSeriesFilter);
        }



//        if (metaSeriesFilter == null) {
//            getListBookingByFilter = bookingRepository.getBookingForPriceVolumeSensitivityGroupBySegment(segmentFilter);
//            countAll = bookingRepository.countAllForPriceVolSensitivityGroupBySegment(segmentFilter == null ? new ArrayList<>() : segmentFilter);
//
//        }else if(segmentFilter==null) {
//            getListBookingByFilter = bookingRepository.getBookingForPriceVolumeSensitivityGroupBySeries(metaSeriesFilter);
//            countAll = bookingRepository.countAllForPriceVolSensitivityGroupBySeries(metaSeriesFilter == null ? new ArrayList<>() : metaSeriesFilter);
//        } else {
//            getListBookingByFilter = bookingRepository.getBookingForPriceVolumeSensitivityGroupBySeriesAndSegment(segmentFilter, metaSeriesFilter);
//            countAll = bookingRepository.countAllForPriceVolSensitivityGroupBySeriesAndSegment(segmentFilter == null ? new ArrayList<>() : segmentFilter, metaSeriesFilter);
//        }

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


    public List<PriceVolSensitivityPayLoad> calculatePriceVolSensitivity(List<Booking> bookings, double discountPercent, boolean withMarginVolumeRecovery) {
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

            // resolve case if  / 0 when revenue=0
            double fakeRevenue=1;
            long volume = booking.getQuantity();
            double COGS = booking.getTotalCost();
            double margin = revenue - COGS;
            double marginPercent = margin / (revenue!=0?revenue:fakeRevenue);

            //resolve case if / 0 when volumn =0
            long fakeVolumn=1;
            double ASP = revenue / (volume!=0?volume:fakeVolumn);
            double ACP = COGS / (volume!=0?volume:fakeVolumn);
            double AvSM = (ASP - ACP)!=0?(ASP-ACP):1;

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

            double fakeRevenueAfterOffset=1;
            if (withMarginVolumeRecovery)
                revenueAfterOffset = incrementalRevenueRecovery + revenue;
            double COGSAfterOffset = volumeAfterOffset * ACP;
            double marginAfterOffset = revenueAfterOffset - COGSAfterOffset;
            double marginPercentAfterOffset = marginAfterOffset / (revenueAfterOffset!=0?revenueAfterOffset:fakeRevenueAfterOffset);

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

}
