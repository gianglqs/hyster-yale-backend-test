/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.controller;

import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.model.filters.PriceVolSensitivityFilterModel;
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

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class PriceVolSensitivityControllerTest {
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
    public void testGetDataPriceVolumeSensitivity_userRole() throws Exception {
        PriceVolSensitivityFilterModel filters = new PriceVolSensitivityFilterModel();
        FilterModel filterModel = new FilterModel();
        filters.setDataFilter(filterModel);
        log.info(JsonUtils.toJSONString(filters));

        MvcResult result =
                mockMvc
                        .perform(
                                post("/priceVolSensitivity/getDataForTable")
                                        .content(JsonUtils.toJSONString(filters))
                                        .contentType(MediaType.APPLICATION_JSON)
                        )
                        .andExpect(jsonPath("$.totalItems").isNumber())
                        .andExpect(jsonPath("$.listOrder").isArray())
                        .andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void testGetDataPriceVolumeSensitivity_adminRole() throws Exception {
        PriceVolSensitivityFilterModel filters = new PriceVolSensitivityFilterModel();
        FilterModel filterModel = new FilterModel();
        filters.setDataFilter(filterModel);
        log.info(JsonUtils.toJSONString(filters));

        MvcResult result =
                mockMvc
                        .perform(
                                post("/priceVolSensitivity/getDataForTable")
                                        .content(JsonUtils.toJSONString(filters))
                                        .contentType(MediaType.APPLICATION_JSON)
                        )
                        .andExpect(jsonPath("$.totalItems").isNumber())
                        .andExpect(jsonPath("$.listOrder").isArray())
                        .andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
    }
}
