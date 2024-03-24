package com.hysteryale.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvalidFileNameException extends Exception{

    private String savedFileName;

    public InvalidFileNameException(String message, String savedFileName){
        super(message);
        this.savedFileName = savedFileName;
    }
}
