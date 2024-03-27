package com.hysteryale.controller;

import com.hysteryale.model.payLoad.DealerPayload;
import com.hysteryale.service.DealerService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping(path = "/dealers")
public class DealerController {
    @Resource
    DealerService dealerService;

    @PostMapping()
    public  Map<String, Object> getDealerListing(@RequestBody DealerPayload dealerPayload, @RequestParam(defaultValue = "1") int pageNo) {
        return Map.of(
                "dealerListing", dealerService.getDealerListing(dealerPayload, pageNo)
        );
    }
}
