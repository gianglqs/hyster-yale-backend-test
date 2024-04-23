/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hysteryale.model.Country;
import com.hysteryale.model.GDPCountry;
import com.hysteryale.model.embedId.GDPId;
import com.hysteryale.model.payLoad.BubbleChartGDPCountryPageLoad;
import com.hysteryale.repository.CountryRepository;
import com.hysteryale.repository.GDPCountryRepository;
import com.hysteryale.service.GDPService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Service
public class GDPServiceImp implements GDPService {

    @Resource
    private CountryRepository countryRepository;

    @Resource
    private GDPCountryRepository GDPCountryRepository;


    private final String baseUrl = "https://api.worldbank.org/v2/countries";


    @Override
    public void collectData() {
        List<Country> countries = countryRepository.findCountryHasDealer();

        List<GDPCountry> listGDPCountries = new ArrayList<>();
        List<GDPCountry> listGDPWorld = getGDPOfWorld();

        for (Country country : countries) {
            if (country.getCode() == null) continue;

            List<GDPCountry> listGDPCountriesOfOneCountry = new ArrayList<>();

            getIndicatorData(country, "NY.GDP.MKTP.CD", listGDPCountriesOfOneCountry); //GDP
            getIndicatorData(country, "NY.GDP.PCAP.CD", listGDPCountriesOfOneCountry);//GDP Per capita
            getIndicatorData(country, "NY.GDP.MKTP.KD.ZG", listGDPCountriesOfOneCountry);// GDP Growth
            getGDPShareOfWorld(listGDPCountriesOfOneCountry, listGDPWorld); // share of world

            listGDPCountries.addAll(listGDPCountriesOfOneCountry);

        }
        GDPCountryRepository.saveAll(listGDPCountries);
    }

    @Override
    public Map<String, Object> getDataForTable(int year, int pageNo, int perPage) {
        Map<String, Object> result = new HashMap<>();
        Pageable pageable = PageRequest.of(pageNo == 0 ? pageNo : pageNo - 1, perPage == 0 ? 100 : perPage);

        List<GDPCountry> listGDPCountriesByYear = GDPCountryRepository.findByYear(year, pageable);
        long total = GDPCountryRepository.countByYear(year);

        if (total == 0) {
            listGDPCountriesByYear = getNoneData(pageable);
            total = GDPCountryRepository.countByYear(2022);
        }

        result.put("dataTable", listGDPCountriesByYear);
        result.put("totalItems", total);

        return result;
    }

    @Override
    public List<BubbleChartGDPCountryPageLoad> getDataForBubbleChart(int year) {
        List<BubbleChartGDPCountryPageLoad> result = new ArrayList<>();

        List<GDPCountry> listGDPCountriesByYear = GDPCountryRepository.findByYearAndSort(year);

        if (listGDPCountriesByYear.isEmpty()) {
            return List.of();
        }

        List<GDPCountry> top4GDPCountriesByYear = listGDPCountriesByYear.subList(0, 3);
        for (GDPCountry gdpCountry : top4GDPCountriesByYear) {
            BubbleChartGDPCountryPageLoad payload = new BubbleChartGDPCountryPageLoad();
            payload.setGdpCountry(gdpCountry);
            payload.setColor("#DABBF7");
            payload.setRank("Rank 1 - 3");
            result.add(payload);
        }

        List<GDPCountry> top5to15GDPCountriesByYear = listGDPCountriesByYear.subList(3, 9);
        for (GDPCountry gdpCountry : top5to15GDPCountriesByYear) {
            BubbleChartGDPCountryPageLoad payload = new BubbleChartGDPCountryPageLoad();
            payload.setGdpCountry(gdpCountry);
            payload.setColor("#80ACFF");
            payload.setRank("Rank 3 - 10");
            result.add(payload);
        }

        List<GDPCountry> top16to50GDPCountriesByYear = listGDPCountriesByYear.subList(9, listGDPCountriesByYear.size());
        for (GDPCountry gdpCountry : top16to50GDPCountriesByYear) {
            BubbleChartGDPCountryPageLoad payload = new BubbleChartGDPCountryPageLoad();
            payload.setGdpCountry(gdpCountry);
            payload.setColor("#FAD6D6");
            payload.setRank("Rank 11 - 50");
            result.add(payload);
        }
        Collections.reverse(result);

        return result;
    }

