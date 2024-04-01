package com.hysteryale.service;

import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.model.filters.PriceVolSensitivityFilterModel;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Map;
import java.util.Objects;

@Service
public interface PriceVolumeSensitivityService {
    Map<String, Object> getDataByFilter(PriceVolSensitivityFilterModel filters) throws ParseException;
}
