package com.hysteryale.service;

import com.hysteryale.model.Region;
import com.hysteryale.repository.RegionRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class RegionServiceTest {
    @Resource
    RegionService regionService;
    @Resource
    RegionRepository regionRepository;

    @Test
    public void testGetAllRegionForFilter() {
        int expectedSize = regionRepository.findAllRegion().size();
        List<Map<String, String>> result = regionService.getAllRegionForFilter();

        Assertions.assertNotNull(result);
        Assertions.assertEquals(expectedSize, result.size());
    }

    @Test
    public void testGetRegionByName() {
        String region = "Asia";

        Region result = regionService.getRegionByName(region);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(region, result.getRegion());
    }

    @Test
    public void testGetRegionByName_notFound() {
        Region result = regionService.getRegionByName("asdasd");
        Assertions.assertNull(result);
    }

    @Test
    public void testGetRegionByShortName() {
        String regionShortName = "A";

        Region result = regionService.getRegionByShortName(regionShortName);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(regionShortName, result.getRegionShortName());
    }

    @Test
    public void testGetRegionShortName_notFound() {
        Region result = regionService.getRegionByShortName("ASDASD");
        Assertions.assertNull(result);
    }
}
