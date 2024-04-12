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
public class ExistingEmailException extends Exception{
    private String email;

    public ExistingEmailException (String message, String email) {
        super(message);
        this.email = email;
    }
}
