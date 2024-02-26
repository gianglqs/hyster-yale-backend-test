package com.hysteryale.utils;

import com.hysteryale.exception.MissingColumnException;

import java.util.List;

public class CheckRequiredColumnUtils {

    public static final List<String> SHIPMENT_REQUIRED_COLUMN = List.of("Order number", "Series", "Model",
            "Serial Number", "Quantity", "Revenue", "Revenue - Other", "Discounts", "Additional Discounts",
            "Cash Discounts", "Cost of Sales", "Dealer Commisions", "Warranty", "COS - Other", "Ship-to Country Code", "Created On");

    public static void checkRequiredColumn(List<String> currentColumns, List<String> requiredColumns) throws MissingColumnException {
        for (String requiredColumn : requiredColumns) {
            if (!currentColumns.contains(requiredColumn)) {
                throw new MissingColumnException("Missing column '" + requiredColumn + "'");
            }
        }
    }
}
