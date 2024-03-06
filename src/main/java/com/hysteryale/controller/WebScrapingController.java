package com.hysteryale.controller;

import com.hysteryale.service.WebScrapingService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@RestController
@RequestMapping(path = "/web-scraping")
public class WebScrapingController {
    @Resource
    WebScrapingService webScrapingService;

    @GetMapping(path = "/scrape-data", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<InputStreamResource> scrapData() throws FileNotFoundException {
        String url = "https://cellphones.com.vn/mobile.html";

        String fileLocation = webScrapingService.scrapData(url);

        File file = new File(fileLocation);
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + file.getName());
        headers.add("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
