/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvalidFileFormatException extends Exception {
    private String fileUUID;
    private String savedFile;
    private String fileExtension;

    public InvalidFileFormatException(String message, String fileUUID) {
        super(message);
        this.fileUUID = fileUUID;
    }

    public InvalidFileFormatException(String message, String savedFile, String fileExtension) {
        super(message);
        this.savedFile = savedFile;
        this.fileExtension = fileExtension;
    }

}
