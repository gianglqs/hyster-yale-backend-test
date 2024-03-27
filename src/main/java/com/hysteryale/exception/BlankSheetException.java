package com.hysteryale.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BlankSheetException extends Exception {

    private String fileUUID;

    public BlankSheetException(String message, String fileUUID) {
        super(message);
        this.fileUUID = fileUUID;
    }

}
