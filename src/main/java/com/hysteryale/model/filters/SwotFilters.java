package com.hysteryale.model.filters;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SwotFilters {
    private String regions;
    private List<String> countries;
    private List<String> classes;
    private List<String> categories;
    private List<String> series;
}
