package com.hysteryale.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MissingSheetException extends Exception {

    private String savedFileName;

    public MissingSheetException(String message, String savedFileName) {
        super(message);
        this.savedFileName = savedFileName;
    }
}
