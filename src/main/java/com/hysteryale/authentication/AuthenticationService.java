package com.hysteryale.authentication;


import com.hysteryale.authentication.payload.JwtResponse;
import com.hysteryale.authentication.payload.TokenRefreshResponse;
import com.hysteryale.model.User;
import com.hysteryale.response.ResponseObject;
import com.hysteryale.service.RoleService;
import com.hysteryale.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {


    private final PasswordEncoder passwordEncoder;

    @Resource
    RoleService roleService;
    @Resource
    JwtService jwtService;

    @Resource
    UserService userService;

    @Resource
    AuthenticationManager authenticationManager;


    // signup for user
//    public void register(User userReq) {
//        Role role = roleService.getRoleByRowName("ADMIN");
//        User user = User.builder()
//                .email(userReq.getEmail())
//                .password(userReq.getPassword())
//                .name(userReq.getName())
//                .isActive(true)
//                .role(role).build();
//        userService.addUser(user);
//    }

    public ResponseEntity<ResponseObject> login(User userReq) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            userReq.getEmail(),
                            userReq.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            Optional<User> userDB = userService.getActiveUserByEmail(userReq.getEmail());

            String accessToken = jwtService.generateAccessToken(userReq);
            String refreshToken = jwtService.generateRefreshToken(userReq);
            JwtResponse response = new JwtResponse(
                    accessToken,
                    refreshToken,
                    userDB.get().getName(),
                    userDB.get().getEmail(),
                    userDB.get().getRole().getRoleName());

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseObject(
                                    "Login successfully",
                                    response
                            )
                    );
        } catch (AuthenticationException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseObject(
                            "Username or password is incorrect!",
                            null));
        }
    }

    public ResponseEntity<ResponseObject> refreshToken(String refreshToken){
        //check token
        if(jwtService.validateToken(refreshToken)){
            //get user from
            String username = jwtService.extractUsername(refreshToken);
            User user = userService.getActiveUserByEmail(username).get();

            String newAccessToken = jwtService.generateAccessToken(user);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseObject("Refresh token is successfully!",
                            new TokenRefreshResponse(
                                    newAccessToken,
                                    refreshToken
                            )));
        }
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ResponseObject("RefreshToken is invalid!", null));
    }

}
