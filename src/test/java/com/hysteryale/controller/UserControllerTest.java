/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.controller;

import com.hysteryale.authentication.JwtService;
import com.hysteryale.model.Role;
import com.hysteryale.model.User;
import com.hysteryale.repository.UserRepository;
import com.hysteryale.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Resource;
import java.util.Objects;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private MockMvc mockMvc;

    @Resource
    UserRepository userRepository;
    @Resource
    UserService userService;
    @Resource
    JwtService jwtService;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }
    @Test
    @WithMockUser(authorities = "ADMIN")
    public void testGetUserById() throws Exception {
        User user = userRepository.save(new User("test", "test@gmail.com", "12345678", new Role(1, "ADMIN")));
        MvcResult result =
                mockMvc.perform(get("/users/getDetails/" + user.getId()).contentType(MediaType.APPLICATION_JSON))
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
        Assertions.assertTrue(result.getResponse().getContentAsString().contains(user.getName()));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void testGetUserById_NotFound() throws Exception {
        int notFoundUserId = 123123;
        MvcResult response =
                mockMvc
                        .perform(get("/users/getDetails/" + notFoundUserId))
                        .andReturn();
        Assertions.assertEquals(404, response.getResponse().getStatus());
        log.info(response.getResponse().getContentAsString());
        Assertions.assertTrue(response.getResponse().getContentAsString().contains("Cannot found user with id: " + notFoundUserId));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void testAddUser() throws Exception {
        User user = new User("admin1", "weortuoewrtiu@gmail.com", "12345678", new Role(1, "USER"));
        MvcResult result =
                mockMvc
                        .perform(post("/users").content(parseUserToJSONString(user)).contentType(MediaType.APPLICATION_JSON))
                        .andReturn();

        Assertions.assertEquals(200, result.getResponse().getStatus());
    }


    @Test
    @WithMockUser(authorities = "ADMIN")
    public void testAddUser_EmailExistence() throws Exception {
        User user = new User("admin", "admin@gmail.com", "12345678", new Role(1, "ADMIN"));
        MvcResult result =
                mockMvc
                        .perform(post("/users").content(parseUserToJSONString(user)).contentType(MediaType.APPLICATION_JSON))
                        .andReturn();

        Assertions.assertEquals(400, result.getResponse().getStatus());
        Assertions.assertTrue(result.getResponse().getContentAsString().contains("This email has been registered: " + user.getEmail()));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void testActivateUser_changeToLock() throws Exception {
        User user = userRepository.save(User.builder()
                        .name("test")
                        .email("test@gmail.com")
                        .password("12345678")
                        .isActive(true)
                        .role(new Role(2, "USER"))
                        .build());
        MvcResult result =
                mockMvc
                        .perform(put("/users/activate/" + user.getId()))
                        .andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
        Assertions.assertFalse(userService.getUserById(user.getId()).isActive());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void testActivateUser_changeToActive() throws Exception {
        User user = userRepository.save(User.builder()
                .name("test")
                .email("test@gmail.com")
                .password("12345678")
                .isActive(false)
                .role(new Role(2, "USER"))
                .build());
        MvcResult result =
                mockMvc
                        .perform(put("/users/activate/" + user.getId()))
                        .andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
        Assertions.assertTrue(userService.getUserById(user.getId()).isActive());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void testUpdateUserInformation() throws Exception {
        User user = userRepository.save(User.builder()
                .name("test")
                .email("test@gmail.com")
                .password("12345678")
                .isActive(false)
                .defaultLocale("English")
                .role(new Role(2, "USER"))
                .build());
        String name = "test123";
        Role role = new Role(1, "ADMIN");
        User updatedUser = User.builder().name(name).role(role).build();

        mockMvc.perform(
                put(
                        "/users/updateUser/" + user.getId())
                        .content(parseUserToJSONString(updatedUser))
                        .contentType(MediaType.APPLICATION_JSON)
        );
        User dbUser = userService.getUserById(user.getId());
        Assertions.assertEquals(name, dbUser.getName());
        Assertions.assertEquals(role.getRoleName(), dbUser.getRole().getRoleName());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void testChangePassword() throws Exception {
        String email = "admin@gmail.com";
        String password = "123456";
        String token = jwtService.generateAccessToken(new User(email, password));
        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.multipart("/users/changePassword")
                        .param("oldPassword", "123456")
                        .param("newPassword", "Admin123456;")
                        .header("Authorization", "Bearer " + token)
        ).andReturn();

        User dbUser = userService.getUserByEmail(email);
        Assertions.assertEquals(200, result.getResponse().getStatus());
        Assertions.assertTrue(result.getResponse().getContentAsString().contains("Password has been changed successfully"));
        Assertions.assertTrue(passwordEncoder().matches("Admin123456;", dbUser.getPassword()));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void testChangePassword_wrongOldPassword() throws Exception {
        String email = "admin@gmail.com";
        String password = "123456";
        String token = jwtService.generateAccessToken(new User(email, password));
        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.multipart("/users/changePassword")
                        .param("oldPassword", "1234")
                        .param("newPassword", "Admin123456;")
                        .header("Authorization", "Bearer " + token)
        ).andReturn();

        Assertions.assertEquals(400, result.getResponse().getStatus());
        Assertions.assertTrue(result.getResponse().getContentAsString().contains("Old password is not correct."));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void testChangePassword_weakNewPassword() throws Exception {
        String email = "admin@gmail.com";
        String password = "123456";
        String token = jwtService.generateAccessToken(new User(email, password));
        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.multipart("/users/changePassword")
                        .param("oldPassword", "123456")
                        .param("newPassword", "Admin1234")
                        .header("Authorization", "Bearer " + token)
        ).andReturn();

        Assertions.assertEquals(400, result.getResponse().getStatus());
        Assertions.assertTrue(
                result
                        .getResponse()
                        .getContentAsString()
                        .contains("Password must consist of at least 12 characters and has at least"));
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void testResetPassword() throws Exception {
        User user = User.builder()
                .name("test")
                .email("admin@gmail.com")
                .password("12345678")
                .isActive(false)
                .defaultLocale("English")
                .role(new Role(2, "USER"))
                .build();

        MvcResult result =
                mockMvc
                        .perform(
                                post("/users/resetPassword")
                                        .content(parseUserToJSONString(user))
                                        .contentType(MediaType.APPLICATION_JSON)
                        ).andReturn();

        Assertions.assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void testResetPassword_emailNotFound() throws Exception{
        User user = User.builder()
                .name("test")
                .email("admin123123@gmail.com")
                .password("12345678")
                .isActive(false)
                .defaultLocale("English")
                .role(new Role(2, "USER"))
                .build();

        MvcResult result =
                mockMvc
                        .perform(
                                post("/users/resetPassword")
                                        .content(parseUserToJSONString(user))
                                        .contentType(MediaType.APPLICATION_JSON)
                        ).andReturn();

        Assertions.assertEquals(404, result.getResponse().getStatus());
        Assertions.assertTrue(result.getResponse().getContentAsString().contains("Cannot found user with email: " + user.getEmail()));
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void testLogin() throws Exception {
        User user = User.builder()
                .name("test")
                .email("admin@gmail.com")
                .password("123456")
                .isActive(false)
                .role(new Role(2, "USER"))
                .build();

        MvcResult result =
                mockMvc
                        .perform(post("/oauth/login")
                                .content(parseUserToJSONString(user))
                                .contentType(MediaType.APPLICATION_JSON)
                        )
                        .andExpect(jsonPath("$.data.accessToken").exists())
                        .andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
        Assertions.assertTrue(result.getResponse().getContentAsString().contains("Login successfully"));
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void testLogin_failed() throws Exception {
        User user = User.builder()
                .name("test")
                .email("admin@gmail.com")
                .password("123")
                .isActive(false)
                .role(new Role(2, "USER"))
                .build();

        MvcResult result =
                mockMvc
                        .perform(post("/oauth/login")
                                .content(parseUserToJSONString(user))
                                .contentType(MediaType.APPLICATION_JSON)
                        )
                        .andReturn();
        Assertions.assertEquals(400, result.getResponse().getStatus());
        Assertions.assertTrue(result.getResponse().getContentAsString().contains("Username or password is incorrect"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void testSearchUser() throws Exception {

        String strSearch = "a";
        int perPage = 100;

        MvcResult result =
                mockMvc
                        .perform(get("/users")
                                .param("search", strSearch)
                                .param("perPage", Integer.toString(perPage))
                                .param("pageNo", "1")
                        )
                        .andExpect(jsonPath("$.userList").isArray())
                        .andExpect(jsonPath("$.perPage").value(perPage))
                        .andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
    }

    /**
     * Test case entering search string for getting list of all user
     */
    @Test
    @WithMockUser(authorities = "ADMIN")
    public void testSearchUser_emptySearchString() throws Exception {
        String strSearch = "";
        int perPage = 100;

        MvcResult result =
                mockMvc
                        .perform(get("/users")
                                .param("search", strSearch)
                                .param("perPage", Integer.toString(perPage))
                                .param("pageNo", "1")
                        )
                        .andExpect(jsonPath("$.userList").isArray())
                        .andExpect(jsonPath("$.perPage").value(perPage))
                        .andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void testSearchUser_searchNotFound() throws Exception {
        String strSearch = "qwerqweqeqweqwe";
        int perPage = 100;

        MvcResult result =
                mockMvc
                        .perform(get("/users")
                                .param("search", strSearch)
                                .param("perPage", Integer.toString(perPage))
                                .param("pageNo", "1")
                        )
                        .andExpect(jsonPath("$.userList").isArray())
                        .andExpect(jsonPath("$.perPage").value(perPage))
                        .andExpect(jsonPath("$.totalItems").value(0))
                        .andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
    }



    private PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Parse User object into JSON String for passing to Controller testing
     */
    public String parseUserToJSONString(User user) throws JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", user.getName());
        jsonObject.put("email", user.getEmail());
        jsonObject.put("password", user.getPassword());

        JSONObject role = new JSONObject();
        role.put("id", user.getRole().getId());
        role.put("roleName", user.getRole().getRoleName());
        jsonObject.put("role", role);

        return jsonObject.toString();
    }
}
