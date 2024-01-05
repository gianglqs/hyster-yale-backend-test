package com.hysteryale.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.service.BookingOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Resource;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

//@ExtendWith(MockitoExtension.class)
//@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration
public class BookingOrderControllerTest extends BasedControllerTest {


    @Resource
    private MockMvc mvc;
    @Mock
    BookingOrderService bookingOrderService;

    @InjectMocks
    BookingOrderController bookingOrderController;

    @Resource
    WebApplicationContext webApplicationContext;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .build();
    }

    @Test
    @WithMockUser("customUsername@example.io")
    void getDataBooking() throws Exception {
        String url = "/bookingOrders";
        FilterModel filters = new FilterModel();
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonRequest = objectMapper.writeValueAsString(filters);
        HashMap<String, Object> result = new HashMap<>();

        Mockito.when(bookingOrderService.getBookingByFilter(filters)).thenReturn(result);

        MockHttpServletResponse response = mvc.perform(MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
                        .param("pageNo", "1")
                        .param("perPage", "10")
                )
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).contains("totalItems");
    }
}
