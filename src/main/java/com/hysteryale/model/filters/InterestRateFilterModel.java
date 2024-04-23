package com.hysteryale.model.filters;

import lombok.*;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class InterestRateFilterModel {
    private String bankName;
    private List<String> regions;
    private int perPage;
    private int pageNo;

}
