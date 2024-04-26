package com.hysteryale.controller;
import com.hysteryale.model.InterestRate;
import com.hysteryale.model.User;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.model.filters.InterestRateFilterModel;
import com.hysteryale.service.InterestRateService;
import com.hysteryale.utils.EnvironmentUtils;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
public class InterestRateController {
    @Resource
    InterestRateService interestRateService;


    @GetMapping("/importDataInterestRate")
    public void importDataInterestRate() {
        String baseFolder = EnvironmentUtils.getEnvironmentValue("BASE_IMPORT_FILE");
        try {
            String filePath = baseFolder+"/interest_rate/API_FR.INR.RINR_DS2_en_excel_v2_119.xls";
            interestRateService.importInterestRateFromFile(filePath);
            ResponseEntity.status(200).body("import successfully");
        }catch (Exception e) {
            ResponseEntity.status(500).body("import failed");
        }
    }

    @PostMapping("/getAllInterestRate")
    public Map<String, Object> getAllInterestRate(@RequestBody InterestRateFilterModel filters,
                                                        @RequestParam(defaultValue = "1") int pageNo,
                                                        @RequestParam(defaultValue = "10") int perPage) throws Exception {
        filters.setPageNo(pageNo);
        filters.setPerPage(perPage);
        return  interestRateService.getListInterestRateByFilter(filters);
    }
}
