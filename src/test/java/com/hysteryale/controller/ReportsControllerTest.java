/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.controller;

import com.hysteryale.model.reports.CompareCurrencyRequest;
import com.hysteryale.utils.JsonUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class ReportsControllerTest {
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
    public void testCompareCurrency() throws Exception {
        CompareCurrencyRequest request = new CompareCurrencyRequest("USD", List.of("EUR", "JPY"), false, "", "");

        MvcResult result =
                mockMvc
                        .perform(
                                post("/reports/compareCurrency")
                                        .content(JsonUtils.toJSONString(request))
                                        .contentType(MediaType.APPLICATION_JSON)
                        )
                        .andExpect(jsonPath("$.compareCurrency").isMap())
                        .andExpect(jsonPath("$.compareCurrency.stable").isArray())
                        .andExpect(jsonPath("$.compareCurrency.weakening").isArray())
                        .andExpect(jsonPath("$.compareCurrency.strengthening").isArray())
                        .andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(username = "admin@gmail.com")
    public void testUploadExchangeRateFile() throws Exception {
        Resource fileResource = new ClassPathResource("import_files/currency_exchangerate/EXCSEP2023.xlsx");
        Assertions.assertNotNull(fileResource);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                fileResource.getFilename(),
                MediaType.MULTIPART_FORM_DATA_VALUE,
                fileResource.getInputStream()
        );
        MvcResult result =
                mockMvc
                        .perform(
                                MockMvcRequestBuilders
                                        .multipart("/reports/uploadExchangeRate")
                                        .file(file)
                        )
                        .andExpect(jsonPath("$.message").isString())
                        .andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
    }
}
