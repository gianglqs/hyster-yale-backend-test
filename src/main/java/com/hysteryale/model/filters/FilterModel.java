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
    private List<String> segment;
    private String chineseBrand;
    private String aopMarginPercentageGroup;
    private String marginPercentage;
    private String fromDate;
    private String toDate;
    private Integer year;
    private int perPage;
    private int pageNo;

}
