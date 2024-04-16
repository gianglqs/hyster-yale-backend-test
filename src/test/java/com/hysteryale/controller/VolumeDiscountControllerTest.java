/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.controller;

import com.hysteryale.model.volumeDiscountAnalysis.VolumeDiscountRequest;
import com.hysteryale.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
public class VolumeDiscountControllerTest {
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void testCalculateVolumeDiscount() throws Exception {
        VolumeDiscountRequest request = new VolumeDiscountRequest();

        MvcResult result =
                mockMvc.perform(post("/volume-discount-analysis/calculate-volume-discount")
                        .content(JsonUtils.toJSONString(request)).contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.volumeDiscountAnalysis").isArray())
                        .andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
    }
}
