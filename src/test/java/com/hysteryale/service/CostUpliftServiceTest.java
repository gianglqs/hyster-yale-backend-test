/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.service;

import com.hysteryale.repository.CostUpliftRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.annotation.Resource;
import java.io.FileNotFoundException;

public class CostUpliftServiceTest {
    @Resource
    @Mock
    CostUpliftRepository costUpliftRepository;
    @Resource
    @InjectMocks
    CostUpliftService costUpliftService;
    AutoCloseable autoCloseable;

    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    void testImportCostUplift() throws FileNotFoundException {
        //costUpliftService.importCostUplift();
    }
}
