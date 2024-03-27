package com.hysteryale.controller;

import com.hysteryale.model.filters.ImportFailureFilter;
import com.hysteryale.service.ImportFailureService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("importFailure")
public class ImportFailureController {


    @Resource
    ImportFailureService importFailureService;

    @PostMapping("/getImportFailureForTable")
    public Map<String, Object> getDataForTable(@RequestBody ImportFailureFilter filter,
                                               @RequestParam(defaultValue = "1") int pageNo,
                                               @RequestParam(defaultValue = "100") int perPage, @RequestHeader("locale") String locale) {
        return importFailureService.getDataForTable(filter, pageNo, perPage, locale);
    }
}
