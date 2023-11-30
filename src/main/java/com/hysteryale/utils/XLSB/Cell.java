package com.hysteryale.utils.XLSB;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cell {
    private String cellColumn;
    private String value;

    public double getNumericCellValue() {
        double number;
        String modifiedValue = value.replaceAll("[^.%\\d]", "");
        if(modifiedValue.isEmpty())
            return 0;
        if(value.contains("%")) {
            modifiedValue = modifiedValue.replace("%", "");
            number = Double.parseDouble(modifiedValue) / 100;
        }
        else {
            number = Double.parseDouble(modifiedValue);
        }
        return number;
    }
}
