package com.hysteryale.controller;
import com.hysteryale.model.InterestRate;
import com.hysteryale.model.User;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.model.filters.InterestRateFilterModel;
import com.hysteryale.service.InterestRateService;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
public class InterestRateController {
    @Resource
    InterestRateService interestRateService;

    @PostMapping("/getAllInterestRate")
    public Map<String, Object> getAllInterestRate(@RequestBody InterestRateFilterModel filters,
                                                        @RequestParam(defaultValue = "1") int pageNo,
                                                        @RequestParam(defaultValue = "100") int perPage) throws Exception {
        try {
            //import data from world bank excel file
            interestRateService.importInterestRateFromFile("import_files/interest_rate/API_FR.INR.RINR_DS2_en_excel_v2_119.xls");
            ResponseEntity.status(HttpStatus.OK).body("Data imported successfully");
        } catch (Exception e) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error occurred while importing data: " + e.getMessage());
        }
        filters.setPageNo(pageNo);
        filters.setPerPage(perPage);
        return  interestRateService.getListInterestRateByFilter(filters);
    }


}
