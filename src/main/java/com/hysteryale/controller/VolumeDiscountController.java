/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.controller;

import com.hysteryale.model.volumeDiscountAnalysis.VolumeDiscountRequest;
import com.hysteryale.service.VolumeDiscountService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/volume-discount-analysis")
public class VolumeDiscountController {
    @Resource
    VolumeDiscountService volumeDiscountService;

    @PostMapping(path = "/calculate-volume-discount")
    public Map<String, Object> calculateVolumeDiscount(@RequestBody VolumeDiscountRequest request) {
        return Map.of(
                "volumeDiscountAnalysis", volumeDiscountService.calculateVolumeDiscount(request)
        );
    }
}
