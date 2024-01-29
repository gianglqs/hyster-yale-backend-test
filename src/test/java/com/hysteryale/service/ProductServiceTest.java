package com.hysteryale.service;

import com.hysteryale.model.Product;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.repository.ProductDimensionRepository;
import com.hysteryale.utils.ConvertDataFilterUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class ProductServiceTest {
    @Resource
    ProductDimensionService productDimensionService;
    @Resource
    ProductDimensionRepository productDimensionRepository;

    @Test
    public void testGetAllMetaSeries() {
        int expectedSize = productDimensionRepository.getAllMetaSeries().size();
        List<Map<String, String>> result = productDimensionService.getAllMetaSeries();

        Assertions.assertNotNull(result);
        Assertions.assertEquals(expectedSize, result.size());
    }

    @Test
    public void testGetAllPlants() {
        int expectedSize = productDimensionRepository.getPlants().size();
        List<Map<String, String>> result = productDimensionService.getAllPlants();

        Assertions.assertNotNull(result);
        Assertions.assertEquals(expectedSize, result.size());
    }

    @Test
    public void testGetAllClasses() {
        int expectedSize = productDimensionRepository.getAllClass().size();
        List<Map<String, String>> result = productDimensionService.getAllClasses();

        Assertions.assertNotNull(result);
        Assertions.assertEquals(expectedSize, result.size());
    }

    @Test
    public void testGetAllSegments() {
        int expectedSize = productDimensionRepository.getAllSegments().size();
        List<Map<String, String>> result = productDimensionService.getAllSegments();

        Assertions.assertNotNull(result);
        Assertions.assertEquals(expectedSize, result.size());
    }

    @Test
    public void testGetProductDimensionByModelCode() {
        String modelCode = "J40XN";

        Product result = productDimensionService.getProductDimensionByModelCode(modelCode);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(modelCode, result.getModelCode());
    }

    @Test
    public void testGetProductDimensionByModelCode_notFound() {
        Product result = productDimensionService.getProductDimensionByModelCode("asdbasjhdb");
        Assertions.assertNull(result);
    }

    @Test
    public void testGetModelFromMetaSeries() {
        String metaSeries = "935";

        String modelCode = productDimensionService.getModelFromMetaSeries(metaSeries);
        Assertions.assertNotNull(modelCode);
        Assertions.assertEquals("J40XN", modelCode);
    }

    @Test
    public void testGetModelFromMetaSeries_notFound() {
        String modelCode = productDimensionService.getModelFromMetaSeries("askjdh");
        Assertions.assertNull(modelCode);
    }

    @Test
    public void testGetDataByFilter() throws ParseException {
        FilterModel filters = new FilterModel();

        Map<String, Object> filterMap = ConvertDataFilterUtil.loadDataFilterIntoMap(filters);
        List<Product> getData = productDimensionRepository.getDataByFilter(
                (String) filterMap.get("modelCodeFilter"), (List<String>) filterMap.get("plantFilter"),
                (List<String>) filterMap.get("metaSeriesFilter"), (List<String>) filterMap.get("classFilter"),
                (List<String>) filterMap.get("segmentFilter"), (List<String>) filterMap.get("brandFilter"),
                (List<String>) filterMap.get("familyFilter"),(Pageable) filterMap.get("pageable")
        );
        long countAll = productDimensionRepository.countAll(
                (String) filterMap.get("modelCode"), (List<String>) filterMap.get("plantFilter"),
                (List<String>) filterMap.get("metaSeriesFilter"), (List<String>) filterMap.get("classFilter"),
                (List<String>) filterMap.get("segmentFilter"), (List<String>) filterMap.get("brandFilter"),
                (List<String>) filterMap.get("truckType"), (List<String>) filterMap.get("familyFilter"));

        Map<String, Object> result = productDimensionService.getDataByFilter(filters);

        Assertions.assertNotNull(result.get("listData"));
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertEquals(getData.size(), ((List<Object>) result.get("listData")).size());
        Assertions.assertEquals(countAll, (long) result.get("totalItems"));
    }
}
