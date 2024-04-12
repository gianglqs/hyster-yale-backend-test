/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.service;

import com.hysteryale.exception.UserException.EmailNotFoundException;
import com.hysteryale.exception.UserException.ExistingEmailException;
import com.hysteryale.exception.UserException.UserIdNotFoundException;
import com.hysteryale.model.Role;
import com.hysteryale.model.User;
import com.hysteryale.repository.UserRepository;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Slf4j
@SpringBootTest
@Transactional
public class UserServiceTest {
    @Resource
    UserService userService;

    @Resource
    UserRepository userRepository;

    private PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Test
    public void testGetUserById() throws UserIdNotFoundException {
        User newUser = userRepository.save(new User("New User", "newuser@gmail.com", "12345678", new Role(2, "USER")));

        int userId = newUser.getId();
        User dbUser = userService.getUserById(userId);

        Assertions.assertEquals(userId, dbUser.getId());
    }

    @Test
    public void testGetUserById_notFound() {
        int userId = 123123123;
        UserIdNotFoundException exception = Assertions.assertThrows(UserIdNotFoundException.class, () -> userService.getUserById(userId));
        Assertions.assertEquals("NOT_FOUND: userId " + userId, exception.getMessage());
        Assertions.assertEquals(userId, exception.getUserId());
    }

    @Test
    public void testAddUser() throws ExistingEmailException {
        User newUser = new User("New User", "newuser@gmail.com", "12345678", new Role(2, "USER"));
        userService.addUser(newUser);

        Optional<User> optionalUser = userRepository.findById(newUser.getId());
        Assertions.assertTrue(optionalUser.isPresent());
        Assertions.assertEquals("newuser@gmail.com", optionalUser.get().getEmail());
    }

    @Test
    public void testAddUser_emailTaken() {
        User newUser = new User("admin", "admin@gmail.com", "12345678", new Role(2, "ADMIN"));
        ExistingEmailException exception = Assertions.assertThrows(ExistingEmailException.class, () -> userService.addUser(newUser));

        Assertions.assertEquals("EXISTING EMAIL " + exception.getEmail(), exception.getMessage());
        Assertions.assertEquals(newUser.getEmail(), exception.getEmail());
    }

    @Test
    public void testGetUserByEmail() throws EmailNotFoundException {
        String email = "admin@gmail.com";

        User dbUser = userService.getUserByEmail(email);
        Assertions.assertEquals(email, dbUser.getEmail());
    }

    @Test
    public void testGetUserByEmail_notFound() {
        String email = "2137hdgdyete21@gmail.com";

        EmailNotFoundException exception =
                Assertions.assertThrows(EmailNotFoundException.class, () -> userService.getUserByEmail(email));

        Assertions.assertEquals("NOT FOUND EMAIL " + email, exception.getMessage());
        Assertions.assertEquals(email, exception.getEmail());
    }

    @Test
    public void testGetActiveUserByEmail() {
        String email = "admin@gmail.com";

        Optional<User> optionalUser = userService.getActiveUserByEmail(email);
        Assertions.assertTrue(optionalUser.isPresent());
        Assertions.assertEquals(email, optionalUser.get().getEmail());
    }

    @Test
    public void testGetActiveUserByEmail_notFound() {
        String email = "12312379uih21isd@gmail.com";

        Optional<User> optionalUser = userService.getActiveUserByEmail(email);
        Assertions.assertTrue(optionalUser.isEmpty());
    }

    @Test
    public void testSetActiveState() throws UserIdNotFoundException {
        boolean isActive = true;
        User newUser = userRepository.save(new User("New User", "admin@gmail.com", "12345678", new Role(2, "ADMIN"), isActive));

        userService.setUserActiveState(newUser, !isActive);
        User dbUser = userService.getUserById(newUser.getId());
        Assertions.assertEquals(!isActive, dbUser.isActive());
    }

    @Test
    public void testUpdateUserInformation() throws UserIdNotFoundException {
        String updatedUsername = "New User 123456";
        User newUser = userRepository.save(new User("New User", "newuser@gmail.com", "12345678", new Role(1, "ADMIN"), true));
        userService.updateUserInformation(newUser, new User("New User 123456", "newuser@gmail.com", "123456789", new Role(1, "ADMIN")));

        User dbUser = userService.getUserById(newUser.getId());
        Assertions.assertEquals(updatedUsername, dbUser.getName());
    }

    @Test
    public void testChangeUserPassword() throws UserIdNotFoundException {
        String originalPassword = "123456789";
        String updatedPassword = "Newuser123456;";
        User newUser = userRepository.save(new User("New User", "newuser@gmail.com", "12345678", new Role(1, "ADMIN"), true));

        userService.changeUserPassword(newUser, updatedPassword);
        User dbUser = userService.getUserById(newUser.getId());

        Assertions.assertNotEquals(originalPassword, dbUser.getPassword());
        Assertions.assertTrue(passwordEncoder().matches(updatedPassword, dbUser.getPassword()));
    }

    @Test
    public void testResetUserPassword() throws MailjetSocketTimeoutException, MailjetException, UserIdNotFoundException, EmailNotFoundException {
        String originalPassword = "123456789";
        User newUser = userRepository.save(new User("New User", "newuser@gmail.com", originalPassword, new Role(1, "ADMIN"), true));
        userService.resetUserPassword(newUser.getEmail());

        User dbUser = userService.getUserById(newUser.getId());
        Assertions.assertNotEquals(originalPassword, dbUser.getPassword());
    }

    @Test
    public void testSearchUser() {
        String searchString = "ad";
        int pageNo = 1;
        int perPage = 100;

        Page<User> userPage = userService.searchUser(searchString, pageNo, perPage, "");
        List<User> userList = userPage.getContent();

        Assertions.assertFalse(userList.isEmpty());
        Assertions.assertEquals(pageNo, userPage.getNumber() + 1);
    }

    @Test
    public void testSearchUser_notFound() {
        String searchString = "12381723iuasehiqe";
        int pageNo = 1;
        int perPage = 100;

        Page<User> userPage = userService.searchUser(searchString, pageNo, perPage, "");
        List<User> userList = userPage.getContent();

        Assertions.assertTrue(userList.isEmpty());
        Assertions.assertEquals(pageNo, userPage.getNumber() + 1);
    }

    @Test
    public void testLoadUserByUsername() {
        String username = "admin@gmail.com";

        UserDetails dbUser = userService.loadUserByUsername(username);
        Assertions.assertEquals(username, dbUser.getUsername());
    }

    @Test
    public void testLoadUserByUsername_notFound() {
        String username = "asdjiuh1i3126378@gmail.com";

        UsernameNotFoundException exception = Assertions.assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername(username));
        Assertions.assertEquals("User not found: " + username, exception.getLocalizedMessage());
    }

    @Test
    public void testIsPasswordMatched() {
        String rawPassword = "123456789";
        User newUser = userRepository.save(new User("New User", "newuser@gmail.com", passwordEncoder().encode(rawPassword), new Role(1, "ADMIN"), true));

        Assertions.assertTrue(userService.isPasswordMatched(rawPassword, newUser));
    }

    @Test
    public void testIsPasswordMatched_notMatched() {
        String rawPassword = "123456789";
        User newUser = userRepository.save(new User("New User", "newuser@gmail.com", passwordEncoder().encode(rawPassword), new Role(1, "ADMIN"), true));

        Assertions.assertFalse(userService.isPasswordMatched("udbfhusbfuebfe", newUser));
    }

}