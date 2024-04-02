package com.hysteryale.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SeriesNotFoundException extends Exception{
    private String series;
    public SeriesNotFoundException(String name, String series) {
        super(name);
        this.series = series;
    }
}