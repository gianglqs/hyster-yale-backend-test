package com.hysteryale.controller;

import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.response.ResponseObject;
import com.hysteryale.service.PartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.Map;

@RestController
@RequestMapping("part")
public class PartController {

    @Resource
    private PartService partService;

    @PostMapping("/getPartForTableProductDetail")
    public Map<String, Object> getPartForTableProductDetail(
            @RequestBody FilterModel filters,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "100") int perPage) throws ParseException {
        filters.setPageNo(pageNo);
        filters.setPerPage(perPage);
        return partService.getPartByFilter(filters);
    }
}
