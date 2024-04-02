package com.hysteryale.service;

import com.hysteryale.model.competitor.CompetitorColor;
import com.hysteryale.model.competitor.CompetitorPricing;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.model.filters.SwotFilters;
import com.hysteryale.repository.CompetitorColorRepository;
import com.hysteryale.repository.CompetitorPricingRepository;
import com.hysteryale.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest
@Transactional
@SuppressWarnings("unchecked")
@Slf4j
public class IndicatorServiceTest {
    @Resource
    IndicatorService indicatorService;
    @Resource
    CompetitorPricingRepository competitorPricingRepository;
    @Resource
    CompetitorColorRepository competitorColorRepository;
    @Resource
    AuthenticationManager authenticationManager;
    @Resource
    FileUploadService fileUploadService;

    FilterModel filters;
    SwotFilters swotFilters;

   @BeforeEach
   public void setUp() {
       resetFilters();
       resetSwotFilters();

       File uploadedFiles = new File("/tmp/UploadFiles/forecast_pricing");
       if(uploadedFiles.mkdirs())
           log.info("Create UploadFiles in /tmp");
       else
           log.info("Folders have already been created");
   }

    /**
     * Reset the filters to initial state
     */
   private void resetFilters() {
       filters = new FilterModel(
               "",
               new ArrayList<>(),
               new ArrayList<>(),
               new ArrayList<>(),
               new ArrayList<>(),
               new ArrayList<>(),
               new ArrayList<>(),
               new ArrayList<>(),
               "",
               "",
               "",
               "",
               "",
               null,
               1000,
               1,
               "",
               null,
               new ArrayList<>(),
               new ArrayList<>(),
               new ArrayList<>(),
               new ArrayList<>(),
               "",
               null);
   }

    /**
     * Reset SwotFilters to initial state
     */
   private void resetSwotFilters() {
       swotFilters = new SwotFilters(
               "Asia",
               new ArrayList<>(),
               new ArrayList<>(),
               new ArrayList<>(),
               new ArrayList<>()
       );
   }

    /**
     * Assert Total Value return by getCompetitorPricingForTable
     */
   private void assertTotalValue(CompetitorPricing totalResult, double actualTotal,
                                 double AOPFTotal, double LRFFTotal,
                                 double dealerStreetPricingTotal, double dealerHandlingCostTotal,
                                 double competitorPricingTotal, double averageDealerNetTotal) {
       Assertions.assertEquals(
               CurrencyFormatUtils.formatDoubleValue(actualTotal, CurrencyFormatUtils.decimalFormatFourDigits),
               CurrencyFormatUtils.formatDoubleValue(totalResult.getActual(), CurrencyFormatUtils.decimalFormatFourDigits)
       );
       Assertions.assertEquals(
               CurrencyFormatUtils.formatDoubleValue(AOPFTotal, CurrencyFormatUtils.decimalFormatFourDigits),
               CurrencyFormatUtils.formatDoubleValue(totalResult.getAOPF(), CurrencyFormatUtils.decimalFormatFourDigits)
       );
       Assertions.assertEquals(
               CurrencyFormatUtils.formatDoubleValue(LRFFTotal, CurrencyFormatUtils.decimalFormatFourDigits),
               CurrencyFormatUtils.formatDoubleValue(totalResult.getLRFF(), CurrencyFormatUtils.decimalFormatFourDigits)
       );
       Assertions.assertEquals(
               CurrencyFormatUtils.formatDoubleValue(dealerStreetPricingTotal, CurrencyFormatUtils.decimalFormatFourDigits),
               CurrencyFormatUtils.formatDoubleValue(totalResult.getDealerStreetPricing(), CurrencyFormatUtils.decimalFormatFourDigits)
       );
       Assertions.assertEquals(
               CurrencyFormatUtils.formatDoubleValue(dealerHandlingCostTotal, CurrencyFormatUtils.decimalFormatFourDigits),
               CurrencyFormatUtils.formatDoubleValue(totalResult.getDealerHandlingCost(), CurrencyFormatUtils.decimalFormatFourDigits)
       );
       Assertions.assertEquals(
               CurrencyFormatUtils.formatDoubleValue(competitorPricingTotal, CurrencyFormatUtils.decimalFormatFourDigits),
               CurrencyFormatUtils.formatDoubleValue(totalResult.getCompetitorPricing(), CurrencyFormatUtils.decimalFormatFourDigits)
       );
       Assertions.assertEquals(
               CurrencyFormatUtils.formatDoubleValue(averageDealerNetTotal, CurrencyFormatUtils.decimalFormatFourDigits),
               CurrencyFormatUtils.formatDoubleValue(totalResult.getAverageDN(), CurrencyFormatUtils.decimalFormatFourDigits)
       );
   }

