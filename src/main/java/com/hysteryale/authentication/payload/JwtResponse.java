package com.hysteryale.authentication.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private String refreshToken;
    private String name;
    private String email;
    private String roles;

    public JwtResponse(String token, String refreshToken, String name, String email, String roles) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.name = name;
        this.email = email;
        this.roles = roles;
    }
}
