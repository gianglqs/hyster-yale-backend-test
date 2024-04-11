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
