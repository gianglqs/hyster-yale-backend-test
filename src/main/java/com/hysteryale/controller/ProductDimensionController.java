package com.hysteryale.controller;

import com.hysteryale.model.ProductDimension;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.service.ProductDimensionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController("/product")
public class ProductDimensionController {

    @Resource
    private ProductDimensionService productDimensionService;

    @PostMapping("/getData")
    public Map<String, Object> getDataByFilter(FilterModel filters, @RequestParam(defaultValue = "1") int pageNo,
                               @RequestParam(defaultValue = "100") int perPage) throws java.text.ParseException {
        filters.setPageNo(pageNo);
        filters.setPerPage(perPage);
        return productDimensionService.getDataByFilter(filters);

    }
}
