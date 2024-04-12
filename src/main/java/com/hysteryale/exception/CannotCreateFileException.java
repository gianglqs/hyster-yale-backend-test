/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CannotCreateFileException extends Exception{
    private String fileName;

    public CannotCreateFileException (String message, String fileName) {
        super(message);
        this.fileName = fileName;
    }
}
