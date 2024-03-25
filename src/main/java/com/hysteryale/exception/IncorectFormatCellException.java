package com.hysteryale.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IncorectFormatCellException extends Exception {
    private String savedFileName;
    public IncorectFormatCellException(String name, String savedFileName) {
        super(name);
        this.savedFileName = savedFileName;
    }
}