    private List<GDPCountry> getNoneData(Pageable pageable) {

        List<GDPCountry> listGDPCountriesByYear = GDPCountryRepository.findByYear(2022, pageable);
        for (GDPCountry gdpCountry : listGDPCountriesByYear) {
            gdpCountry.setGDP(0);
            gdpCountry.setGrowth(0);
            gdpCountry.setPerCapita(0);
            gdpCountry.setShareOfWorld(0);
        }
        return listGDPCountriesByYear;
    }

    @Override
    public Map<String, Object> getDataForTopCountry(int year) {
        Map<String, Object> result = new HashMap<>();

        List<GDPCountry> listGDPCountriesByYear = GDPCountryRepository.findByYearAndSort(year);

        result.put("dataTopCountry", listGDPCountriesByYear.isEmpty() ? List.of() : listGDPCountriesByYear.subList(0, 4));

        return result;
    }


    private void getGDPShareOfWorld(List<GDPCountry> listGDPCountriesOfOneCountry, List<GDPCountry> listGDPWorld) {
        for (GDPCountry gdpCountry : listGDPCountriesOfOneCountry) {
            GDPCountry gdpWorld = getGDPCountryByYear(listGDPWorld, gdpCountry.getGDPId().getYears());
            if (gdpWorld != null) {
                gdpCountry.setShareOfWorld(gdpCountry.getGDP() / gdpWorld.getGDP());
            }
        }
    }

    private void getIndicatorData(Country country, String indicator, List<GDPCountry> listGDPCountries) {

        String url = baseUrl + "/" + country.getCode() + "/indicators/" + indicator + "?format=json&per_page=24";
        ResponseEntity<String> response = new RestTemplate().getForEntity(url, String.class);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode dataNode = root.get(1);

            for (JsonNode node : dataNode) {
                int year = node.get("date").asInt();
                GDPCountry gdpCountry = getGDPCountryByYear(listGDPCountries, year);
                if (gdpCountry == null) {
                    gdpCountry = new GDPCountry();
                    GDPId gdpId = new GDPId();
                    gdpId.setYears(year);
                    gdpId.setCountry(country);
                    gdpCountry.setGDPId(gdpId);
                    listGDPCountries.add(gdpCountry);
                }

                JsonNode nodeValue = node.get("value");
                if (nodeValue.asText().equalsIgnoreCase("null")) {
                    listGDPCountries.remove(gdpCountry);
                    continue;
                }
                double value = nodeValue.asDouble();
                switch (indicator) {
                    case "NY.GDP.MKTP.CD":
                        gdpCountry.setGDP(value);
                        break;
                    case "NY.GDP.PCAP.CD":
                        gdpCountry.setPerCapita(value);
                        break;
                    case "NY.GDP.MKTP.KD.ZG":
                        gdpCountry.setGrowth(value);
                }
            }
        } catch (Exception e) {
            log.error(e.toString());
        }
    }

    private List<GDPCountry> getGDPOfWorld() {
        List<GDPCountry> listGDPWorld = new ArrayList<>();
        String url = baseUrl + "/WLD/indicators/NY.GDP.MKTP.CD?format=json&per_page=24";
        ResponseEntity<String> response = new RestTemplate().getForEntity(url, String.class);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode dataNode = root.get(1);

            for (JsonNode node : dataNode) {
                int year = node.get("date").asInt();

                GDPCountry gdpWorldPerYear = new GDPCountry();
                GDPId gdpId = new GDPId();
                gdpId.setYears(year);
                gdpWorldPerYear.setGDPId(gdpId);
                listGDPWorld.add(gdpWorldPerYear);

                JsonNode nodeValue = node.get("value");

                if (nodeValue == null)
                    continue;

                gdpWorldPerYear.setGDP(nodeValue.asDouble());

            }
        } catch (
                Exception e) {
            log.error(e.toString());
        }
        return listGDPWorld;
    }

    private GDPCountry getGDPCountryByYear(List<GDPCountry> listGDPCountries, int year) {
        for (GDPCountry gdpCountry : listGDPCountries) {
            if (gdpCountry.getGDPId().getYears() == year)
                return gdpCountry;
        }
        return null;
    }
}
