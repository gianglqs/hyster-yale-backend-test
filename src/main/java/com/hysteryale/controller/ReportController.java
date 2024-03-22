package com.hysteryale.controller;

import com.hysteryale.model.reports.CompareCurrencyRequest;
import com.hysteryale.service.ExchangeRateService;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/reports")
public class ReportController {
    @Resource
    ExchangeRateService exchangeRateService;

    @PostMapping(path = "/compareCurrency")
    public Map<String, Object> compareCurrency(@RequestBody CompareCurrencyRequest request) {
        if(request.isFromRealTime())
            return Map.of("compareCurrency", exchangeRateService.compareCurrencyFromAPI(request));
        else
            return Map.of("compareCurrency", exchangeRateService.compareCurrency(request));
    }

    @PostMapping(path = "/uploadExchangeRate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> uploadExchangeRateFile(@RequestBody MultipartFile file, Authentication authentication) throws Exception {
        exchangeRateService.importExchangeRateFromFile(file, authentication);
        return Map.of(
                "message", "Upload successfully"
        );
    }
}
