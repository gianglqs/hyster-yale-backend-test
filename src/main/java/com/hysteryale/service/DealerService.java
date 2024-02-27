package com.hysteryale.service;

import com.hysteryale.model.Dealer;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DealerService {

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

}
