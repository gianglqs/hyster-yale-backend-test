package com.hysteryale.repositories;

import com.hysteryale.model.User;
import com.hysteryale.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.Page;
import org.springframework.test.context.TestPropertySource;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@DataJpaTest
//@PropertySource({ "classpath:application-test.properties" })
//@TestPropertySource(locations = "classpath:application-test.properties")
public class UserRepositoryTest {

    @Resource
    UserRepository userRepository;

    @Test
    void findAll() {
        List<User> getAll = (List<User>) userRepository.findAll();

        Assertions.assertEquals(getAll.size(), 3);
    }

    @Test
    void getUserByEmail(){
        Optional<User> adminUser = userRepository.getUserByEmail("admin@gmail.com");
        Optional<User> userGiang = userRepository.getUserByEmail("songiang@gmail.com");

        Assertions.assertTrue(adminUser.isPresent());
        Assertions.assertFalse(userGiang.isPresent());
    }

    @Test
    void isEmailExisted(){
        String emailExist = "admin@gmail.com";
        String emailNotExist = "songiang@gmail.com";

        boolean isEmailExistedResult = userRepository.isEmailExisted(emailExist);
        boolean isEmailNotExistedResult = userRepository.isEmailExisted(emailNotExist);

        Assertions.assertTrue(isEmailExistedResult);
        Assertions.assertFalse(isEmailNotExistedResult);
    }

    @Test
    void getActiveUserByEmail(){
        String emailUserActive = "admin@gmail.com";
        String emailUserNotActive = "giang@gmail.com";

        Optional<User> getUserNotActive = userRepository.getActiveUserByEmail(emailUserNotActive);
        Optional<User> getUserActive = userRepository.getActiveUserByEmail(emailUserActive);

        Assertions.assertTrue(getUserActive.isPresent());
        Assertions.assertFalse(getUserNotActive.isPresent());
    }

    @Test
    void searchUser(){
        String emailExist = "admin@gmail.com";
        String usernameExist = "admin";
        String emailNotExist = "songiang@gmail.com";
        String usernameNotExist = "songiang";

        Page<User> searchUserByEmailExist  = userRepository.searchUser(emailExist, null);
        Page<User> searchUserByEmailNotExist  = userRepository.searchUser(emailNotExist, null);
        Page<User> searchUserByUsernameExist  = userRepository.searchUser(usernameExist, null);
        Page<User> searchUserByUsernameNotExist  = userRepository.searchUser(usernameNotExist, null);

        Assertions.assertEquals(searchUserByEmailExist.getTotalElements(), 1);
        Assertions.assertEquals(searchUserByEmailNotExist.getTotalElements(), 0);
        Assertions.assertEquals(searchUserByUsernameExist.getTotalElements(), 1);
        Assertions.assertEquals(searchUserByUsernameNotExist.getTotalElements(), 0);
    }
}
