package com.hysteryale.controller;

import com.hysteryale.model.dealer.Dealer;
import com.hysteryale.model.payLoad.DealerPayload;
import com.hysteryale.response.ResponseObject;
import com.hysteryale.service.DealerService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping(path = "/dealers")
public class DealerController {
    @Resource
    DealerService dealerService;

    @PostMapping()
    public Map<String, Object> getDealerListing(@RequestBody DealerPayload dealerPayload, @RequestParam(defaultValue = "1") int pageNo) {
        Page<Dealer> dealerPage = dealerService.getDealerListing(dealerPayload, pageNo);
        return Map.of(
                "dealerListing", dealerPage.getContent(),
                "pageNo", dealerPage.getNumber() + 1,
                "totalItems", dealerPage.getTotalElements()
        );
    }

    @GetMapping()
    public ResponseEntity<ResponseObject> getDealerDetails(@RequestParam int dealerId) {
        Dealer dealer = dealerService.getDealerById(dealerId);
        if(dealer != null)
            return ResponseEntity.status(200).body(new ResponseObject("", dealer));
        else
            return ResponseEntity.status(404).body(new ResponseObject("Dealer not found", null));
    }
}
