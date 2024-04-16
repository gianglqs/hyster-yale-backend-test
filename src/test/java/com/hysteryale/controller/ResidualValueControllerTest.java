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
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class ResidualValueControllerTest {

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
     * {@link ResidualValueController#getResidualValueDataByFilter(String)}
     */
    @Test
    @WithMockUser(authorities = "USER")
    public void testGetResidualValueDataByFilter() throws Exception {

        MvcResult result =
                mockMvc
                        .perform(get("/residualValue/getResidualValueData?modelCode=J2.5XNL"))
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.serverTimeZone").isString())
                        .andExpect(jsonPath("$.listResidualValue").isArray())
                        .andReturn();
        log.info(result.getResponse().getContentAsString());
        Assertions.assertEquals(200, result.getResponse().getStatus());
    }


    /**
     * {@link ResidualValueController#importData}
     */
    @Test
    @WithMockUser(username = "admin@gmail.com", authorities = "ADMIN")
    public void testImportData() throws Exception {
        org.springframework.core.io.Resource fileResource = new ClassPathResource("import_files/residual_value/RV_ASIA_2023_v1.1-2.xlsx");
        Assertions.assertNotNull(fileResource);

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        fileResource.getFilename() +1,
                        MediaType.MULTIPART_FORM_DATA_VALUE,
                        fileResource.getInputStream()
                );

        MvcResult result =
                mockMvc
                        .perform(
                                MockMvcRequestBuilders
                                        .multipart("/residualValue/importData")
                                        .file(file)
                                        .header("locale", "en")

                        ).andReturn();
        log.info(result.getResponse().getContentAsString());

        Assertions.assertEquals(200, result.getResponse().getStatus());
    }

}
