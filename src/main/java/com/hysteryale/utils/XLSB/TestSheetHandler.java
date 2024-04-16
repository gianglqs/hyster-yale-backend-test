/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.utils.XLSB;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.springframework.security.core.parameters.P;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class TestSheetHandler implements XSSFSheetXMLHandler.SheetContentsHandler {

    @Getter
    private final Sheet sheet  = new Sheet();
    private List<Row> rowList;
    private List<Cell> cellList;
    private int index;

    public void startSheet() {
        rowList = new ArrayList<>();
    }
    public void endSheet(String sheetName) {
        sheet.setSheetName(sheetName);
        sheet.setRowList(rowList);
    }

    @Override
    public void startRow(int rowNum) {
        cellList = new ArrayList<>();
    }

    @Override
    public void endRow(int rowNum) {
        if(!cellList.isEmpty()) {
            Row row = new Row(rowNum, cellList);
            rowList.add(row);
        }
    }

    @Override
    public void cell(String cell, String formattedValue, XSSFComment comment) {
        String cellColumn = cell.replaceAll("\\d", "");

        formattedValue = (formattedValue.isEmpty()) ? "N/A" : formattedValue;
        Cell xlsbCell = new Cell(cellColumn, formattedValue);
        cellList.add(xlsbCell);
    }

    @Override
    public void headerFooter(String text, boolean isHeader, String tagName) {

    }
}
