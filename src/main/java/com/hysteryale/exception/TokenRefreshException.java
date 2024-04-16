/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.exception;

public class TokenRefreshException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public TokenRefreshException(String token, String message) {
        super(String.format("Failed for [%s]: %s", token, message));
    }
}
