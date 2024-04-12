/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CannotExtractDateException extends Exception{
    private String fileName;

    public CannotExtractDateException(String message, String fileName) {
        super(message);
        this.fileName = fileName;
    }
}