    @Test
    public void testGetCompetitorPriceForTableByFilter_region() throws ParseException {
        resetFilters();

        String expectedRegion = "Asia";
        filters.setRegions(Collections.singletonList(expectedRegion));

        //Assertions
        Map<String, Object> result = indicatorService.getCompetitorPriceForTableByFilter(filters);
        Assertions.assertNotNull(result.get("listCompetitor"));
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertNotNull(result.get("total"));

        List<CompetitorPricing> competitorPricingList = (List<CompetitorPricing>) result.get("listCompetitor");
        CompetitorPricing totalResult = ((List<CompetitorPricing>) result.get("total")).get(0);
        Assertions.assertFalse(competitorPricingList.isEmpty());


        double actualTotal = 0.0;
        double AOPFTotal = 0.0;
        double LRFFTotal = 0.0;
        double dealerStreetPricingTotal = 0.0;
        double dealerHandlingCostTotal = 0.0;
        double competitorPricingTotal = 0.0;
        double averageDealerNetTotal = 0.0;
        for (CompetitorPricing cp : competitorPricingList) {
            Assertions.assertEquals(expectedRegion, cp.getCountry().getRegion().getRegionName());

            actualTotal += cp.getActual();
            AOPFTotal += cp.getAOPF();
            LRFFTotal += cp.getLRFF();
            dealerStreetPricingTotal += cp.getDealerStreetPricing();
            dealerHandlingCostTotal += cp.getDealerHandlingCost();
            competitorPricingTotal += cp.getCompetitorPricing();
            averageDealerNetTotal += cp.getAverageDN();
        }

        assertTotalValue(totalResult, actualTotal, AOPFTotal, LRFFTotal, dealerStreetPricingTotal, dealerHandlingCostTotal, competitorPricingTotal, averageDealerNetTotal);
    }

    @Test
    public void testGetCompetitorPriceForTableByFilter_plant() throws ParseException {
        resetFilters();

        String expectedPlant = "Greenville";
        filters.setPlants(Collections.singletonList(expectedPlant));

        //Assertions
        Map<String, Object> result = indicatorService.getCompetitorPriceForTableByFilter(filters);
        Assertions.assertNotNull(result.get("listCompetitor"));
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertNotNull(result.get("total"));

        List<CompetitorPricing> competitorPricingList = (List<CompetitorPricing>) result.get("listCompetitor");
        CompetitorPricing totalResult = ((List<CompetitorPricing>) result.get("total")).get(0);
        Assertions.assertFalse(competitorPricingList.isEmpty());

        double actualTotal = 0.0;
        double AOPFTotal = 0.0;
        double LRFFTotal = 0.0;
        double dealerStreetPricingTotal = 0.0;
        double dealerHandlingCostTotal = 0.0;
        double competitorPricingTotal = 0.0;
        double averageDealerNetTotal = 0.0;
        for (CompetitorPricing cp : competitorPricingList) {
            Assertions.assertEquals(expectedPlant, cp.getPlant());

            actualTotal += cp.getActual();
            AOPFTotal += cp.getAOPF();
            LRFFTotal += cp.getLRFF();
            dealerStreetPricingTotal += cp.getDealerStreetPricing();
            dealerHandlingCostTotal += cp.getDealerHandlingCost();
            competitorPricingTotal += cp.getCompetitorPricing();
            averageDealerNetTotal += cp.getAverageDN();
        }

        assertTotalValue(totalResult, actualTotal, AOPFTotal, LRFFTotal, dealerStreetPricingTotal, dealerHandlingCostTotal, competitorPricingTotal, averageDealerNetTotal);
    }

