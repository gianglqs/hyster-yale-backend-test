package com.hysteryale.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IncorectFormatCellException extends Exception {
    private String fileUUID;
    public IncorectFormatCellException(String name, String fileUUID) {
        super(name);
        this.fileUUID = fileUUID;
    }
}
