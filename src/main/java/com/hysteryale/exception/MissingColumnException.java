package com.hysteryale.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MissingColumnException extends Exception{

    private String savedFileName;

    public MissingColumnException(String message, String savedFileName){
        super(message);
        this.savedFileName = savedFileName;
    }
}