    @Test
    public void testGetCompetitorPriceForTableByFilter_metaSeries() throws ParseException {
        resetFilters();

        String expectedMetaSeries = "466";
        filters.setMetaSeries(Collections.singletonList(expectedMetaSeries));

        //Assertions
        Map<String, Object> result = indicatorService.getCompetitorPriceForTableByFilter(filters);
        Assertions.assertNotNull(result.get("listCompetitor"));
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertNotNull(result.get("total"));

        List<CompetitorPricing> competitorPricingList = (List<CompetitorPricing>) result.get("listCompetitor");
        CompetitorPricing totalResult = ((List<CompetitorPricing>) result.get("total")).get(0);
        Assertions.assertFalse(competitorPricingList.isEmpty());

        double actualTotal = 0.0;
        double AOPFTotal = 0.0;
        double LRFFTotal = 0.0;
        double dealerStreetPricingTotal = 0.0;
        double dealerHandlingCostTotal = 0.0;
        double competitorPricingTotal = 0.0;
        double averageDealerNetTotal = 0.0;

        for (CompetitorPricing cp : competitorPricingList) {
            Assertions.assertEquals(expectedMetaSeries, cp.getSeries().substring(1));

            actualTotal += cp.getActual();
            AOPFTotal += cp.getAOPF();
            LRFFTotal += cp.getLRFF();
            dealerStreetPricingTotal += cp.getDealerStreetPricing();
            dealerHandlingCostTotal += cp.getDealerHandlingCost();
            competitorPricingTotal += cp.getCompetitorPricing();
            averageDealerNetTotal += cp.getAverageDN();
        }

        assertTotalValue(totalResult, actualTotal, AOPFTotal, LRFFTotal, dealerStreetPricingTotal, dealerHandlingCostTotal, competitorPricingTotal, averageDealerNetTotal);
    }

    @Test
    public void testGetCompetitorPriceForTableByFilter_class() throws ParseException {
        resetFilters();

        String expectedClass = "Class 1";
        filters.setClasses(Collections.singletonList(expectedClass));

        //Assertions
        Map<String, Object> result = indicatorService.getCompetitorPriceForTableByFilter(filters);
        Assertions.assertNotNull(result.get("listCompetitor"));
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertNotNull(result.get("total"));

        List<CompetitorPricing> competitorPricingList = (List<CompetitorPricing>) result.get("listCompetitor");
        CompetitorPricing totalResult = ((List<CompetitorPricing>) result.get("total")).get(0);
        Assertions.assertFalse(competitorPricingList.isEmpty());

        double actualTotal = 0.0;
        double AOPFTotal = 0.0;
        double LRFFTotal = 0.0;
        double dealerStreetPricingTotal = 0.0;
        double dealerHandlingCostTotal = 0.0;
        double competitorPricingTotal = 0.0;
        double averageDealerNetTotal = 0.0;

        for (CompetitorPricing cp : competitorPricingList) {
            Assertions.assertEquals(expectedClass, cp.getClazz().getClazzName());

            actualTotal += cp.getActual();
            AOPFTotal += cp.getAOPF();
            LRFFTotal += cp.getLRFF();
            dealerStreetPricingTotal += cp.getDealerStreetPricing();
            dealerHandlingCostTotal += cp.getDealerHandlingCost();
            competitorPricingTotal += cp.getCompetitorPricing();
            averageDealerNetTotal += cp.getAverageDN();
        }

        assertTotalValue(totalResult, actualTotal, AOPFTotal, LRFFTotal, dealerStreetPricingTotal, dealerHandlingCostTotal, competitorPricingTotal, averageDealerNetTotal);
    }

    @Test
    public void testGetCompetitorPriceForTableByFilter_chineseBrand() throws ParseException {
        resetFilters();

        String expectedChineseBrand = "Chinese Brand";
        filters.setChineseBrand(expectedChineseBrand);

        //Assertions
        Map<String, Object> result = indicatorService.getCompetitorPriceForTableByFilter(filters);
        Assertions.assertNotNull(result.get("listCompetitor"));
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertNotNull(result.get("total"));

        List<CompetitorPricing> competitorPricingList = (List<CompetitorPricing>) result.get("listCompetitor");
        CompetitorPricing totalResult = ((List<CompetitorPricing>) result.get("total")).get(0);
        Assertions.assertFalse(competitorPricingList.isEmpty());

        double actualTotal = 0.0;
        double AOPFTotal = 0.0;
        double LRFFTotal = 0.0;
        double dealerStreetPricingTotal = 0.0;
        double dealerHandlingCostTotal = 0.0;
        double competitorPricingTotal = 0.0;
        double averageDealerNetTotal = 0.0;

        for (CompetitorPricing cp : competitorPricingList) {
            Assertions.assertTrue(cp.getChineseBrand());

            actualTotal += cp.getActual();
            AOPFTotal += cp.getAOPF();
            LRFFTotal += cp.getLRFF();
            dealerStreetPricingTotal += cp.getDealerStreetPricing();
            dealerHandlingCostTotal += cp.getDealerHandlingCost();
            competitorPricingTotal += cp.getCompetitorPricing();
            averageDealerNetTotal += cp.getAverageDN();
        }

        assertTotalValue(totalResult, actualTotal, AOPFTotal, LRFFTotal, dealerStreetPricingTotal, dealerHandlingCostTotal, competitorPricingTotal, averageDealerNetTotal);
    }

