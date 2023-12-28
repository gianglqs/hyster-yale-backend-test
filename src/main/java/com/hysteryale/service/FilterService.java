package com.hysteryale.service;

import com.hysteryale.repository.*;
import com.hysteryale.repository.bookingorder.BookingOrderRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class FilterService {

    @Resource
    CompetitorPricingRepository competitorPricingRepository;

    @Resource
    ProductDimensionRepository productDimensionRepository;

    @Resource
    RegionRepository regionRepository;

    @Resource
    ShipmentRepository shipmentRepository;
    
    @Resource
    BookingOrderRepository bookingOrderRepository;
    @Resource
    CountryRepository countryRepository;

    public Map<String, Object> getCompetitorPricingFilter() {

        Map<String, Object> filters = new HashMap<>();
        filters.put("classes", getAllClassesForIndicators());
        filters.put("plants", getAllPlants());
        filters.put("metaSeries", getAllMetaSeries());
        filters.put("models", getAllModels());
        filters.put("chineseBrands", getChineseBrandFilter());
        filters.put("marginPercentageGrouping", getMarginPercentageGroup());
        //  filters.put("T&C", getTCForCompetitorPricing());
        filters.put("regions", getAllRegions());
        filters.put("dealers", getAllDealerNames());
        filters.put("series", getSeries());
        filters.put("categories", getCategories());
        filters.put("countries", getCountries());

        return filters;
    }

    public Map<String, Object> getOrderFilter() {

        Map<String, Object> filters = new HashMap<>();
        filters.put("regions", getAllRegions());
        filters.put("classes", getAllClasses());
        filters.put("plants", getAllPlants());
        filters.put("metaSeries", getAllMetaSeries());
        filters.put("models", getAllModels());
        filters.put("marginPercentageGroup", getMarginPercentageGroup());
        filters.put("AOPMarginPercentageGroup", getAOPMarginPercentageGroup());
        filters.put("dealers", getAllDealerNames());
        filters.put("segments", getAllSegments());

        return filters;
    }

    public Map<String, Object> getOutlierFilter() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("regions", getAllRegions());
        filters.put("classes", getAllClasses());
        filters.put("plants", getAllPlants());
        filters.put("metaSeries", getAllMetaSeries());
        filters.put("models", getAllModels());
        filters.put("marginPercentageGroup", getMarginPercentageGroup());
        filters.put("dealers", getAllDealerNames());
        filters.put("series", getSeries());

        return filters;
    }

    public Map<String, Object> getTrendsFilter() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("regions", getAllRegions());
        filters.put("plants", getAllPlants());
        filters.put("metaSeries", getAllMetaSeries());
        filters.put("classes", getAllClasses());
        filters.put("models", getAllModels());
        filters.put("segments", getAllSegments());
        filters.put("years", getRecentYears());

        return filters;
    }

    private List<Map<String, Integer>> getRecentYears() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);

        return List.of(
                Map.of("value", year - 1),
                Map.of("value", year),
                Map.of("value", year + 1)
                );
    }



    private List<Map<String, String>> getChineseBrandFilter() {
        List<Map<String, String>> result = new ArrayList<>();

        Map<String, String> nonChinese = new HashMap<>();
        nonChinese.put("value", "Non Chinese Brand");
        result.add(nonChinese);

        Map<String, String> Chinese = new HashMap<>();
        Chinese.put("value", "Chinese Brand");
        result.add(Chinese);

        return result;
    }

    private List<Map<String, String>> getAllClassesForIndicators() {
        List<Map<String, String>> classMap = new ArrayList<>();
        List<String> classes = productDimensionRepository.getAllClass();
        classes.sort(String::compareTo);
        for (String m : classes) {
            if(m.equals("Class 5 not BT"))
                m = "Class 5 non BT";
            Map<String, String> mMap = new HashMap<>();
            mMap.put("value", m);
            classMap.add(mMap);
        }
        return classMap;
    }

    private List<Map<String, String>> getAllClasses() {
        List<Map<String, String>> classMap = new ArrayList<>();
        List<String> classes = productDimensionRepository.getAllClass();
        classes.sort(String::compareTo);
        for (String m : classes) {
            Map<String, String> mMap = new HashMap<>();
            mMap.put("value", m);
            classMap.add(mMap);
        }
        return classMap;
    }

    private List<Map<String, String>> getAllDealerNames() {
        List<Map<String, String>> DealerNameMap = new ArrayList<>();
        List<String> dealerNames = shipmentRepository.findAllDealerName();
        dealerNames.sort(String::compareTo);
        for (String m : dealerNames) {
            Map<String, String> mMap = new HashMap<>();
            mMap.put("value", m);
            DealerNameMap.add(mMap);
        }
        return DealerNameMap;
    }

    private List<Map<String, String>> getAllPlants() {
        List<Map<String, String>> plantListMap = new ArrayList<>();
        List<String> plants = productDimensionRepository.getPlants();
        plants.sort(String::compareTo);
        for (String p : plants) {
            Map<String, String> pMap = new HashMap<>();
            pMap.put("value", p);

            plantListMap.add(pMap);
        }
        return plantListMap;
    }

    private List<Map<String, String>> getAllMetaSeries() {
        List<Map<String, String>> metaSeriesMap = new ArrayList<>();
        List<String> metaSeries = productDimensionRepository.getAllMetaSeries();
        metaSeries.sort(String::compareTo);
        for (String m : metaSeries) {
            Map<String, String> mMap = new HashMap<>();
            mMap.put("value", m);
            metaSeriesMap.add(mMap);
        }
        return metaSeriesMap;
    }

    private List<Map<String, String>> getAllModels() {
        List<Map<String, String>> result = new ArrayList<>();
        List<String> modelList = bookingOrderRepository.getAllModel();
        modelList.sort(String::compareTo);
        for (String model : modelList) {
            Map<String, String> map = new HashMap<>();
            map.put("value", model);
            result.add(map);
        }
        return result;
    }

    private List<Map<String, String>> getAllSegments() {
        List<Map<String, String>> segmentMap = new ArrayList<>();
        List<String> segments = productDimensionRepository.getAllSegments();
        segments.sort(String::compareTo);
        for (String m : segments) {
            Map<String, String> mMap = new HashMap<>();
            mMap.put("value", m);
            segmentMap.add(mMap);
        }
        return segmentMap;
    }

    /**
     * Get margin Percentage
     */
    private List<Map<String, String>> getMarginPercentageGroup() {
        List<Map<String, String>> result = new ArrayList<>();

        Map<String, String> MarginBelow10 = new HashMap<>();
        MarginBelow10.put("value", "<10% Margin");
        result.add(MarginBelow10);

        Map<String, String> MarginBelow20 = new HashMap<>();
        MarginBelow20.put("value", "<20% Margin");
        result.add(MarginBelow20);

        Map<String, String> MarginBelow30 = new HashMap<>();
        MarginBelow30.put("value", "<30% Margin");
        result.add(MarginBelow30);

        Map<String, String> MarginAbove30 = new HashMap<>();
        MarginAbove30.put("value", ">=30% Margin");
        result.add(MarginAbove30);

        Map<String, String> MarginVE = new HashMap<>();
        MarginVE.put("value", "<0 Margin");
        result.add(MarginVE);

        return result;
    }

    private List<Map<String, String>> getAOPMarginPercentageGroup() {
        List<Map<String, String>> result = new ArrayList<>();

        Map<String, String> marginBelow = new HashMap<>();
        marginBelow.put("value", "Below AOP Margin %");
        result.add(marginBelow);

        Map<String, String> marginAbove = new HashMap<>();
        marginAbove.put("value", "Above AOP Margin %");
        result.add(marginAbove);

        return result;
    }

    private List<Map<String, String>> getTCForCompetitorPricing() {
        List<Map<String, String>> result = new ArrayList<>();

        Map<String, String> on = new HashMap<>();
        on.put("value", "On");
        result.add(on);

        Map<String, String> off = new HashMap<>();
        off.put("value", "Off");
        result.add(off);

        return result;
    }


    private List<Map<String, String>> getAllRegions() {
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

    /**
     * Get Category value for Competitive Landscape filter
     */
    private List<Map<String, String>> getCategories() {
        List<Map<String, String>> listCategories = new ArrayList<>();
        List<String> categories = competitorPricingRepository.getDistinctCategory();
        categories.sort(String::compareTo);
        for(String category : categories) {
            listCategories.add(Map.of("value", category));
        }
        return listCategories;
    }

    /**
     * Get Series value for Competitive Landscape filter
     */
    private List<Map<String, String>> getSeries() {
        List<Map<String, String>> listSeries = new ArrayList<>();
        List<String> series = competitorPricingRepository.getDistinctSeries();
        series.sort(String::compareTo);
        for(String s : series) {
            listSeries.add(Map.of("value", s));
        }
        return listSeries;
    }

    /**
     * Get Countries value for Competitive Landscape filter
     */
    private List<Map<String, String>> getCountries() {
        List<Map<String, String>> listCountries = new ArrayList<>();
        List<String> countries = countryRepository.getAllCountryNames();
        countries.sort(String::compareTo);
        for(String country : countries) {
            listCountries.add(Map.of("value", country));
        }
        return listCountries;
    }



}
