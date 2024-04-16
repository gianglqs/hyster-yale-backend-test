/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.exception.UserException;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PasswordException extends Exception {
    // wrong_old_password
    // weak_password
    private String type;

    public PasswordException (String message, String type) {
        super(message);
        this.type = type;
    }
}