    @Test
    public void testGetCompetitorPriceForTableByFilter_marginPercentageGroup() throws ParseException {
        resetFilters();

        String expectedMarginPercentageGroup = "<10% Margin";
        filters.setChineseBrand(expectedMarginPercentageGroup);

        //Assertions
        Map<String, Object> result = indicatorService.getCompetitorPriceForTableByFilter(filters);
        Assertions.assertNotNull(result.get("listCompetitor"));
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertNotNull(result.get("total"));

        List<CompetitorPricing> competitorPricingList = (List<CompetitorPricing>) result.get("listCompetitor");
        CompetitorPricing totalResult = ((List<CompetitorPricing>) result.get("total")).get(0);
        Assertions.assertFalse(competitorPricingList.isEmpty());

        double actualTotal = 0.0;
        double AOPFTotal = 0.0;
        double LRFFTotal = 0.0;
        double dealerStreetPricingTotal = 0.0;
        double dealerHandlingCostTotal = 0.0;
        double competitorPricingTotal = 0.0;
        double averageDealerNetTotal = 0.0;

        for (CompetitorPricing cp : competitorPricingList) {
            Assertions.assertTrue(cp.getDealerPremiumPercentage() <= 0.1);

            actualTotal += cp.getActual();
            AOPFTotal += cp.getAOPF();
            LRFFTotal += cp.getLRFF();
            dealerStreetPricingTotal += cp.getDealerStreetPricing();
            dealerHandlingCostTotal += cp.getDealerHandlingCost();
            competitorPricingTotal += cp.getCompetitorPricing();
            averageDealerNetTotal += cp.getAverageDN();
        }

        assertTotalValue(totalResult, actualTotal, AOPFTotal, LRFFTotal, dealerStreetPricingTotal, dealerHandlingCostTotal, competitorPricingTotal, averageDealerNetTotal);
    }

    @Test
    public void testGetCompetitorPriceForTableByFilter_multipleFilters() throws ParseException {
        resetFilters();

        String expectedRegion = "Asia";
        String expectedPlant = "Greenville";
        String expectedClass = "Class 1";
        filters.setRegions(Collections.singletonList(expectedRegion));
        filters.setPlants(Collections.singletonList(expectedPlant));
        filters.setClasses(Collections.singletonList(expectedClass));

        //Assertions
        Map<String, Object> result = indicatorService.getCompetitorPriceForTableByFilter(filters);
        Assertions.assertNotNull(result.get("listCompetitor"));
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertNotNull(result.get("total"));

        List<CompetitorPricing> competitorPricingList = (List<CompetitorPricing>) result.get("listCompetitor");
        CompetitorPricing totalResult = ((List<CompetitorPricing>) result.get("total")).get(0);
        Assertions.assertFalse(competitorPricingList.isEmpty());

        double actualTotal = 0.0;
        double AOPFTotal = 0.0;
        double LRFFTotal = 0.0;
        double dealerStreetPricingTotal = 0.0;
        double dealerHandlingCostTotal = 0.0;
        double competitorPricingTotal = 0.0;
        double averageDealerNetTotal = 0.0;

        for (CompetitorPricing cp : competitorPricingList) {
            Assertions.assertEquals(expectedRegion, cp.getCountry().getRegion().getRegionName());
            Assertions.assertEquals(expectedPlant, cp.getPlant());
            Assertions.assertEquals(expectedClass, cp.getClazz().getClazzName());

            actualTotal += cp.getActual();
            AOPFTotal += cp.getAOPF();
            LRFFTotal += cp.getLRFF();
            dealerStreetPricingTotal += cp.getDealerStreetPricing();
            dealerHandlingCostTotal += cp.getDealerHandlingCost();
            competitorPricingTotal += cp.getCompetitorPricing();
            averageDealerNetTotal += cp.getAverageDN();
        }

        assertTotalValue(totalResult, actualTotal, AOPFTotal, LRFFTotal, dealerStreetPricingTotal, dealerHandlingCostTotal, competitorPricingTotal, averageDealerNetTotal);
    }

