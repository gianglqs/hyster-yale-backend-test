package com.hysteryale.service;

import com.hysteryale.model.Role;
import com.hysteryale.model.User;
import com.hysteryale.repository.UserRepository;
import com.hysteryale.service.impl.EmailServiceImpl;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
public class UserServiceTest {
    @Resource
    @Mock
    private UserRepository userRepository;
    @Resource @InjectMocks
    private UserService underTest;
    @Resource
    @Mock
    EmailServiceImpl emailService;
    private AutoCloseable autoCloseable;
    int pageNo = 1;
    int perPage = 100;
    String sortType = "ascending";

    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception{
        autoCloseable.close();
    }

    @Test
    void testGetAllUsers() {
        // GIVEN
        Role role = new Role(1, "admin");
        User given1 = new User(1,"user","admin2@gmail.com","$2a$10$oTxck2rZyU6y6LbUrUM3Zey/CBjNRonGAQ3cM5.QjzkRVIw5.hOhm",role,"us", true);
        User given2 = new User(2, "given2", "given2@gmail.com", "given", role, "us", true);
        List<User> userList = new ArrayList<>();
        userList.add(given1);
        userList.add(given2);

        underTest.addUser(given1);
        underTest.addUser(given2);

        // WHEN
        when(userRepository.findAll()).thenReturn(userList);
        List<User> result = underTest.getAllUsers();

        // THEN
        Assertions.assertEquals(userList.size(), result.size());     // assert the result
        verify(userRepository).findAll();                            // verify the flow of function
    }
    @Test
    void testThrowNotFoundIfIdIsNotExisted() {
        //GIVEN
        Integer accountId = 0;

        //WHEN
//        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, ()-> underTest.getUserById(accountId));

            // expected
    //    HttpStatus expectedStatus = HttpStatus.NOT_FOUND;
            //return
  //      HttpStatus returnStatus = responseStatusException.getStatus();

        // THEN
    //    Assertions.assertEquals(expectedStatus, returnStatus);
    }

    @Test
    void testAddUser() {
        // GIVEN
        Role role = new Role(1, "admin");
        User givenUser = new User("givenUser", "test@gmail.com", "123456", role);

        // WHEN
        underTest.addUser(givenUser);

        // THEN
        ArgumentCaptor<User> accountArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(accountArgumentCaptor.capture());
        User capturedUser = accountArgumentCaptor.getValue();

        Assertions.assertEquals(capturedUser, givenUser);
    }
    @Test
    void testThrowExceptionIfEmailIsTaken() {
        //GIVEN
        Role role = new Role(1, "admin");
        String email = "admin@gmail.com";
        User givenUser = new User("givenUser", email, "123456", role);
        given(userRepository.isEmailExisted(givenUser.getEmail())).willReturn(true);

        //THEN
        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () -> underTest.addUser(givenUser));

        HttpStatus expectedStatus = HttpStatus.BAD_REQUEST;
        HttpStatus returnStatus = responseStatusException.getStatus();

        Assertions.assertEquals(returnStatus, expectedStatus);
    }

    @Test
    void testCheckIfEmailIsExisted() {
        // GIVEN
        Role role = new Role(1, "admin");
        String email = "admin@gmail.com";
        User givenUser = new User("givenUser", email, "123456", role);

        //WHEN
        underTest.addUser(givenUser);

        //THEN
        verify(userRepository).isEmailExisted(email);
    }

    @Test
    void testGetUserByEmail() {
        //GIVEN
        Role role = new Role(1, "admin");
        User given1 = new User(1, "given1", "admin@gmail.com", "given", role, "us", true);
        String email = "admin@gmail.com";

        //WHEN
        when(userRepository.getUserByEmail(email)).thenReturn(Optional.of(given1));
        underTest.getUserByEmail(email);

        //THEN
        ArgumentCaptor<String> emailArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(userRepository).getUserByEmail(emailArgumentCaptor.capture());
        String capturedEmail = emailArgumentCaptor.getValue();

        Assertions.assertEquals(capturedEmail, email);
    }
    @Test
    void testGetActiveUserByEmail() {
        // GIVEN
        String email = "user1@gmail.com";

        // WHEN
        underTest.getActiveUserByEmail(email);

        // THEN
        Mockito.verify(userRepository).getActiveUserByEmail(email);
    }
    @Test
    void testSearchUserByUserName() {
        // GIVEN
//        Role role = new Role(1, "admin");
//        User given1 = new User(1, "given1", "given1@gmail.com", "given", role, "us", true);
//        User given2 = new User(2, "given2", "given2@gmail.com", "given", role, "us", true);
//        List<User> userList = new ArrayList<>();
//        userList.add(given1);
//        userList.add(given2);
//
//        String userName = "given";
//
//        // WHEN
//        when(userRepository.searchUser(userName, PageRequest.of(pageNo - 1, perPage, Sort.by("userName").ascending()))).thenReturn(new PageImpl<>(userList));
//        Page<User> result = underTest.searchUser(userName, pageNo, perPage, sortType);
//
//        // THEN
//        Mockito.verify(userRepository).searchUser(userName, PageRequest.of(pageNo - 1, perPage, Sort.by("userName").ascending()));
//        Assertions.assertEquals(userList.size(), result.getContent().size());
    }

    @Test
    void testResetPassword() throws MailjetSocketTimeoutException, MailjetException {

        // GIVEN
        Role role = new Role(1, "admin");
        User given1 = new User(1, "given1", "given1@gmail.com", "given", role, "us", true);

        String email = "given1@gmail.com";
        String oldPassword = given1.getPassword();

        // WHEN
        when(userRepository.getUserByEmail(email)).thenReturn(Optional.of(given1));
        underTest.resetUserPassword(email);

        // THEN
        Assertions.assertNotEquals(oldPassword, given1.getPassword());
    }
}