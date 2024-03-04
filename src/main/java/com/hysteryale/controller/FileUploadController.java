package com.hysteryale.controller;

import com.hysteryale.model.filters.AdminFilter;
import com.hysteryale.response.ResponseObject;
import com.hysteryale.service.FileUploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/fileUpload")
public class FileUploadController {

    @Resource
    private FileUploadService fileUploadService;

    @PostMapping("/getDataForTable")
    public Map<String, Object> getDataForTable(@RequestBody AdminFilter filter,
                                               @RequestParam(defaultValue = "1") int pageNo,
                                               @RequestParam(defaultValue = "100") int perPage) {

        return fileUploadService.getDataForTable(filter, pageNo, perPage);

    }
}
