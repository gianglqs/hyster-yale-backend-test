/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.utils.XLSB;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Sheet {
    private String sheetName;
    private List<Row> rowList;

    public Row getRow(int rowNum) {
        return rowList.get(rowNum);
    }
}
