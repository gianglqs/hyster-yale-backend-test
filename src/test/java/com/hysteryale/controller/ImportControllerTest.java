/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.controller;

import com.hysteryale.service.PartService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.annotation.Resource;

import java.io.FileNotFoundException;

@RunWith(MockitoJUnitRunner.class)
class ImportControllerTest extends BasedControllerTest {
    @InjectMocks
    ImportController importController;
    @Mock
    PartService partService;

    @Test
    void importData() {
        try {
       //     importController.importData();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}