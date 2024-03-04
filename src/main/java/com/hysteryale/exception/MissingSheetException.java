package com.hysteryale.exception;

public class MissingSheetException extends Exception{
    public MissingSheetException(String name){
        super(name);
    }
}
