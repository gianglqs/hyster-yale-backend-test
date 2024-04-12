/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

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

    @Test
    @WithMockUser(authorities = "USER")
    public void testGetDealerProducts() throws Exception {
        FilterModel filters = new FilterModel();
        MvcResult result =
                mockMvc
                        .perform(
                                post("/dealers/get-products")
                                        .content(JsonUtils.toJSONString(filters))
                                        .contentType(MediaType.APPLICATION_JSON)
                        )
                        .andExpect(jsonPath("$.pageNo").isNumber())
                        .andExpect(jsonPath("$.totalItems").isNumber())
                        .andExpect(jsonPath("$.listData").isArray())
                        .andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(username = "admin@gmail.com")
    public void testImportDealerProducts() throws Exception {
        Resource fileResource = new ClassPathResource("import_files/dealer/10 years data of Top 20 API Dealer v5.xlsx");
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
                                        .multipart("/dealers/import-dealer-product")
                                        .file(file)
                        )
                        .andExpect(jsonPath("$.message").isString())
                        .andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(username = "admin@gmail.com")
    public void testReadNOVOFile_notExcelFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "mockfile_novo.txt",
                MediaType.MULTIPART_FORM_DATA_VALUE,
                "123".getBytes()
        );

        MvcResult result =
                mockMvc
                        .perform(
                                MockMvcRequestBuilders
                                        .multipart("/dealers/import-dealer-product")
                                        .file(file)
                        )
                        .andReturn();
        Assertions.assertEquals(400, result.getResponse().getStatus());
        Assertions.assertTrue(result.getResponse().getContentAsString().contains("File is not Excel"));
    }
}
