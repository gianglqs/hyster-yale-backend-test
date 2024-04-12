/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PagingnatorUtilsTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void calculateNumberOfPages() {
        assertEquals(10,PagingnatorUtils.calculateNumberOfPages(10, 99));
        assertEquals(10,PagingnatorUtils.calculateNumberOfPages(10, 100));
        assertEquals(11,PagingnatorUtils.calculateNumberOfPages(10, 101));
    }
}