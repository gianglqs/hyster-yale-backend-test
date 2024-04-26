/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.utils;

import com.hysteryale.model.filters.FilterModel;

import com.hysteryale.model.filters.InterestRateFilterModel;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConvertDataFilterUtil {

    public static Map<String, Object> loadDataFilterIntoMap(FilterModel filterModel) throws ParseException {
        Map<String, Object> result = new HashMap<>();
        String orderNoFilter = checkStringData(filterModel.getOrderNo());
        List<String> regionFilter = checkListData(filterModel.getRegions());
        List<String> plantFilter = checkListData(filterModel.getPlants());
        List<String> metaSeriesFilter = checkListData(filterModel.getMetaSeries());
        List<String> classFilter = checkListData(filterModel.getClasses());
        List<String> modelFilter = checkListData(filterModel.getModels());
        List<String> dealerNameFilter = checkListData(filterModel.getDealers());
        List<String> segmentFilter = checkListData(filterModel.getSegments());
        Boolean ChineseBrandFilter = checkBooleanData(filterModel.getChineseBrand());
        String aopMarginPercentageFilter = checkStringData(filterModel.getAopMarginPercentageGroup());
        List<Object> marginPercentageFilter = checkComparator(filterModel.getMarginPercentage());
        List<Object> marginPercentageAfterAdjFilter = checkComparator(filterModel.getMarginPercentageAfterAdj());
        LocalDate fromDateFilter = checkDateData(filterModel.getFromDate());
        LocalDate toDateFilter = checkDateData(filterModel.getToDate());
        Pageable pageable = PageRequest.of(filterModel.getPageNo() == 0 ? filterModel.getPageNo() : filterModel.getPageNo() - 1, filterModel.getPerPage() == 0 ? 100 : filterModel.getPerPage());
        String modelCodeFilter = checkStringData(filterModel.getModelCode());
        List<String> brandFilter = checkListData(filterModel.getBrands());
        List<String> familyFilter = checkListData(filterModel.getFamily());
        List<String> truckTypeFilter = checkListData(filterModel.getTruckType());
        List<String> orderNumberListFilter = checkListDataHaveNoneElement(filterModel.getOrderNumbers());
        String metaseriesFilter = checkStringData(filterModel.getMetaSeriez());
        Integer dealerIdListFilter = filterModel.getDealerId();

        LocalDate calendar = LocalDate.now();
        Integer year = filterModel.getYear() == null ? calendar.getYear() : filterModel.getYear();

        result.put("orderNoFilter", orderNoFilter);
        result.put("regionFilter", regionFilter);
        result.put("plantFilter", plantFilter);
        result.put("dealerNameFilter", dealerNameFilter);
        result.put("metaSeriesFilter", metaSeriesFilter);
        result.put("classFilter", classFilter);
        result.put("modelFilter", modelFilter);
        result.put("segmentFilter", segmentFilter);
        result.put("ChineseBrandFilter", ChineseBrandFilter);
        result.put("aopMarginPercentageFilter", aopMarginPercentageFilter);
        result.put("marginPercentageFilter", marginPercentageFilter);
        result.put("marginPercentageAfterAdjFilter", marginPercentageAfterAdjFilter);
        result.put("fromDateFilter", fromDateFilter);
        result.put("toDateFilter", toDateFilter);
        result.put("pageable", pageable);
        result.put("year", year);

        //for ProductDimensionUI
        result.put("modelCodeFilter", modelCodeFilter);
        result.put("brandFilter", brandFilter);
        result.put("familyFilter", familyFilter);
        result.put("truckTypeFilter", truckTypeFilter);

        // for ProductDimension detail
        result.put("orderNumberListFilter", orderNumberListFilter);
        result.put("metaseriesFilter", metaseriesFilter);
        result.put("dealerId", dealerIdListFilter);


        return result;
    }

    public static Map<String, Object> loadInterestRateDataFilterIntoMap(InterestRateFilterModel filterModel) throws ParseException {
        Map<String, Object> result = new HashMap<>();
        String bankNameFilter = checkStringData(filterModel.getBankName());
        List<String> regionFilter = checkListData(filterModel.getRegions());
        Pageable pageable = PageRequest.of(filterModel.getPageNo() == 0 ? filterModel.getPageNo() : filterModel.getPageNo() - 1, filterModel.getPerPage() == 0 ? 10 : filterModel.getPerPage());
        result.put("pageable", pageable);
        result.put("regionFilter", regionFilter);
        result.put("bankNameFilter", bankNameFilter);

        return result;
    }

    private static List<String> checkListData(List<String> data) {
        return data == null || data.isEmpty() ? null : data;
    }

    private static List<String> checkListDataHaveNoneElement(List<String> data) {
        return data == null || data.isEmpty() || (data.size() == 1 && data.get(0)==null) ? null : data;
    }

    private static String checkStringData(String data) {
        return data == null || data.isEmpty() ? null : data;
    }

    private static Boolean checkBooleanData(String data) {
        if (data == null)
            return null;
        return data.equals("Chinese Brand");
    }

    public static LocalDate checkDateData(String data) throws ParseException {
        if (data == null || data.isEmpty())
            return null;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return (formatter.parse(data).toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        //  return formatter.parse(data);
    }

    /**
     * @return [comparator, value]
     */
    private static List<Object> checkComparator(String data) {
        List<Object> result = new ArrayList<>();
        if (data != null) {
            String patternString = "([<>]=?|=)\\s*([0-9]+(?:\\.[0-9]+)?)\\s*%?";
            Pattern pattern = Pattern.compile(patternString);
            Matcher matcher = pattern.matcher(data);
            if (matcher.find()) {
                String operator = matcher.group(1);
                result.add(operator);
                double percentValue = Double.parseDouble(matcher.group(2)) / 100;
                result.add(percentValue);
            }
        }
        return result;
    }

    public static String convertFilter(String filter) {
        if (filter == null || filter.trim().isEmpty())
            return null;
        return filter;
    }

}
