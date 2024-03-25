package com.hysteryale.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvalidFileFormatException extends Exception{
    private String savedFileName;
    private String fileExtension;
    public InvalidFileFormatException(String name, String savedFileName, String fileExtension) {
        super(name);
        this.savedFileName = savedFileName;
        this.fileExtension = fileExtension;
    }
}
