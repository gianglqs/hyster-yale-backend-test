package com.hysteryale.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hysteryale.model.marginAnalyst.MarginAnalystData;
import com.hysteryale.model.marginAnalyst.MarginAnalystSummary;
import com.hysteryale.service.MarginAnalystService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController

@Slf4j
public class MarginAnalystController {

    @Resource
    MarginAnalystService marginAnalystService;

    @PostMapping(path = "/marginAnalystData", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<MarginAnalystData>> getMarginAnalystData(@RequestBody MarginAnalystData marginAnalystData) throws ParseException {

        return marginAnalystService.getMarginAnalystData(marginAnalystData.getModelCode(), marginAnalystData.getCurrency().getCurrency(), marginAnalystData.getMonthYear());
    }

    @PostMapping(path = "/marginAnalystSummary", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, MarginAnalystSummary> getMarginAnalystSummary(@RequestBody MarginAnalystData marginAnalystData) {
        return marginAnalystService.getMarginAnalystSummary(marginAnalystData.getModelCode(), marginAnalystData.getCurrency().getCurrency(), marginAnalystData.getMonthYear());
    }
}

