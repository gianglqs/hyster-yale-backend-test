/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.model.filters;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FilterModel {

    private String orderNo;
    private List<String> regions;
    private List<String> dealers;
    private List<String> plants;
    private List<String> metaSeries;
    private List<String> classes;
    private List<String> models;
    private List<String> segments;
    private String chineseBrand;
    private String aopMarginPercentageGroup;
    private String marginPercentage;
    private String fromDate;
    private String toDate;
    private Integer year;
    private int perPage;
    private int pageNo;
    private String marginPercentageAfterAdj;
    //for ProductDimension
    private String modelCode;
    private List<String> brands;
    private List<String> family;
    private List<String> truckType;
    //for productDimension detail
    private List<String> orderNumbers;
    private String metaSeriez;
    private Integer dealerId;

}
