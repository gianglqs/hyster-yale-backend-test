package com.hysteryale.exception.CompetitorException;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CompetitorColorNotFoundException extends Exception {
    private int id;

    public CompetitorColorNotFoundException(String message, int id) {
        super(message);
        this.id = id;
    }
}
