package com.hysteryale.service;

import com.hysteryale.model.Dealer;
import com.hysteryale.model.payLoad.DealerPayload;
import com.hysteryale.repository.DealerRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class DealerService {
    @Resource
    DealerRepository dealerRepository;

    public Dealer getDealerByName(List<Dealer> dealers, String name) {
        for (Dealer dealer : dealers) {
            if (keepLettersAndDigits(dealer.getName()).contains(keepLettersAndDigits(name)))
                return dealer;
        }
        return null;
    }

    private String keepLettersAndDigits(String input) {
        return input.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }

    public List<Dealer> getDealerListing(DealerPayload payload, int pageNo) {
        Pageable pageable = PageRequest.of(pageNo - 1, 15);
        return dealerRepository.getDealerListingByFilter(payload.getDealerName(), pageable);
    }

}
