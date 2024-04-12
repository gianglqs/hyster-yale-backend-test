/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.service;

import com.hysteryale.model.Region;
import com.hysteryale.repository.RegionRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RegionService {

    @Resource
    RegionRepository regionRepository;

    public List<Map<String, String>> getAllRegionForFilter() {
        List<Map<String, String>> listRegion = new ArrayList<>();
        List<String> regions = regionRepository.findAllRegion();
        regions.sort(String::compareTo);
        for (String region : regions) {
            Map<String, String> mapRegion = new HashMap<>();
            mapRegion.put("value", region);
            listRegion.add(mapRegion);
        }
        return listRegion;
    }

    public Region getRegionByName(String strRegion) {
        return regionRepository.getRegionByName(strRegion);
    }

    public Region getRegionByShortName(String shortName) {
        return regionRepository.getRegionByShortName(shortName);
    }

    public Region getRegionInListRegionByShortName(List<Region> regions, String regionShortName) {
        for (Region region : regions) {
            if (region.getRegionShortName().equals(regionShortName))
                return region;
        }
        return null;
    }

}
