/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.controller;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class GDPControllerTest {
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

    /**
     * {@link GDPController#getDataForTable(int, int, int)}
     */
    @Test
    @WithMockUser(authorities = "USER")
    public void testGetDataForTable_2020_perPage100() throws Exception {
        int year = 2020;
        int payNo = 1;
        int perPage = 100;
        MvcResult result =
                mockMvc
                        .perform(
                                get("/gdp/getDataForTable?year=" + year + "&pageNo=" + payNo + "&perPage=" + perPage)
                                        .contentType(MediaType.APPLICATION_JSON)
                        )
                        .andExpect(jsonPath("$.totalItems").isNumber())
                        .andExpect(jsonPath("$.dataTable").isArray())
                        .andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
    }


    /**
     * {@link GDPController#getDataForTable(int, int, int)}
     */
    @Test
    @WithMockUser(authorities = "USER")
    public void testGetDataForTable_2030_perPage10() throws Exception {
        int year = 2030;
        int payNo = 1;
        int perPage = 10;
        MvcResult result =
                mockMvc
                        .perform(
                                get("/gdp/getDataForTable?year=" + year + "&pageNo=" + payNo + "&perPage=" + perPage)
                                        .contentType(MediaType.APPLICATION_JSON)
                        )
                        .andExpect(jsonPath("$.totalItems").isNumber())
                        .andExpect(jsonPath("$.dataTable").isArray())
                        .andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
    }

    /**
     * {@link GDPController#getDataForBubbleChart(int)}
     */
    @Test
    @WithMockUser(authorities = "USER")
    public void testGetDataForBubbleChart_2020() throws Exception {
        int year = 2020;
        MvcResult result =
                mockMvc
                        .perform(
                                get("/gdp/getDataForBubbleChart?year=" + year)
                                        .contentType(MediaType.APPLICATION_JSON)
                        )
                        .andExpect(jsonPath("$").isArray())
                        .andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
    }

    /**
     * {@link GDPController#getDataForBubbleChart(int)}
     */
    @Test
    @WithMockUser(authorities = "USER")
    public void testGetDataForBubbleChart_2030() throws Exception {
        int year = 2030;
        MvcResult result =
                mockMvc
                        .perform(
                                get("/gdp/getDataForBubbleChart?year=" + year)
                                        .contentType(MediaType.APPLICATION_JSON)
                        )
                        .andExpect(jsonPath("$").isArray())
                        .andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
    }

    /**
     * {@link GDPController#getDataForBubbleChart(int)}
     */
    @Test
    @WithMockUser(authorities = "USER")
    public void testGetDataForBubbleChart_2022() throws Exception {
        int year = 2022;
        MvcResult result =
                mockMvc
                        .perform(
                                get("/gdp/getDataForBubbleChart?year=" + year)
                                        .contentType(MediaType.APPLICATION_JSON)
                        )
                        .andExpect(jsonPath("$").isArray())
                        .andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
    }

    /**
     * {@link GDPController#getDataForTopCountry(int)}
     */
    @Test
    @WithMockUser(authorities = "USER")
    public void testGetDataForTopCountry_2022() throws Exception {
        int year = 2022;
        MvcResult result =
                mockMvc
                        .perform(
                                get("/gdp/getDataForTopCountry?year=" + year)
                                        .contentType(MediaType.APPLICATION_JSON)
                        )
                        .andExpect(jsonPath("$.dataTopCountry").isArray())
                        .andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
    }

    /**
     * {@link GDPController#getDataForTopCountry(int)}
     */
    @Test
    @WithMockUser(authorities = "USER")
    public void testGetDataForTopCountry_2002() throws Exception {
        int year = 2002;
        MvcResult result =
                mockMvc
                        .perform(
                                get("/gdp/getDataForTopCountry?year=" + year)
                                        .contentType(MediaType.APPLICATION_JSON)
                        )
                        .andExpect(jsonPath("$.dataTopCountry").isArray())
                        .andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
    }


    /**
     * {@link GDPController#getDataForTopCountry(int)}
     */
    @Test
    @WithMockUser(authorities = "USER")
    public void testGetDataForTopCountry_2030() throws Exception {
        int year = 2030;
        MvcResult result =
                mockMvc
                        .perform(
                                get("/gdp/getDataForTopCountry?year=" + year)
                                        .contentType(MediaType.APPLICATION_JSON)
                        )
                        .andExpect(jsonPath("$.dataTopCountry").isArray())
                        .andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
    }

    /**
     * {@link GDPController#getDataForTopCountry(int)}
     */
    @Test
    @WithMockUser(authorities = "USER")
    public void testGetDataForTopCountry_2025() throws Exception {
        int year = 2025;
        MvcResult result =
                mockMvc
                        .perform(
                                get("/gdp/getDataForTopCountry?year=" + year)
                                        .contentType(MediaType.APPLICATION_JSON)
                        )
                        .andExpect(jsonPath("$.dataTopCountry").isArray())
                        .andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
    }

}
