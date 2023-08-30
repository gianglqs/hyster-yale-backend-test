package com.hysteryale.controller;

import com.hysteryale.model.Role;
import com.hysteryale.model.User;
import com.hysteryale.repository.UserRepository;
import com.hysteryale.service.UserService;
import com.hysteryale.service.impl.EmailServiceImpl;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.annotation.Resource;
import java.util.*;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
public class UserControllerTest {
    private AutoCloseable autoCloseable;
    @Resource @InjectMocks
    UserController userController;
    @Resource
    @Mock
    UserService userService;
    @Resource
    @Mock
    private UserRepository userRepository;
    @Resource
    @Mock
    private EmailServiceImpl emailService;


    @BeforeEach
    void setUp(){
        autoCloseable = MockitoAnnotations.openMocks(this);
    }
    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }
    @Test
    void testGetAllUsers() {
        // GIVEN
        Role role = new Role(1, "admin", null);
        User given1 = new User(1,"user","admin2@gmail.com","$2a$10$oTxck2rZyU6y6LbUrUM3Zey/CBjNRonGAQ3cM5.QjzkRVIw5.hOhm",role,"us", true);
        User given2 = new User(2, "given2", "given2@gmail.com", "given", role, "us", true);

        List<User> givenList = new ArrayList<>();
        givenList.add(given1);
        givenList.add(given2);
        userRepository.saveAll(givenList);
        // WHEN
        when(userService.getAllUsers()).thenReturn(givenList);
        Map<String, List<User>> result = userController.getAllUsers();

        // THEN
        Assertions.assertEquals(givenList.size(), result.get("userList").size());
    }
    @Test
    void testAddUser() throws MailjetSocketTimeoutException, MailjetException {
        // GIVEN
        Role role = new Role(1, "admin", null);
        User givenUser = new User(1, "given1", "given2@gmail.com", "user", role, "us", true);

        // WHEN
        userController.addUser(givenUser);

        // THEN
        Mockito.verify(userService).addUser(givenUser);
        Mockito.verify(emailService).sendRegistrationEmail(givenUser.getUserName(), givenUser.getPassword(), givenUser.getEmail());
    }
    @Test
    void testDeactivateUser() {
        // GIVEN
        Role role = new Role(1, "admin", null);
        User givenUser = new User(1, "given1", "given2@gmail.com", "user", role, "us", true);

        // WHEN
        when(userService.getUserById(givenUser.getId())).thenReturn(Optional.of(givenUser));
        userController.activateUser(givenUser.getId());

        // THEN
        verify(userService).setUserActiveState(givenUser, false);
    }

    @Test
    void testActivateUser() {
        // GIVEN
        Role role = new Role(1, "admin", null);
        User givenUser = new User(1, "given1", "given2@gmail.com", "user", role, "us", false);

        // WHEN
        when(userService.getUserById(givenUser.getId())).thenReturn(Optional.of(givenUser));
        userController.activateUser(givenUser.getId());

        // THEN
        verify(userService).setUserActiveState(givenUser, true);
    }
    @Test
    void testGetUserById() {
        // GIVEN
        Role role = new Role(1, "admin", null);
        User givenUser = new User(1, "given1", "given2@gmail.com", "user", role, "us", true);

        // WHEN
        when(userService.getUserById(givenUser.getId())).thenReturn(Optional.of(givenUser));
        Optional<User> result = userController.getUserById(givenUser.getId());

        // THEN
        Mockito.verify(userService).getUserById(givenUser.getId());
        Assertions.assertEquals(givenUser, result.get());
    }

    @Test
    void testSearchUserByUserName() {
        // GIVEN
        Role role = new Role(1, "admin", null);
        User given1 = new User(1, "given1", "given1@gmail.com", "given", role, "us", true);
        User given2 = new User(2, "given2", "given2@gmail.com", "given", role, "us", true);
        List<User> userList = new ArrayList<>();
        userList.add(given1);
        userList.add(given2);

        String userName = "given";

        // WHEN
        when(userService.searchUserByUserName(userName)).thenReturn(userList);
        Map<String, List<User>> result = userController.searchUserByUserName(userName);

        // THEN
        Mockito.verify(userService).searchUserByUserName(userName);
        Assertions.assertEquals(userList.size(), result.get("searchedUserList").size());
    }
    @Test
    void testUpdateUserInformation() {
        // GIVEN
        Role role = new Role(1, "admin", null);
        User given1 = new User(1, "given1", "given1@gmail.com", "given", role, "us", true);

        // WHEN
        when(userService.getUserByEmail(given1.getEmail())).thenReturn(Optional.of(given1));
        userController.updateUserInformation(given1);

        // THEN
        Mockito.verify(userService).updateUserInformation(given1, given1);
    }
}