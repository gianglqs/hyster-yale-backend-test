package com.hysteryale.service;

import com.hysteryale.model.Dealer;
import com.hysteryale.model.payLoad.DealerPayload;
import com.hysteryale.repository.DealerRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import javax.annotation.Resource;

@SpringBootTest
public class DealerServiceTest {
    @Resource
    DealerService dealerService;
    @Resource
    DealerRepository dealerRepository;

    @Test
    public void testGetDealerListing() {
        DealerPayload payload = new DealerPayload();
        int pageNo = 0;
        int perPage = 15;

        // Expected
        Pageable pageable = PageRequest.of(pageNo, 15);
        Page<Dealer> expected = dealerRepository.getDealerListingByFilter(payload.getDealerName(), pageable);

        // Actual
        Page<Dealer> result = dealerService.getDealerListing(payload, pageNo + 1);

        // Assertions
        Assertions.assertEquals(pageNo, result.getNumber());
        Assertions.assertEquals(perPage, result.getSize());
        Assertions.assertEquals(expected.getContent().size(), result.getContent().size());
    }

    @Test
    public void testGetDealerById() {
        int dealerId = 1;
        Dealer result = dealerService.getDealerById(dealerId);
        Assertions.assertNotNull(result);

        int dealerId_notFound = 9999;
        Dealer result_notFound = dealerService.getDealerById(dealerId_notFound);
        Assertions.assertNull(result_notFound);
    }
}
