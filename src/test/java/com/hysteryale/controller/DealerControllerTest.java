package com.hysteryale.controller;

import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.model.payLoad.DealerPayload;
import com.hysteryale.utils.JsonUtils;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class DealerControllerTest {
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
    public void testGetDealerListing() throws Exception {
        DealerPayload payload = new DealerPayload();
        MvcResult result =
                mockMvc
                        .perform(
                                post("/dealers")
                                        .content(JsonUtils.toJSONString(payload))
                                        .contentType(MediaType.APPLICATION_JSON)
                        )
                        .andExpect(jsonPath("$.dealerListing").isArray())
                        .andExpect(jsonPath("$.pageNo").isNumber())
                        .andExpect(jsonPath("$.totalItems").isNumber())
                        .andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void testGetDealerDetails() throws Exception {
        MvcResult result =
                mockMvc
                        .perform(
                                get("/dealers")
                                        .param("dealerId", String.valueOf(1))
                                        .contentType(MediaType.APPLICATION_JSON)
                        )
                        .andExpect(jsonPath("$.data.id").isNumber())
                        .andExpect(jsonPath("$.data.name").isString())
                        .andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void testGetDealerDetails_notFound() throws Exception {
        MvcResult result =
                mockMvc
                        .perform(
                                get("/dealers")
                                        .param("dealerId", String.valueOf(9999))
                                        .contentType(MediaType.APPLICATION_JSON)
                        )
                        .andExpect(jsonPath("$.message").isString())
                        .andReturn();
        Assertions.assertEquals(404, result.getResponse().getStatus());
    }
}
