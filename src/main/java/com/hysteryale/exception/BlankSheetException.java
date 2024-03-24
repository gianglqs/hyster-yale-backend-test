package com.hysteryale.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BlankSheetException extends Exception {

    private String savedFileName;

    public BlankSheetException(String message, String savedFileName) {
        super(message);
        this.savedFileName = savedFileName;
    }

}
