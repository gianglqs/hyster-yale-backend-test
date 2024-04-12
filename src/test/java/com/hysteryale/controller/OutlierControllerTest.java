/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.controller;

import com.hysteryale.model.filters.FilterModel;
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
public class OutlierControllerTest {
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
    public void testGetDataForTable() throws Exception {
        FilterModel filters = new FilterModel();

        MvcResult result =
                mockMvc
                        .perform(
                                post("/table/getOutlierTable")
                                        .content(JsonUtils.toJSONString(filters))
                                        .contentType(MediaType.APPLICATION_JSON)
                        )
                        .andExpect(jsonPath("$.total").isArray())
                        .andExpect(jsonPath("$.totalItems").isNumber())
                        .andExpect(jsonPath("$.listOutlier").isArray())
                        .andReturn();

        Assertions.assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void testGetOutliersForChart() throws Exception {
        FilterModel filters = new FilterModel();

        MvcResult result =
                mockMvc
                        .perform(
                                post("/chart/getOutliers")
                                        .content(JsonUtils.toJSONString(filters))
                                        .contentType(MediaType.APPLICATION_JSON)
                        )
                        .andExpect(jsonPath("$.chartOutliersData").isArray())
                        .andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
    }
}
