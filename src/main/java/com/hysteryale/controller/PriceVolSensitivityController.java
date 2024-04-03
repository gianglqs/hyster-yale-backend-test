package com.hysteryale.controller;


import com.hysteryale.model.filters.PriceVolSensitivityFilterModel;
import com.hysteryale.service.PriceVolumeSensitivityService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.Map;

@RestController
@RequestMapping("priceVolSensitivity")
public class PriceVolSensitivityController {

    @Resource
    private PriceVolumeSensitivityService priceVolumeSensitivityService;


        @PostMapping("/getDataForTable")
    public Map<String, Object> getDataForTable(@RequestBody PriceVolSensitivityFilterModel filters,
                                                @RequestParam(defaultValue = "1") int pageNo,
                                                @RequestParam(defaultValue = "100") int perPage) throws ParseException {
        filters.getDataFilter().setPageNo(pageNo);
        filters.getDataFilter().setPerPage(perPage);

        return priceVolumeSensitivityService.getDataByFilter(filters);
    }
}
