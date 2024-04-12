package com.hysteryale.controller;

import com.hysteryale.service.ImportTrackingService;
import com.hysteryale.utils.ConvertDataFilterUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("importTracking")
public class ImportTrackingController {

    @Resource
    ImportTrackingService importTrackingService;

    @GetMapping("/getDataImportTracking")
    public Map<String, Object> getDataImportTrackingForTable(String date) throws ParseException {
        LocalDate localDate = ConvertDataFilterUtil.checkDateData(date);

        return importTrackingService.getDataByFilter(localDate);
    }
}
