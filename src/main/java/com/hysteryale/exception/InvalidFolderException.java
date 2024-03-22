package com.hysteryale.exception;

public class InvalidFolderException extends RuntimeException {
    public InvalidFolderException(String message) {
        super(message);
    }
}
