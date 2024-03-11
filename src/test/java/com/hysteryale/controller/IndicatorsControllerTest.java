package com.hysteryale.controller;

import com.hysteryale.model.competitor.CompetitorColor;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.model.filters.SwotFilters;
import com.hysteryale.repository.CompetitorColorRepository;
import com.hysteryale.repository.UserRepository;
import com.hysteryale.service.ImportService;
import com.hysteryale.service.IndicatorService;
import com.hysteryale.service.UserService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
public class IndicatorsControllerTest {
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private MockMvc mockMvc;
    @Resource
    private CompetitorColorRepository competitorColorRepository;
    @Resource
    private IndicatorService indicatorService;
    @MockBean
    private ImportService importService;
    @MockBean
    private UserService userService;
    @Resource
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        File uploadedFiles = new File("/tmp/UploadFiles/forecast_pricing");
        if(uploadedFiles.mkdirs())
            log.info("Create UploadFiles in /tmp");
        else
            log.info("Error on create folders");
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void testGetCompetitorData() throws Exception {
        FilterModel filters = new FilterModel();

        MvcResult result =
                mockMvc
                        .perform(post("/getCompetitorData")
                                .content(JsonUtils.toJSONString(filters))
                                .contentType(MediaType.APPLICATION_JSON)
                        )
                        .andExpect(jsonPath("$.total").isArray())
                        .andExpect(jsonPath("$.listCompetitor").isArray())
                        .andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void testGetDataForLineChartRegion() throws Exception {
        FilterModel filters = new FilterModel();

        MvcResult result =
                mockMvc
                        .perform(post("/chart/getDataForRegionLineChart")
                                .content(JsonUtils.toJSONString(filters))
                                .contentType(MediaType.APPLICATION_JSON)
                        )
                        .andExpect(jsonPath("$.lineChartRegion").isArray())
                        .andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void testGetDataForLineChartPlant() throws Exception {
        FilterModel filters = new FilterModel();

        MvcResult result =
                mockMvc
                        .perform(post("/chart/getDataForPlantLineChart")
                                .content(JsonUtils.toJSONString(filters))
                                .contentType(MediaType.APPLICATION_JSON)
                        )
                        .andExpect(jsonPath("$.lineChartPlant").isArray())
                        .andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void testGetDataBubbleChart() throws Exception {
        SwotFilters filters = new SwotFilters(
                "Asia",
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
                );

        MvcResult result =
                mockMvc
                        .perform(post("/chart/getDataForCompetitorBubbleChart")
                            .content(JsonUtils.toJSONString(filters))
                            .contentType(MediaType.APPLICATION_JSON)
                        )
                        .andExpect(jsonPath("$.competitiveLandscape").isArray())
                        .andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void testGetCompetitorColor() throws Exception {
        String strSearch = "a";
        int perPage = 100;
        int pageNo = 1;

        MvcResult result =
                mockMvc
                        .perform(get("/competitorColors")
                                .param("search", strSearch)
                                .param("perPage", Integer.toString(perPage))
                                .param("pageNo", Integer.toString(pageNo))
                        )
                        .andExpect(jsonPath("$.competitorColors").isArray())
                        .andExpect(jsonPath("$.perPage").value(perPage))
                        .andExpect(jsonPath("$.page").value(pageNo))
                        .andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void testGetCompetitorColor_emptySearchString() throws Exception {
        String strSearch = "";
        int perPage = 100;
        int pageNo = 1;

        MvcResult result =
                mockMvc
                        .perform(get("/competitorColors")
                                .param("search", strSearch)
                                .param("perPage", Integer.toString(perPage))
                                .param("pageNo", Integer.toString(pageNo))
                        )
                        .andExpect(jsonPath("$.competitorColors").isArray())
                        .andExpect(jsonPath("$.perPage").value(perPage))
                        .andExpect(jsonPath("$.page").value(pageNo))
                        .andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void testGetCompetitorColor_notFoundSearchString() throws Exception {
        String strSearch = "qweqweqweqweqweqwe";
        int perPage = 100;
        int pageNo = 1;

        MvcResult result =
                mockMvc
                        .perform(get("/competitorColors")
                                .param("search", strSearch)
                                .param("perPage", Integer.toString(perPage))
                                .param("pageNo", Integer.toString(pageNo))
                        )
                        .andExpect(jsonPath("$.competitorColors").isArray())
                        .andExpect(jsonPath("$.perPage").value(perPage))
                        .andExpect(jsonPath("$.page").value(pageNo))
                        .andExpect(jsonPath("$.totalItems").value(0))
                        .andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void testGetCompetitorDetails() throws Exception {
        CompetitorColor competitorColor = competitorColorRepository.save(new CompetitorColor("competitorColorTest", "#121212"));

        MvcResult result =
                mockMvc
                        .perform(get("/competitorColors/getDetails")
                                .param("id", Integer.toString(competitorColor.getId()))
                        )
                        .andExpect(jsonPath("$.competitorColorDetail.id").value(competitorColor.getId()))
                        .andExpect(jsonPath("$.competitorColorDetail.groupName").value(competitorColor.getGroupName()))
                        .andExpect(jsonPath("$.competitorColorDetail.colorCode").value(competitorColor.getColorCode()))
                        .andReturn();

        Assertions.assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void testGetCompetitorDetails_notFound() throws Exception {
        MvcResult result =
                mockMvc
                        .perform(get("/competitorColors/getDetails")
                                .param("id", Integer.toString(12837129))
                        )
                        .andReturn();
        Assertions.assertEquals(404, result.getResponse().getStatus());
        Assertions.assertTrue(Objects.requireNonNull(result.getResolvedException()).getMessage().contains("Competitor Color not found"));
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void testUpdateCompetitorColor() throws Exception {
        CompetitorColor competitorColor = competitorColorRepository.save(new CompetitorColor("competitorColorTest", "#121212"));

        String newGroupName = "Competitor Color Test 2";
        String newColorCode = "#323232";
        competitorColor.setGroupName(newGroupName);
        competitorColor.setColorCode(newColorCode);
        MvcResult result =
                mockMvc
                        .perform(put("/competitorColors")
                                .content(JsonUtils.toJSONString(competitorColor))
                                .contentType(MediaType.APPLICATION_JSON)
                        )
                        .andReturn();
        CompetitorColor dbCompetitorColor = indicatorService.getCompetitorById(competitorColor.getId());

        Assertions.assertEquals(newGroupName, dbCompetitorColor.getGroupName());
        Assertions.assertEquals(newColorCode, dbCompetitorColor.getColorCode());
        Assertions.assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void testUpdateCompetitorColor_notFound() throws Exception {
        CompetitorColor competitorColor = competitorColorRepository.save(new CompetitorColor("competitorColorTest", "#121212"));

        String newGroupName = "Competitor Color Test 2";
        String newColorCode = "#323232";
        competitorColor.setId(123123213);
        competitorColor.setGroupName(newGroupName);
        competitorColor.setColorCode(newColorCode);

        MvcResult result =
                mockMvc
                        .perform(put("/competitorColors")
                                .content(JsonUtils.toJSONString(competitorColor))
                                .contentType(MediaType.APPLICATION_JSON)
                        )
                        .andReturn();
        Assertions.assertEquals(404, result.getResponse().getStatus());
        Assertions.assertTrue(Objects.requireNonNull(result.getResolvedException()).getMessage().contains("Competitor Color not found"));
    }

    @Test
    @WithMockUser(username = "user1@gmail.com", authorities = "ADMIN")
    public void testImportIndicatorsFile_notExcelFile() throws Exception {
        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "mockfile.txt",
                        MediaType.MULTIPART_FORM_DATA_VALUE,
                        "123".getBytes()
                );
        MvcResult result =
                mockMvc
                        .perform(
                                MockMvcRequestBuilders
                                        .multipart("/importIndicatorsFile")
                                        .file(file)
                        ).andReturn();
        Assertions.assertEquals(400, result.getResponse().getStatus());
        Assertions.assertTrue(Objects.requireNonNull(result.getResolvedException()).getMessage().contains("Uploaded file is not an Excel file"));
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", authorities = "USER")
    public void testImportIndicatorsFile_missingForecastFile() throws Exception {
        org.springframework.core.io.Resource fileResource = new ClassPathResource("/import_files/competitor_pricing/Competitor Pricing Database.xlsx");
        Assertions.assertNotNull(fileResource);

        when(importService.loadForecastForCompetitorPricingFromFile())
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Missing Forecast Dynamic Pricing Excel file"));
        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        fileResource.getFilename(),
                        MediaType.MULTIPART_FORM_DATA_VALUE,
                        fileResource.getInputStream()
                );

        MvcResult result =
                mockMvc
                        .perform(
                                MockMvcRequestBuilders
                                        .multipart("/importIndicatorsFile")
                                        .file(file)
                        ).andReturn();
        Assertions.assertEquals(404, result.getResponse().getStatus());
        Assertions.assertTrue(Objects.requireNonNull(result.getResolvedException()).getMessage().contains("Missing Forecast Dynamic Pricing Excel file"));
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void testImportIndicatorsFile() throws Exception {
        org.springframework.core.io.Resource fileResource = new ClassPathResource("/import_files/competitor_pricing/Competitor Pricing Database.xlsx");
        Assertions.assertNotNull(fileResource);

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        fileResource.getFilename(),
                        MediaType.MULTIPART_FORM_DATA_VALUE,
                        fileResource.getInputStream()
                        );

        MvcResult result =
                mockMvc
                        .perform(
                                MockMvcRequestBuilders
                                        .multipart("/importIndicatorsFile")
                                        .file(file)
                        ).andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
    }
}