    @Test
    public void testGetCompetitorPricingAfterFilterAndGroupByRegion() throws ParseException {
        FilterModel filters = new FilterModel();

        // Expected value
        Map<String, Object> filterMap = ConvertDataFilterUtil.loadDataFilterIntoMap(filters);
        List<CompetitorPricing> expected = competitorPricingRepository.findCompetitorByFilterForLineChartRegion(
                (List<String>)   filterMap.get("regionFilter"),(List<String>) filterMap.get("plantFilter"), (List<String>) filterMap.get("metaSeriesFilter"),
                (List<String>)   filterMap.get("classFilter"),(List<String>) filterMap.get("modelFilter"),(Boolean) filterMap.get("ChineseBrandFilter"),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null: ((String)((List) filterMap.get("marginPercentageFilter")).get(0)),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null: ((Double)((List) filterMap.get("marginPercentageFilter")).get(1)));

        // Assertions
        List<CompetitorPricing> result = indicatorService.getCompetitorPricingAfterFilterAndGroupByRegion(filters);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(expected.size(), result.size());
    }

    @Test
    public void testGetCompetitorPricingAfterFilterAndGroupByPlant() throws ParseException {
        FilterModel filters = new FilterModel();

        // Expected value
        Map<String, Object> filterMap = ConvertDataFilterUtil.loadDataFilterIntoMap(filters);
        List<CompetitorPricing> expected = competitorPricingRepository.findCompetitorByFilterForLineChartPlant(
                (List<String>)   filterMap.get("regionFilter"),(List<String>) filterMap.get("plantFilter"), (List<String>) filterMap.get("metaSeriesFilter"),
                (List<String>)   filterMap.get("classFilter"),(List<String>) filterMap.get("modelFilter"),(Boolean) filterMap.get("ChineseBrandFilter"),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null: ((String)((List) filterMap.get("marginPercentageFilter")).get(0)),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null: ((Double)((List) filterMap.get("marginPercentageFilter")).get(1)));

        // Assertions
        List<CompetitorPricing> result = indicatorService.getCompetitorPricingAfterFilterAndGroupByPlant(filters);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(expected.size(), result.size());
    }

    @Test
    public void testGetCompetitiveLandscape_region() {
        // Expected value
        String expectedRegion = "Asia";
        swotFilters.setRegions(expectedRegion);
        List<CompetitorPricing> expected =
                competitorPricingRepository.getDataForBubbleChart(Collections.singletonList(expectedRegion), null, null, null, null);

        // Assertions
        List<CompetitorPricing> result = indicatorService.getCompetitiveLandscape(swotFilters);
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(expected.size(), result.size());
    }

    @Test
    public void testGetCompetitiveLandscape_country() {
        // Expected value
        String expectedCountry = "South Korea";
        swotFilters.setCountries(Collections.singletonList(expectedCountry));
        List<CompetitorPricing> expected =
                competitorPricingRepository.getDataForBubbleChart(Collections.singletonList("Asia"), Collections.singletonList(expectedCountry), null, null, null);

        // Assertions
        List<CompetitorPricing> result = indicatorService.getCompetitiveLandscape(swotFilters);
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(expected.size(), result.size());
    }

    @Test
    public void testGetCompetitiveLandscape_class() {
        // Expected value
        String expectedClass = "Class 1";
        swotFilters.setClasses(Collections.singletonList(expectedClass));
        List<CompetitorPricing> expected = competitorPricingRepository.getDataForBubbleChart(
                Collections.singletonList("Asia"),
                null,
                Collections.singletonList(expectedClass),
                null,
                null);

        // Assertions
        List<CompetitorPricing> result = indicatorService.getCompetitiveLandscape(swotFilters);
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(expected.size(), result.size());
    }

    @Test
    public void testGetCompetitiveLandscape_category() {
        // Expected value
        String expectedCategory = "Lead Acid";
        swotFilters.setCategories(Collections.singletonList(expectedCategory));
        List<CompetitorPricing> expected = competitorPricingRepository.getDataForBubbleChart(
                Collections.singletonList("Asia"),
                null,
                null,
                Collections.singletonList(expectedCategory),
                null);

        // Assertions
        List<CompetitorPricing> result = indicatorService.getCompetitiveLandscape(swotFilters);
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(expected.size(), result.size());
    }

