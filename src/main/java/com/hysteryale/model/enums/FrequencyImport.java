package com.hysteryale.model.enums;

import lombok.Getter;
import lombok.Setter;

@Getter
public enum FrequencyImport {

    MONTHLY("monthly"), ANNUAL("annual"), AD_HOC_IMPORT("ad hoc import")  ;

    private final String value;

    FrequencyImport(String type) {
        this.value = type;
    }
}
