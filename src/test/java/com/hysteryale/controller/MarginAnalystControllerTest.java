/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.controller;

import com.hysteryale.model.marginAnalyst.CalculatedMargin;
import com.hysteryale.model_h2.MarginData;
import com.hysteryale.model_h2.MarginDataId;
import com.hysteryale.service.marginAnalyst.MarginDataService;
import com.hysteryale.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class MarginAnalystControllerTest {
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private MarginDataService marginDataService;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username = "admin@gmail.com")
    public void testReadNOVOFile() throws Exception {
        Resource fileResource = new ClassPathResource("import_files/novo/SN_AUD.xlsx");
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
                                        .multipart("/marginData/readNOVOFile")
                                        .file(file)
                        )
                        .andExpect(jsonPath("$.fileUUID").isString())
                        .andReturn();
        log.info(result.getResponse().getContentAsString());
        Assertions.assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", authorities = "ADMIN")
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
                                        .multipart("/marginData/readNOVOFile")
                                        .file(file)
                        )
                        .andReturn();
        Assertions.assertEquals(400, result.getResponse().getStatus());
        Assertions.assertTrue(result.getResponse().getContentAsString().contains("File is not Excel"));
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", authorities = "ADMIN")
    public void testImportMacroFile() throws Exception {
        Resource fileResource = new ClassPathResource("import_files/margin_analyst_data/USD AUD Margin Analysis Template Macro_Oct  2023 Rev.xlsb");
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
                                        .multipart("/importMacroFile")
                                        .file(file)
                        ).andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", authorities = "ADMIN")
    public void testImportMacroFile_notExcelFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "mockfile.txt",
                MediaType.MULTIPART_FORM_DATA_VALUE,
                "123".getBytes()
        );

        MvcResult result =
                mockMvc
                        .perform(
                                MockMvcRequestBuilders
                                        .multipart("/importMacroFile")
                                        .file(file)
                        )
                        .andReturn();
        Assertions.assertEquals(400, result.getResponse().getStatus());
        Assertions.assertTrue(result.getResponse().getContentAsString().contains("File is not Excel"));
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void testEstimateMarginAnalystData() throws Exception {

        MarginData marginData = new MarginData();
        MarginDataId id = new MarginDataId();
        id.setModelCode("");
        id.setCurrency("USD");
        id.setType(0);

        marginData.setId(id);
        marginData.setOrderNumber("");
        marginData.setSeries("D466");
        marginData.setFileUUID("123");
        CalculatedMargin filter = new CalculatedMargin(marginData, "Asia");

        when(marginDataService.isFileCalculated("123", "USD", "")).thenReturn(true);

        MvcResult result =
                mockMvc
                        .perform(
                                post("/estimateMarginAnalystData")
                                        .content(JsonUtils.toJSONString(filter))
                                        .contentType(MediaType.APPLICATION_JSON)
                        )
                        .andExpect(jsonPath("$.MarginAnalystData").isArray())
                        .andExpect(jsonPath("$.MarginAnalystSummary").isMap())
                        .andExpect(jsonPath("$.TargetMargin").isNumber())
                        .andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
    }
}
