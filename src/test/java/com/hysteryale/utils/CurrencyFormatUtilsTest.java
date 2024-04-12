/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CurrencyFormatUtilsTest {

    @Test
    public void formatDoubleValue() {
        assertEquals(1.4545 , CurrencyFormatUtils.formatDoubleValue(1.45454545 , CurrencyFormatUtils.decimalFormatFourDigits));
        assertEquals(1.45 , CurrencyFormatUtils.formatDoubleValue(1.45454545 , CurrencyFormatUtils.decimalFormatTwoDigits));
    }
}