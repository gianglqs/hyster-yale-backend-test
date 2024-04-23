/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.service;

import com.hysteryale.model.GDPCountry;
import com.hysteryale.model.payLoad.BubbleChartGDPCountryPageLoad;
import com.hysteryale.repository.GDPCountryRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class GDPServiceTest {

    @Resource
    GDPService gdpService;

    @Resource
    GDPCountryRepository gdpCountryRepository;

    /**
     * {@link GDPService#getDataForTable(int, int, int)}
     */
    @Test
    public void testGetDataForTable_2023() {
        int year = 2023;
        int pageNo = 1;
        int perPage = 100;
        Map<String, Object> dataForTable = gdpService.getDataForTable(year, pageNo, perPage);

        Pageable pageable = PageRequest.of(pageNo - 1, perPage);
        List<GDPCountry> listGDPCountriesByYear = gdpCountryRepository.findByYear(2020, pageable);
        long totalItems = gdpCountryRepository.countByYear(2020);
        Assertions.assertNotNull(dataForTable);

        Assertions.assertEquals(totalItems, dataForTable.get("totalItems"));
        ArrayList<GDPCountry> listDataTable = (ArrayList<GDPCountry>) dataForTable.get("dataTable");
        Assertions.assertEquals(listGDPCountriesByYear.size(), listDataTable.size());
        for (GDPCountry gdpCountry : listDataTable) {
            Assertions.assertEquals(gdpCountry.getGDP(), 0.0);
            Assertions.assertEquals(gdpCountry.getGrowth(), 0.0);
            Assertions.assertEquals(gdpCountry.getPerCapita(), 0.0);
            Assertions.assertEquals(gdpCountry.getShareOfWorld(), 0.0);
        }
    }

    /**
     * {@link GDPService#getDataForTable(int, int, int)}
     */
    @Test
    public void testGetDataForTable_2023_perPage_10() {
        int year = 2023;
        int pageNo = 1;
        int perPage = 10;
        Map<String, Object> dataForTable = gdpService.getDataForTable(year, pageNo, perPage);
        Assertions.assertNotNull(dataForTable);

        Pageable pageable = PageRequest.of(pageNo - 1, perPage);
        List<GDPCountry> listGDPCountriesByYear = gdpCountryRepository.findByYear(2020, pageable);
        long totalItems = gdpCountryRepository.countByYear(2020);

        Assertions.assertEquals(totalItems, dataForTable.get("totalItems"));
        ArrayList<GDPCountry> listDataTable = (ArrayList<GDPCountry>) dataForTable.get("dataTable");
        Assertions.assertEquals(listGDPCountriesByYear.size(), listDataTable.size());
        for (GDPCountry gdpCountry : listDataTable) {
            Assertions.assertEquals(gdpCountry.getGDP(), 0.0);
            Assertions.assertEquals(gdpCountry.getGrowth(), 0.0);
            Assertions.assertEquals(gdpCountry.getPerCapita(), 0.0);
            Assertions.assertEquals(gdpCountry.getShareOfWorld(), 0.0);
        }
    }

    /**
     * {@link GDPService#getDataForTable(int, int, int)}
     */
    @Test
    public void testGetDataForTable_2022() {
        int year = 2022;
        int pageNo = 1;
        int perPage = 100;
        Map<String, Object> dataForTable = gdpService.getDataForTable(year, pageNo, perPage);

        Pageable pageable = PageRequest.of(pageNo - 1, perPage);
        List<GDPCountry> listGDPCountriesByYear = gdpCountryRepository.findByYear(year, pageable);
        long totalItems = gdpCountryRepository.countByYear(year);
        Assertions.assertNotNull(dataForTable);

        Assertions.assertEquals(totalItems, dataForTable.get("totalItems"));
        ArrayList<GDPCountry> listDataTable = (ArrayList<GDPCountry>) dataForTable.get("dataTable");
        Assertions.assertEquals(listGDPCountriesByYear.size(), listDataTable.size());

        compareListGDPCountry(listDataTable, listGDPCountriesByYear);
    }

    /**
     * {@link GDPService#getDataForTable(int, int, int)}
     */
    @Test
    public void testGetDataForTable_2002_perPage_10() {
        int year = 2002;
        int pageNo = 1;
        int perPage = 10;
        Map<String, Object> dataForTable = gdpService.getDataForTable(year, pageNo, perPage);

        Pageable pageable = PageRequest.of(pageNo - 1, perPage);
        List<GDPCountry> listGDPCountriesByYear = gdpCountryRepository.findByYear(year, pageable);
        long totalItems = gdpCountryRepository.countByYear(year);
        Assertions.assertNotNull(dataForTable);

        Assertions.assertEquals(totalItems, dataForTable.get("totalItems"));
        ArrayList<GDPCountry> listDataTable = (ArrayList<GDPCountry>) dataForTable.get("dataTable");
        Assertions.assertEquals(listGDPCountriesByYear.size(), listDataTable.size());

        compareListGDPCountry(listDataTable, listGDPCountriesByYear);
    }

    private void compareListGDPCountry(List<GDPCountry> list1, List<GDPCountry> list2) {
        Assertions.assertEquals(list1.size(), list2.size());
        for (int i = 0; i < list1.size(); i++) {
            GDPCountry gdpCountry1 = list1.get(i);
            GDPCountry gdpCountry2 = list2.get(i);
            Assertions.assertEquals(gdpCountry1.getGDP(), gdpCountry2.getGDP());
            Assertions.assertEquals(gdpCountry1.getGrowth(), gdpCountry2.getGrowth());
            Assertions.assertEquals(gdpCountry1.getShareOfWorld(), gdpCountry2.getShareOfWorld());
            Assertions.assertEquals(gdpCountry2.getPerCapita(), gdpCountry1.getPerCapita());
        }

    }

    /**
     * {@link GDPService#getDataForBubbleChart(int)}
     */
    @Test
    public void testGetDataForBubbleChart_2020() {
        int year = 2020;

        List<BubbleChartGDPCountryPageLoad> dataForBubbleChart = gdpService.getDataForBubbleChart(year);

        List<GDPCountry> listGDPCountriesByYear = gdpCountryRepository.findByYearAndSort(year);
        Assertions.assertNotNull(dataForBubbleChart);
        Assertions.assertEquals(listGDPCountriesByYear.size(), dataForBubbleChart.size());

        compareListGDPCountryWithDataBubbleChartPayload(listGDPCountriesByYear, dataForBubbleChart);
    }

    /**
     * {@link GDPService#getDataForBubbleChart(int)}
     */
    @Test
    public void testGetDataForBubbleChart_2010() {
        int year = 2010;

        List<BubbleChartGDPCountryPageLoad> dataForBubbleChart = gdpService.getDataForBubbleChart(year);

        List<GDPCountry> listGDPCountriesByYear = gdpCountryRepository.findByYearAndSort(year);
        Assertions.assertNotNull(dataForBubbleChart);
        Assertions.assertEquals(listGDPCountriesByYear.size(), dataForBubbleChart.size());

        compareListGDPCountryWithDataBubbleChartPayload(listGDPCountriesByYear, dataForBubbleChart);
    }

    /**
     * {@link GDPService#getDataForBubbleChart(int)}
     */
    @Test
    public void testGetDataForBubbleChart_2030() {
        int year = 2030;

        List<BubbleChartGDPCountryPageLoad> dataForBubbleChart = gdpService.getDataForBubbleChart(year);

        List<GDPCountry> listGDPCountriesByYear = gdpCountryRepository.findByYearAndSort(year);
        Assertions.assertNotNull(dataForBubbleChart);
        Assertions.assertEquals(0, dataForBubbleChart.size());
        Assertions.assertEquals(listGDPCountriesByYear.size(), dataForBubbleChart.size());

    }

    private void compareListGDPCountryWithDataBubbleChartPayload(List<GDPCountry> listGDP, List<BubbleChartGDPCountryPageLoad> listBubbleChart) {
        Collections.reverse(listGDP);
        for (int i = 0; i < listGDP.size(); i++) {
            GDPCountry gdpCountry1 = listGDP.get(i);
            BubbleChartGDPCountryPageLoad pageLoad = listBubbleChart.get(i);
            Assertions.assertEquals(gdpCountry1.getGDP(), pageLoad.getGdpCountry().getGDP());
            Assertions.assertEquals(gdpCountry1.getGrowth(), pageLoad.getGdpCountry().getGrowth());
            Assertions.assertEquals(gdpCountry1.getShareOfWorld(), pageLoad.getGdpCountry().getShareOfWorld());
            Assertions.assertEquals(gdpCountry1.getPerCapita(), pageLoad.getGdpCountry().getPerCapita());
        }
    }

    /**
     * {@link GDPService#getDataForTopCountry(int)}
     */
    @Test
    public void testGetDataForTopCountry_2020() {
        int year = 2020;

        Map<String, Object> dataForBubbleChart = gdpService.getDataForTopCountry(year);

        List<GDPCountry> listTopCountryExpect = gdpCountryRepository.findByYearAndSort(year).subList(0, 4);
        Assertions.assertNotNull(dataForBubbleChart);
        List<GDPCountry> listTopCountryActual = (List<GDPCountry>) dataForBubbleChart.get("dataTopCountry");
        Assertions.assertEquals(listTopCountryExpect.size(), listTopCountryActual.size());
        compareListGDPCountry(listTopCountryActual, listTopCountryExpect);
    }

    /**
     * {@link GDPService#getDataForTopCountry(int)}
     */
    @Test
    public void testGetDataForTopCountry_2010() {
        int year = 2010;

        Map<String, Object> dataForBubbleChart = gdpService.getDataForTopCountry(year);

        List<GDPCountry> listTopCountryExpect = gdpCountryRepository.findByYearAndSort(year).subList(0, 4);
        Assertions.assertNotNull(dataForBubbleChart);
        List<GDPCountry> listTopCountryActual = (List<GDPCountry>) dataForBubbleChart.get("dataTopCountry");
        Assertions.assertEquals(listTopCountryExpect.size(), listTopCountryActual.size());
        compareListGDPCountry(listTopCountryActual, listTopCountryExpect);
    }

    /**
     * {@link GDPService#getDataForTopCountry(int)}
     */
    @Test
    public void testGetDataForTopCountry_2022() {
        int year = 2022;

        Map<String, Object> dataForBubbleChart = gdpService.getDataForTopCountry(year);

        List<GDPCountry> listTopCountryExpect = gdpCountryRepository.findByYearAndSort(year).subList(0, 4);
        Assertions.assertNotNull(dataForBubbleChart);
        List<GDPCountry> listTopCountryActual = (List<GDPCountry>) dataForBubbleChart.get("dataTopCountry");
        Assertions.assertEquals(listTopCountryExpect.size(), listTopCountryActual.size());
        compareListGDPCountry(listTopCountryActual, listTopCountryExpect);
    }

    /**
     * {@link GDPService#getDataForTopCountry(int)}
     */
    @Test
    public void testGetDataForTopCountry_2030() {
        int year = 2030;

        Map<String, Object> dataForBubbleChart = gdpService.getDataForTopCountry(year);

        Assertions.assertNotNull(dataForBubbleChart);
        List<GDPCountry> listTopCountryActual = (List<GDPCountry>) dataForBubbleChart.get("dataTopCountry");
        Assertions.assertEquals(0, listTopCountryActual.size());

    }

    /**
     * {@link GDPService#getDataForTopCountry(int)}
     */
    @Test
    public void testGetDataForTopCountry_2040() {
        int year = 2040;

        Map<String, Object> dataForBubbleChart = gdpService.getDataForTopCountry(year);

        Assertions.assertNotNull(dataForBubbleChart);
        List<GDPCountry> listTopCountryActual = (List<GDPCountry>) dataForBubbleChart.get("dataTopCountry");
        Assertions.assertEquals(0, listTopCountryActual.size());

    }


}
