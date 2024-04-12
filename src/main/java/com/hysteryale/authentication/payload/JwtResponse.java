/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.authentication.payload;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtResponse {
    private String accessToken;
    private String type = "Bearer";
    private String refreshToken;
    private String name;
    private String email;
    private String role;
    private String defaultLocale;
    private int id;

    public JwtResponse(String token, String refreshToken, String name, String email, String roles, String defaultLocale, int id) {
        this.accessToken = token;
        this.refreshToken = refreshToken;
        this.name = name;
        this.email = email;
        this.role = roles;
        this.defaultLocale = defaultLocale;
        this.id = id;
    }
}
