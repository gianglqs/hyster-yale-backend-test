package com.hysteryale.controller;

import com.hysteryale.service.WebScrapingService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping(path = "/web-scraping")
public class WebScrapingController {
    @Resource
    WebScrapingService webScrapingService;

    @GetMapping(path = "/scrape-data")
    public Map<String, Object> scrapData(@RequestParam String url) {
        return Map.of("productList", webScrapingService.scrapeData(url));
    }
}
