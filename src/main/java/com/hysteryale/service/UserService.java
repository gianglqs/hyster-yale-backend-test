/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.service;

import com.hysteryale.exception.UserException.EmailNotFoundException;
import com.hysteryale.exception.UserException.ExistingEmailException;
import com.hysteryale.exception.UserException.UserIdNotFoundException;
import com.hysteryale.model.User;
import com.hysteryale.repository.UserRepository;
import com.hysteryale.service.impl.EmailServiceImpl;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Random;

@Service
public class UserService extends BasedService implements UserDetailsService {
    @Resource
    UserRepository userRepository;
    @Resource
    EmailServiceImpl emailService;

    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Getting a user by the given ID
     * @param userId: given Id
     * @return an User
     */
    public User getUserById(Integer userId) throws UserIdNotFoundException {
        Optional<User> user = userRepository.findById(userId);
        if(user.isEmpty()){
            throw new UserIdNotFoundException("NOT_FOUND: userId " + userId, userId);
        }
        return user.get();
    }

    /**
     * Adding new user with the encrypted password if the registered email is not existed
     * @param user : new registered User
     */
    public void addUser(User user) throws ExistingEmailException {
        if(!userRepository.isEmailExisted(user.getEmail())) {
            // encrypt password
            user.setPassword(passwordEncoder().encode(user.getPassword()));
            userRepository.save(user);
        }
        else
            throw new ExistingEmailException("EXISTING EMAIL " + user.getEmail(), user.getEmail());
    }

    /**
     * Getting a user by the email
     * @param email: given email
     * @return an User
     */
    public User getUserByEmail(String email) throws EmailNotFoundException {
        Optional<User> optionalUser = userRepository.getUserByEmail(email);
        if(optionalUser.isPresent())
            return optionalUser.get();
        else
            throw new EmailNotFoundException("NOT FOUND EMAIL " + email, email);
    }

    /**
     * Getting a user which is still active by email
     */
    public Optional<User> getActiveUserByEmail(String email) {return userRepository.getActiveUserByEmail(email); }

    /**
     * Set user's isActive state (isActive: true or false)
     */
    @Transactional
    public void setUserActiveState(User user, boolean isActive) {
        user.setActive(isActive);
    }

    /**
     * Update user's information: userName ,role, defaultLocale
     * @param dbUser user get from Database
     * @param updateUser user contained changed information
     */
    @Transactional
    public void updateUserInformation(User dbUser, User updateUser) {
        dbUser.setName(updateUser.getName());
        dbUser.setRole(updateUser.getRole());
        dbUser.setDefaultLocale(updateUser.getDefaultLocale());
    }
    @Transactional
    public void changeUserPassword(User user, String password) {
        user.setPassword(passwordEncoder().encode(password));
    }
    @Transactional
    public void setNewLastLogin(User user) {
        user.setLastLogin(LocalDate.now());
    }
    @Transactional
    public void resetUserPassword(String email) throws MailjetSocketTimeoutException, MailjetException, EmailNotFoundException {
        User user = getUserByEmail(email);
        StringBuilder newPassword = new StringBuilder();

        Random random = new Random();
        for(int i = 0; i < 8; i ++) {
            char c = (char) ('a' + random.nextInt(26));
            newPassword.append(c);
        }
        emailService.sendResetPasswordEmail(user.getName(), newPassword.toString(), user.getEmail());
        user.setPassword(passwordEncoder().encode(newPassword.toString()));
    }

    /**
     * Get Users based on searchString (search by userName and email)
     * @param searchString for searching userName or email
     * @param pageNo current page number
     * @param perPage items per page
     * @param sortType type of sort (ascending or descending)
     */
    public Page<User> searchUser(String searchString, int pageNo, int perPage, String sortType) {
        Pageable pageable = PageRequest.of(pageNo - 1, perPage, Sort.by("name").ascending());
        if(sortType.equals("descending"))
            pageable = PageRequest.of(pageNo - 1, perPage, Sort.by("name").descending());
        return userRepository.searchUser(searchString, pageable);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
       return userRepository
               .getActiveUserByEmail(username)
               .orElseThrow(
                       () -> new UsernameNotFoundException("User not found: " + username)
               );
    }

    /**
     * Check if the password is match with a user's password in db (for changing password feature)
     */
    public boolean isPasswordMatched(String password, User dbUser) {
        return passwordEncoder().matches(password, dbUser.getPassword());
    }
}