    @Test
    public void testGetCompetitiveLandscape_series() {
        // Expected value
        String expectedSeries = "A3C4";
        swotFilters.setSeries(Collections.singletonList(expectedSeries));
        List<CompetitorPricing> expected = competitorPricingRepository.getDataForBubbleChart(
                Collections.singletonList("Asia"),
                null,
                null,
                null,
                Collections.singletonList(expectedSeries));

        // Assertions
        List<CompetitorPricing> result = indicatorService.getCompetitiveLandscape(swotFilters);
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(expected.size(), result.size());
    }

    @Test
    public void testGetCompetitorColor() {
        String groupName = "HYG 123 123";
        String colorCode = "#000000";
        competitorColorRepository.save(new CompetitorColor(groupName, colorCode));

        CompetitorColor result = indicatorService.getCompetitorColor(groupName);
        Assertions.assertEquals(groupName, result.getGroupName());
        Assertions.assertEquals(colorCode, result.getColorCode());
    }

    @Test
    public void testGetCompetitorColor_notFoundGroupName() {
        String groupName = "HYG 123 123 123";

        CompetitorColor result = indicatorService.getCompetitorColor(groupName);
        Assertions.assertEquals(groupName, result.getGroupName());
    }

    @Test
    public void testGetCompetitorById() {
        String groupName = "HYG 123 123";
        String colorCode = "#000000";
        CompetitorColor competitorColor = competitorColorRepository.save(new CompetitorColor(groupName, colorCode));

        int id = competitorColor.getId();
        CompetitorColor result = indicatorService.getCompetitorById(id);

        Assertions.assertEquals(id, result.getId());
        Assertions.assertEquals(groupName, result.getGroupName());
        Assertions.assertEquals(colorCode, result.getColorCode());
    }

    @Test
    public void testGetCompetitorById_notFound() {
        ResponseStatusException exception =  Assertions.assertThrows(
                ResponseStatusException.class,
                () -> indicatorService.getCompetitorById(123123123)
        );

        Assertions.assertEquals(404, exception.getStatus().value());
        Assertions.assertEquals("Competitor Color not found", exception.getReason());
    }

    @Test
    public void testSearchCompetitorColor() {
        String search = "H";
        int pageNo = 1;
        int perPage = 100;

        // Expected value
        Pageable pageable = PageRequest.of(0, perPage, Sort.by("groupName").ascending());
        Page<CompetitorColor> expected = competitorColorRepository.searchCompetitorColor(search, pageable);

        // Assertions
        Page<CompetitorColor> result = indicatorService.searchCompetitorColor(search, pageNo, perPage);

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(expected.getTotalElements(), result.getTotalElements());
        Assertions.assertEquals(expected.getTotalPages(), result.getTotalPages());
        Assertions.assertEquals(expected.getNumber(), result.getNumber());

        for(CompetitorColor color : result) {
            Assertions.assertTrue(color.getGroupName().contains(search));
        }
    }

    @Test
    public void testUpdateCompetitorColor() {
        // Expected value
        CompetitorColor newCompetitorColor = competitorColorRepository.save(new CompetitorColor("HYG", "#000000"));
        CompetitorColor modifiedCompetitorColor = new CompetitorColor(newCompetitorColor.getId(), "HYG", "#111111");

        // Assertions
        indicatorService.updateCompetitorColor(modifiedCompetitorColor);
        CompetitorColor dbCompetitorColor = competitorColorRepository.getReferenceById(newCompetitorColor.getId());
        Assertions.assertEquals(modifiedCompetitorColor.getGroupName(), dbCompetitorColor.getGroupName());
        Assertions.assertEquals(modifiedCompetitorColor.getColorCode(), dbCompetitorColor.getColorCode());
    }

