/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MissingSheetException extends Exception {

    private String fileUUID;

    public MissingSheetException(String message, String fileUUID) {
        super(message);
        this.fileUUID = fileUUID;
    }
}
