package com.hysteryale.model.json;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageJSON {
    private Map<String, String> success;
    private Map<String, String> failure;
    private Map<String, String> warning;
}
