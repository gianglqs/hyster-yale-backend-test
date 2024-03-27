package com.hysteryale.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvalidFileFormatException extends Exception {

    private String fileUUID;

    public InvalidFileFormatException(String message, String fileUUID) {
        super(message);
        this.fileUUID = fileUUID;
    }

}