    @Test
    public void testUpdateCompetitorColor_notFoundId() {
        CompetitorColor modifiedCompetitorColor = new CompetitorColor(123123123, "HYG", "#111111");

        ResponseStatusException exception =
                Assertions.assertThrows(
                        ResponseStatusException.class,
                        () -> indicatorService.updateCompetitorColor( modifiedCompetitorColor)
                );

        Assertions.assertEquals(404, exception.getStatus().value());
        Assertions.assertEquals("Competitor Color not found", exception.getReason());
    }
    @Test
    public void testUploadForecastFile() throws IOException {
        // Set up Authentication
        String username = "user1@gmail.com";
        String password = "123456";

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        password
                )
        );

        // Set up uploaded file
        org.springframework.core.io.Resource fileResource = new ClassPathResource("import_files/forecast_pricing/Forecast Database Dynamic Pricing.xlsx");
        Assertions.assertNotNull(fileResource);

        MultipartFile file = new MockMultipartFile(
                "file",
                fileResource.getFilename(),
                MediaType.MULTIPART_FORM_DATA_VALUE,
                fileResource.getInputStream()
        );

        String targetFolder = EnvironmentUtils.getEnvironmentValue("upload_files.forecast_pricing");
        String excelFileExtension = FileUtils.EXCEL_FILE_EXTENSION;
        // Assertions
        Assertions.assertDoesNotThrow(() -> fileUploadService.saveFileUploaded(file, authentication, targetFolder, excelFileExtension, ModelUtil.FORECAST_PRICING));
    }

    @Test
    public void testImportIndicatorsFromFile() throws Exception {
        String filePath = "import_files/competitor_pricing/Competitor Pricing Database.xlsx";
        indicatorService.importIndicatorsFromFile(filePath, "");// TODO: recheck

        InputStream is = new FileInputStream(filePath);
        XSSFWorkbook workbook = new XSSFWorkbook(is);
        Sheet sheet = workbook.getSheetAt(0);

        String country;
        String competitorName;
        String group;
        String region;
        String clazz;
        double marketShare;
        double price;
        int leadTime;
        String category;
        String series;
        String model;

        HashMap<String, Integer> columns = getColumnNameInFile(sheet.getRow(0));
        Pattern seriesPattern = Pattern.compile("(.{4})/(.{4})");
        Matcher matcher;

        Random random = new Random();
        for(int i = 0; i < 10; i++) {
            int nextRow = random.nextInt(356);
            if(nextRow != 0) {
                Row row = sheet.getRow(nextRow);
                country = row.getCell(columns.get("Country"), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
                competitorName = row.getCell(columns.get("Brand"), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
                group = row.getCell(columns.get("Group"), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
                region = row.getCell(columns.get("Region"), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
                clazz = row.getCell(columns.get("Class"), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
                marketShare = row.getCell(columns.get("Normalized Market Share")).getNumericCellValue();
                price = row.getCell(columns.get("Price (USD)")).getNumericCellValue();
                leadTime = (int) row.getCell(columns.get("Lead Time")).getNumericCellValue();
                category = row.getCell(columns.get("Category"), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
                series = row.getCell(columns.get("HYG Series"), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
                model = row.getCell(columns.get("Model"), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();

                matcher = seriesPattern.matcher(series);
                if(matcher.find()) {
                    series = matcher.group(1);
                }

                Optional<CompetitorPricing> cp = competitorPricingRepository.getCompetitorPricing(country, clazz, category, series, competitorName, model);

                if(cp.isPresent())
                    assertCompetitorPricing(cp.get(), country, competitorName, group, region, clazz , marketShare, price, leadTime, category, series, model);
            }
        }
    }

    private void assertCompetitorPricing(CompetitorPricing cp, String country, String competitorName,
                                         String group, String region, String clazz ,
                                         double marketShare, double price, int leadTime,
                                         String category, String series, String model) {
       Assertions.assertEquals(country, cp.getCountry().getCountryName());
       Assertions.assertEquals(competitorName, cp.getCompetitorName());
       Assertions.assertEquals(group, cp.getColor().getGroupName());
       Assertions.assertEquals(region, cp.getCountry().getRegion().getRegionName());
       Assertions.assertEquals(clazz, cp.getClazz().getClazzName());
       Assertions.assertEquals(marketShare, cp.getMarketShare());
       Assertions.assertEquals(price, cp.getCompetitorPricing());
       Assertions.assertEquals(leadTime, cp.getCompetitorLeadTime());
       Assertions.assertEquals(category, cp.getCategory());
       Assertions.assertEquals(series, cp.getSeries());
       Assertions.assertEquals(model, cp.getModel());
    }

    private HashMap<String, Integer> getColumnNameInFile(Row row) {
       HashMap<String, Integer> columns = new HashMap<>();
       for(Cell cell : row) {
           columns.put(cell.getStringCellValue(), cell.getColumnIndex());
       }
       return columns;
    }
}
