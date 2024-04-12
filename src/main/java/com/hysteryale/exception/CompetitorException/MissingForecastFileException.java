/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.exception.CompetitorException;

import lombok.Getter;

@Getter
public class MissingForecastFileException extends Exception{
    public MissingForecastFileException (String message) {
        super(message);
    }
}
