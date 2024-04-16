/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.model.enums;


import lombok.Getter;

@Getter
public enum ImportTrackingStatus {

    COMPLETED("Completed"), UNFINISHED("Unfinished");

    private final String value;

    ImportTrackingStatus(String type) {
        this.value = type;
    }

}
