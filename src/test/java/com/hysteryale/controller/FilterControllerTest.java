package com.hysteryale.controller;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class FilterControllerTest {
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
    public void testGetCompetitorPricingFilters() throws Exception {
        MvcResult result =
                mockMvc
                        .perform(get("/filters/competitorPricing"))
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.classes").isArray())
                        .andExpect(jsonPath("$.plants").isArray())
                        .andExpect(jsonPath("$.metaSeries").isArray())
                        .andExpect(jsonPath("$.models").isArray())
                        .andExpect(jsonPath("$.chineseBrands").isArray())
                        .andExpect(jsonPath("$.marginPercentageGrouping").isArray())
                        .andExpect(jsonPath("$.regions").isArray())
                        .andExpect(jsonPath("$.dealers").isArray())
                        .andExpect(jsonPath("$.series").isArray())
                        .andExpect(jsonPath("$.categories").isArray())
                        .andExpect(jsonPath("$.countries").isArray())
                        .andReturn();

        Assertions.assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void testGetShipmentFilters() throws Exception {
        MvcResult result =
                mockMvc
                        .perform(get("/filters/shipment"))
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.regions").isArray())
                        .andExpect(jsonPath("$.classes").isArray())
                        .andExpect(jsonPath("$.plants").isArray())
                        .andExpect(jsonPath("$.metaSeries").isArray())
                        .andExpect(jsonPath("$.models").isArray())
                        .andExpect(jsonPath("$.marginPercentageGroup").isArray())
                        .andExpect(jsonPath("$.AOPMarginPercentageGroup").isArray())
                        .andExpect(jsonPath("$.dealers").isArray())
                        .andExpect(jsonPath("$.segments").isArray())
                        .andReturn();

        Assertions.assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void testGetBookingFilters() throws Exception {
        MvcResult result =
                mockMvc
                        .perform(get("/filters/booking"))
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.regions").isArray())
                        .andExpect(jsonPath("$.classes").isArray())
                        .andExpect(jsonPath("$.plants").isArray())
                        .andExpect(jsonPath("$.metaSeries").isArray())
                        .andExpect(jsonPath("$.models").isArray())
                        .andExpect(jsonPath("$.marginPercentageGroup").isArray())
                        .andExpect(jsonPath("$.AOPMarginPercentageGroup").isArray())
                        .andExpect(jsonPath("$.dealers").isArray())
                        .andExpect(jsonPath("$.segments").isArray())
                        .andReturn();

        Assertions.assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void testGetOutlierFilters() throws Exception {
        MvcResult result =
                mockMvc
                        .perform(get("/filters/outlier"))
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.regions").isArray())
                        .andExpect(jsonPath("$.classes").isArray())
                        .andExpect(jsonPath("$.plants").isArray())
                        .andExpect(jsonPath("$.metaSeries").isArray())
                        .andExpect(jsonPath("$.models").isArray())
                        .andExpect(jsonPath("$.marginPercentageGroup").isArray())
                        .andExpect(jsonPath("$.dealers").isArray())
                        .andExpect(jsonPath("$.series").isArray())
                        .andReturn();

        Assertions.assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void testGetTrendsFilters() throws Exception {
        MvcResult result =
                mockMvc
                        .perform(get("/filters/trends"))
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.regions").isArray())
                        .andExpect(jsonPath("$.plants").isArray())
                        .andExpect(jsonPath("$.metaSeries").isArray())
                        .andExpect(jsonPath("$.classes").isArray())
                        .andExpect(jsonPath("$.models").isArray())
                        .andExpect(jsonPath("$.segments").isArray())
                        .andExpect(jsonPath("$.years").isArray())
                        .andExpect(jsonPath("$.dealers").isArray())
                        .andReturn();

        Assertions.assertEquals(200, result.getResponse().getStatus());
    }
}
