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
public class UserIdNotFoundException extends Exception{
    private int userId;

    public UserIdNotFoundException(String message, int userId) {
        super(message);
        this.userId = userId;
    }
}
