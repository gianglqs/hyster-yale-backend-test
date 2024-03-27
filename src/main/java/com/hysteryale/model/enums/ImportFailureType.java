package com.hysteryale.model.enums;

import lombok.Getter;

@Getter
public enum ImportFailureType {
    ERROR ("error"), WARNING("warning");

    private final String value;
    ImportFailureType(String type) {
        this.value = type;
    }
}
