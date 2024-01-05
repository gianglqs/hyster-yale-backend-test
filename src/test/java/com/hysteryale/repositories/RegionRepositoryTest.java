package com.hysteryale.repositories;

import com.hysteryale.model.Region;
import com.hysteryale.repository.RegionRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import javax.annotation.Resource;
import java.util.List;

@DataJpaTest
public class RegionRepositoryTest {

    @Resource
    RegionRepository regionRepository;

    @Test
    void findAllRegion(){
        List<String> getAll = regionRepository.findAllRegion();
        Assertions.assertEquals(getAll.size(), 4);
    }

    @Test
    void getRegionByName(){
        Region getExistRegionByName = regionRepository.getRegionByName("Asia");
        Region getNotExistRegionByName = regionRepository.getRegionByName("BINHDINH");

        Assertions.assertEquals(getNotExistRegionByName, null);
        Assertions.assertNotEquals(getExistRegionByName, null);
    }

    @Test
    void getRegionByShortName(){
        Region getExistRegionByShortName = regionRepository.getRegionByShortName("A");
        Region getNotExistRegionByShortName = regionRepository.getRegionByShortName("B");

        Assertions.assertEquals(getNotExistRegionByShortName, null);
        Assertions.assertNotEquals(getExistRegionByShortName, null);
    }

}
