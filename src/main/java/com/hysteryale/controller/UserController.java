package com.hysteryale.controller;

import com.hysteryale.authentication.AuthenticationService;
import com.hysteryale.authentication.JwtService;
import com.hysteryale.model.User;
import com.hysteryale.response.ResponseObject;
import com.hysteryale.service.UserService;
import com.hysteryale.service.impl.EmailServiceImpl;
import com.hysteryale.utils.StringUtils;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
@Slf4j
public class UserController {
    @Resource
    public UserService userService;
    @Resource
    EmailServiceImpl emailService;

    @Resource
    AuthenticationService authenticationService;
    @Resource
    JwtService jwtService;

    /**
     * Get user's details by userId
     */
    @GetMapping(path = "users/getDetails/{userId}")
    public Map<String, User> getUserDetailsById(@PathVariable int userId) {
        Map<String, User> userMap = new HashMap<>();
        userMap.put("userDetails", userService.getUserById(userId));
        return userMap;
    }

    /**
     * Reverse user's active state into true or false based on whether user is active or not
     */
    @PutMapping(path = "users/activate/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void activateUser(@PathVariable int userId) {
        User user = userService.getUserById(userId);
        userService.setUserActiveState(user, !user.isActive());
    }

    /**
     * Add new User and send informing email to registered email
     *
     * @param user mapping from JSON format
     */
    @PostMapping(path = "/users", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public void addUser(@Valid @RequestBody User user) {
        String password = user.getPassword();

        user.setActive(true);
        userService.addUser(user);

        try {
            emailService.sendRegistrationEmail(user.getName(), password, user.getEmail());
        } catch (MailjetSocketTimeoutException | MailjetException e) {
            log.error(e.toString());
        }
    }

    /**
     * Update user's information
     */
    @PutMapping(path = "/users/updateUser/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateUserInformation(@RequestBody User updateUser, @PathVariable int userId) {
        User dbUser = userService.getUserById(userId);
        userService.updateUserInformation(dbUser, updateUser);
        return ResponseEntity.ok("Update user's information successfully");
    }

    /**
     * Get list of users based on searchString (if searchString is null then get all users)
     */
    @GetMapping(path = "/users")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Map<String, Object> searchUser(@RequestParam(required = false) String search,
                                          @RequestParam(defaultValue = "100") int perPage,
                                          @RequestParam(defaultValue = "1") int pageNo,
                                          @RequestParam(defaultValue = "ascending") String sortType) {

        Page<User> userPage = userService.searchUser(search, pageNo, perPage, sortType);

        Map<String, Object> userPageMap = new HashMap<>();

        userPageMap.put("perPage", perPage);
        userPageMap.put("userList", userPage.getContent());
        userPageMap.put("page", userPage.getNumber());
        userPageMap.put("totalPages", userPage.getTotalPages());
        userPageMap.put("totalItems", userPage.getTotalElements());

        return userPageMap;
    }

    /**
     * Change user's password, {userId, password} passed from JSON format
     */
    @PostMapping(path = "/users/changePassword", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> changePassword(@RequestParam("oldPassword") String oldPassword,
                                            @RequestParam("newPassword") String newPassword,
                                            @RequestHeader("Authorization") String accessToken) {

        String email = jwtService.extractUsername(accessToken.split(" ")[1].trim());
        User dbUser = userService.getUserByEmail(email);

        // Check the old password
        if(userService.isPasswordMatched(oldPassword, dbUser)) {
            // Check the strength of the new password
            if (!StringUtils.checkPasswordStreng(newPassword))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must consist of at least 12 characters and has at least\n" +
                        "\n" +
                        "    one Uppercase character,\n" +
                        "    one Lowercase character,\n" +
                        "    a Digit and\n" +
                        "    a Special character or Symbol.");
            else {
                userService.changeUserPassword(dbUser, newPassword);
                return ResponseEntity.ok("Password has been changed successfully");
            }
        }
        else
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Old password is not correct.");
    }

    /**
     * Reset user's password specified by email (if email is existed), then send informing email for user.
     */
    @PostMapping(path = "/users/resetPassword")
    public void resetPassword(@RequestBody User user) throws MailjetSocketTimeoutException, MailjetException {
        userService.resetUserPassword(user.getEmail());
    }

    /**
     * Revoke the access_token for logging user out
     */
//    @PostMapping(path = "/oauth/revokeAccessToken")
//    public void revokeAccessToken(@RequestHeader("Authorization") String accessToken) {
//        tokenServices.revokeToken(accessToken.substring(6));
//    }
    @PostMapping(path = "/oauth/checkToken")
    public void checkToken() {
    }

    @PostMapping(path = "/oauth/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseObject> login(@RequestBody User user) {
        return authenticationService.login(user);
    }

//    @PostMapping(path = "/oauth/register")
//    public void register(@RequestBody User user) {
//         authenticationService.register(user);
//    }

    @PostMapping(path = "/oauth/checkTokenOfAdmin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void checkTokenOfAdmin() {}
}
