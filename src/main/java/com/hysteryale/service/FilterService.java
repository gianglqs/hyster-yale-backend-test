package com.hysteryale.service;

import com.hysteryale.model.filters.FilterRow;
import com.hysteryale.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class FilterService {

    @Resource
    CompetitorPricingRepository competitorPricingRepository;

    @Resource
    ProductRepository productRepository;

    @Resource
    RegionRepository regionRepository;

    @Resource
    ShipmentRepository shipmentRepository;

    @Resource
    BookingRepository bookingRepository;
    @Resource
    CountryRepository countryRepository;
    @Resource
    CurrencyRepository currencyRepository;

    @Resource
    DealerRepository dealerRepository;

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
        filters.put("dealers", getAllDealerNames());

        return filters;
    }

    public Map<String, Object> getProductFilter() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("plants", getAllPlants());
        filters.put("metaSeries", getAllMetaSeries());
        filters.put("classes", getAllClasses());
        filters.put("segments", getAllSegments());
        filters.put("brands", getAllBrands());
        filters.put("truckType", getAllTruckTypes());
        filters.put("family", getAllFamily());

        return filters;
    }

    private List<Map<String, String>> getAllFamily() {
        List<Map<String, String>> familyMaps = new ArrayList<>();
        List<String> listFamily = productRepository.getAllFamily();
        listFamily.sort(String::compareTo);
        for (String m : listFamily) {
            Map<String, String> mMap = new HashMap<>();
            mMap.put("value", m);
            familyMaps.add(mMap);
        }
        return familyMaps;
    }

    private List<Map<String, String>> getAllTruckTypes() {
        List<Map<String, String>> truckTypeMaps = new ArrayList<>();
        List<String> truckTypes = productRepository.getAllTruckType();
        truckTypes.sort(String::compareTo);
        for (String m : truckTypes) {
            Map<String, String> mMap = new HashMap<>();
            mMap.put("value", m);
            truckTypeMaps.add(mMap);
        }
        return truckTypeMaps;
    }

    private List<Map<String, String>> getAllBrands() {
        List<Map<String, String>> brandMaps = new ArrayList<>();
        List<String> brands = productRepository.getAllBrands();
        brands.sort(String::compareTo);
        for (String m : brands) {
            Map<String, String> mMap = new HashMap<>();
            mMap.put("value", m);
            brandMaps.add(mMap);
        }
        return brandMaps;
    }

    private List<Map<String, Integer>> getRecentYears() {
        int year = LocalDate.now().getYear();

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
        List<String> classes = productRepository.getAllClass();
        classes.sort(String::compareTo);
        for (String m : classes) {
            if (m.equals("Class 5 not BT"))
                m = "Class 5 non BT";
            Map<String, String> mMap = new HashMap<>();
            mMap.put("value", m);
            classMap.add(mMap);
        }
        return classMap;
    }

    private List<Map<String, String>> getAllClasses() {
        List<Map<String, String>> classMap = new ArrayList<>();
        List<String> classes = productRepository.getAllClass();
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
        List<String> dealerNames = dealerRepository.getAllDealerName();
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
        List<String> plants = productRepository.getPlants();
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
        List<String> metaSeries = productRepository.getAllMetaSeries();
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
        List<String> modelList = bookingRepository.getAllModel();
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
        List<String> segments = productRepository.getAllSegments();
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
        for (String category : categories) {
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
        for (String s : series) {
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
        for (String country : countries) {
            listCountries.add(Map.of("value", country));
        }
        return listCountries;
    }


    public Map<String, Object> getProductDetailFilter(String modelCode, String metaSeries) {
        List<Map<String, String>> listOrderMaps = getListOrderNoByModelCode(modelCode, metaSeries);
        return Map.of("orderNos", listOrderMaps);


    }

    private List<Map<String, String>> getListOrderNoByModelCode(String modelCode, String metaSeries) {
        List<String> orderNos = bookingRepository.getOrderNosByModelCodeAndMetaSeries(modelCode, metaSeries);
        log.info(orderNos.toString());
        List<Map<String, String>> listResult = new ArrayList<>();
        orderNos.sort(String::compareToIgnoreCase);
        for (String orderNo : orderNos) {
            listResult.add(Map.of("value", orderNo));
        }
        return listResult;
    }

    public List<FilterRow> getCurrencyFilter() {
        List<String> currencyList = currencyRepository.getExistingCurrencies();
        List<FilterRow> filters = new ArrayList<>();
        for(String c : currencyList)
            filters.add(new FilterRow(c));

        return filters;
    }
}
