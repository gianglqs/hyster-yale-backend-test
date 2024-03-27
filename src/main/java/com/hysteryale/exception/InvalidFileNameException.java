package com.hysteryale.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvalidFileNameException extends Exception{

    private String fileUUID;

    public InvalidFileNameException(String message, String fileUUID){
        super(message);
        this.fileUUID = fileUUID;
    }
}
