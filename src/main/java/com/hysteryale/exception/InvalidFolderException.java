/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.exception;

public class InvalidFolderException extends RuntimeException {
    public InvalidFolderException(String message) {
        super(message);
    }
}
