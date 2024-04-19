package com.hysteryale.model.filters;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class InterestRateFilterModel {
    private String bankName;
    private int perPage;
    private int pageNo;

}
