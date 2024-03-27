package com.hysteryale.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MissingColumnException extends Exception{

    private String fileUUID;

    public MissingColumnException(String message, String fileUUID){
        super(message);
        this.fileUUID = fileUUID;
    }
}
