package com.hysteryale.service;

import com.hysteryale.model.competitor.CompetitorColor;
import com.hysteryale.model.competitor.CompetitorPricing;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.model.filters.SwotFilters;
import com.hysteryale.repository.CompetitorColorRepository;
import com.hysteryale.repository.CompetitorPricingRepository;
import com.hysteryale.utils.ConvertDataFilterUtil;
import org.junit.jupiter.api.Assertions;
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
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@SpringBootTest
@Transactional
public class IndicatorServiceTest {
    @Resource
    IndicatorService indicatorService;
    @Resource
    CompetitorPricingRepository competitorPricingRepository;
    @Resource
    CompetitorColorRepository competitorColorRepository;
    @Resource
    AuthenticationManager authenticationManager;

    @Test
    public void testGetCompetitorPriceForTableByFilter() throws ParseException {
        FilterModel filters = new FilterModel();

        // Expected value
        Map<String, Object> filterMap = ConvertDataFilterUtil.loadDataFilterIntoMap(filters);
        List<CompetitorPricing> competitorPricingList = competitorPricingRepository.findCompetitorByFilterForTable(
                (List<String>)   filterMap.get("regionFilter"),(List<String>) filterMap.get("plantFilter"), (List<String>) filterMap.get("metaSeriesFilter"),
                (List<String>)   filterMap.get("classFilter"),(List<String>) filterMap.get("modelFilter"),(Boolean) filterMap.get("ChineseBrandFilter"),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null: ((String)((List) filterMap.get("marginPercentageFilter")).get(0)),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null: ((Double)((List) filterMap.get("marginPercentageFilter")).get(1)), (Pageable) filterMap.get("pageable"));

        int totalCompetitor = competitorPricingRepository.getCountAll(
                (List<String>)   filterMap.get("regionFilter"),(List<String>) filterMap.get("plantFilter"), (List<String>) filterMap.get("metaSeriesFilter"),
                (List<String>)   filterMap.get("classFilter"),(List<String>) filterMap.get("modelFilter"),(Boolean) filterMap.get("ChineseBrandFilter"),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null: ((String)((List) filterMap.get("marginPercentageFilter")).get(0)),
                ((List) filterMap.get("marginPercentageFilter")).isEmpty() ? null: ((Double)((List) filterMap.get("marginPercentageFilter")).get(1)));

        //Assertions
        Map<String, Object> result = indicatorService.getCompetitorPriceForTableByFilter(filters);
        Assertions.assertNotNull(result.get("listCompetitor"));
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertNotNull(result.get("total"));

        Assertions.assertEquals(competitorPricingList.size(), ((List<Object>) result.get("listCompetitor")).size());
        Assertions.assertEquals(totalCompetitor, (Integer) result.get("totalItems"));
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
    public void testGetCompetitiveLandscape() {
        SwotFilters filters = new SwotFilters(
                "Asia",
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );

        // Expected value
        String regions = filters.getRegions();
        List<CompetitorPricing> expected =
                competitorPricingRepository.getDataForBubbleChart(Collections.singletonList(regions), null, null, null, null);

        // Assertions
        List<CompetitorPricing> result = indicatorService.getCompetitiveLandscape(filters);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(expected.size(), result.size());
    }

    @Test
    public void testGetCompetitorColor() {
        String groupName = "HYG";
        String colorCode = "#000000";
        competitorColorRepository.save(new CompetitorColor(groupName, colorCode));

        CompetitorColor result = indicatorService.getCompetitorColor(groupName);
        Assertions.assertEquals(groupName, result.getGroupName());
        Assertions.assertEquals(colorCode, result.getColorCode());
    }

    @Test
    public void testGetCompetitorColor_notFoundGroupName() {
        String groupName = "HYG";

        CompetitorColor result = indicatorService.getCompetitorColor(groupName);
        Assertions.assertEquals(groupName, result.getGroupName());
    }

    @Test
    public void testGetCompetitorById() {
        String groupName = "HYG";
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

        Assertions.assertNotNull(result);
        Assertions.assertEquals(expected.getTotalElements(), result.getTotalElements());
        Assertions.assertEquals(expected.getTotalPages(), result.getTotalPages());
        Assertions.assertEquals(expected.getNumber(), result.getNumber());
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
        String username = "admin@gmail.com";
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

        // Assertions
        Assertions.assertDoesNotThrow(() -> indicatorService.uploadForecastFile(file, authentication));
    }
}
