/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilsTest {

    @Test
    void checkPasswordStreng() {
        assertFalse(StringUtils.checkPasswordStreng("a1a"));
        assertTrue(StringUtils.checkPasswordStreng("a1A@$aaaa5689"));
    }
}