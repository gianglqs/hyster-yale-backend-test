package com.hysteryale.model.payLoad;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DealerPayload {
    private String dealerName;
    private List<String> regions;
    private List<String> countries;
}
