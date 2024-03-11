package com.hysteryale.service;

import com.hysteryale.model.Product;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.repository.ClazzRepository;
import com.hysteryale.repository.ProductRepository;
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
    ProductService productService;
    @Resource
    ProductRepository productRepository;
    @Resource
    ClazzRepository clazzRepository;

    @Test
    public void testGetAllMetaSeries() {
        int expectedSize = productRepository.getAllMetaSeries().size();
        List<Map<String, String>> result = productService.getAllMetaSeries();

        Assertions.assertNotNull(result);
        Assertions.assertEquals(expectedSize, result.size());
    }

    @Test
    public void testGetAllPlants() {
        int expectedSize = productRepository.getPlants().size();
        List<Map<String, String>> result = productService.getAllPlants();

        Assertions.assertNotNull(result);
        Assertions.assertEquals(expectedSize, result.size());
    }

    @Test
    public void testGetAllSegments() {
        int expectedSize = productRepository.getAllSegments().size();
        List<Map<String, String>> result = productService.getAllSegments();

        Assertions.assertNotNull(result);
        Assertions.assertEquals(expectedSize, result.size());
    }

    @Test
    public void testGetDataByFilter() throws ParseException {
        FilterModel filters = new FilterModel();

        Map<String, Object> filterMap = ConvertDataFilterUtil.loadDataFilterIntoMap(filters);
        List<Product> getData = productRepository.getDataByFilter(
                (String) filterMap.get("modelCodeFilter"), (List<String>) filterMap.get("plantFilter"),
                (List<String>) filterMap.get("metaSeriesFilter"), (List<String>) filterMap.get("classFilter"),
                (List<String>) filterMap.get("segmentFilter"), (List<String>) filterMap.get("brandFilter"),
                (List<String>) filterMap.get("truckTypeFilter"),
                (List<String>) filterMap.get("familyFilter"),(Pageable) filterMap.get("pageable")
        );
        long countAll = productRepository.countAll(
                (String) filterMap.get("modelCode"), (List<String>) filterMap.get("plantFilter"),
                (List<String>) filterMap.get("metaSeriesFilter"), (List<String>) filterMap.get("classFilter"),
                (List<String>) filterMap.get("segmentFilter"), (List<String>) filterMap.get("brandFilter"),
                (List<String>) filterMap.get("truckTypeFilter"), (List<String>) filterMap.get("familyFilter"));

        Map<String, Object> result = productService.getDataByFilter(filters);

        Assertions.assertNotNull(result.get("listData"));
        Assertions.assertNotNull(result.get("totalItems"));
        Assertions.assertEquals(getData.size(), ((List<Object>) result.get("listData")).size());
        Assertions.assertEquals(countAll, (long) result.get("totalItems"));
    }
}
