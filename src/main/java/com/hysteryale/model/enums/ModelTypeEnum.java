/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.model.enums;

import lombok.Getter;

import java.util.List;

@Getter
public enum ModelTypeEnum {

    BOOKING("Booking"),
    DEALER("Dealer"),
    COST_DATA("Cost Data"),
    BOOKING_FPA("Booking FPA"),
    SHIPMENT("Shipment"),
    PRODUCT_APAC("Product APAC"),
    PRODUCT_DIMENSION("Product Dimension"),
    PRODUCT_IMAGE("Product Image"),
    EXCHANGE_RATE("Exchange Rate"),
    COMPETITOR("Competitor"),
    PART("Part"),
    MACRO("Macro"),
    NOVO("NOVO"),
    FORECAST_PRICING("Forecast Pricing"),
    RESIDUAL_VALUE("Residual Value"),
    AOP_MARGIN("AOP Margin"),
    DEALER_PRODUCT("Dealer Product");


    private final String value;

    ModelTypeEnum(String type) {
        this.value = type;
    }


    public static final List<String> listModelTypeNotInImportTracking = List.of(PRODUCT_IMAGE.value, NOVO.value);
}
