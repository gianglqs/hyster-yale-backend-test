/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.utils;

import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

import static com.hysteryale.utils.ConvertDataExcelUtils.convertDataFromExcelToString;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class ConvertDataExcelUtilsTest {

    @Test
    public void  checkConvertDataFromExcelToString(){
        Cell cell1 = new MockCell(CellType.STRING, "abc");
        Cell cell2 = new MockCell(CellType.STRING, "");
        Cell cell3 = new MockCell(CellType.NUMERIC, 32434.545);
        Cell cell4=new MockCell(CellType.STRING," ");
        assertEquals(convertDataFromExcelToString(cell1),"abc");
        assertEquals(convertDataFromExcelToString(cell2),"");
        assertEquals(convertDataFromExcelToString(cell3),"32434.545");
        assertEquals(convertDataFromExcelToString(cell4)," ");
    }
}

 class MockCell implements Cell {
    private CellType type;
    private Object value;

    public MockCell(CellType type, Object value) {
        this.type = type;
        this.value = value;
    }


    public CellType getCellTypeEnum() {
        return type;
    }

     @Override
     public int getColumnIndex() {
         return 0;
     }

     @Override
     public int getRowIndex() {
         return 0;
     }

     @Override
     public Sheet getSheet() {
         return null;
     }

     @Override
     public Row getRow() {
         return null;
     }

     @Override
     public void setCellType(CellType cellType) {

     }

     @Override
     public void setBlank() {

     }

     @Override
    public CellType getCellType() {
        return type;
    }

     @Override
     public CellType getCachedFormulaResultType() {
         return null;
     }

     @Override
     public void setCellValue(double v) {

     }

     @Override
     public void setCellValue(Date date) {

     }

     @Override
     public void setCellValue(LocalDateTime localDateTime) {

     }

     @Override
     public void setCellValue(Calendar calendar) {

     }

     @Override
     public void setCellValue(RichTextString richTextString) {

     }

     @Override
     public void setCellValue(String s) {

     }

     @Override
     public void setCellFormula(String s) throws FormulaParseException, IllegalStateException {

     }

     @Override
     public void removeFormula() throws IllegalStateException {

     }

     @Override
     public String getCellFormula() {
         return null;
     }

     @Override
    public String getStringCellValue() {
        return value.toString();
    }

     @Override
     public void setCellValue(boolean b) {

     }

     @Override
     public void setCellErrorValue(byte b) {

     }

     @Override
     public boolean getBooleanCellValue() {
         return false;
     }

     @Override
     public byte getErrorCellValue() {
         return 0;
     }

     @Override
     public void setCellStyle(CellStyle cellStyle) {

     }

     @Override
     public CellStyle getCellStyle() {
         return null;
     }

     @Override
     public void setAsActiveCell() {

     }

     @Override
     public CellAddress getAddress() {
         return null;
     }

     @Override
     public void setCellComment(Comment comment) {

     }

     @Override
     public Comment getCellComment() {
         return null;
     }

     @Override
     public void removeCellComment() {

     }

     @Override
     public Hyperlink getHyperlink() {
         return null;
     }

     @Override
     public void setHyperlink(Hyperlink hyperlink) {

     }

     @Override
     public void removeHyperlink() {

     }

     @Override
     public CellRangeAddress getArrayFormulaRange() {
         return null;
     }

     @Override
     public boolean isPartOfArrayFormulaGroup() {
         return false;
     }

     @Override
    public double getNumericCellValue() {
        return (double) value;
    }

     @Override
     public Date getDateCellValue() {
         return null;
     }

     @Override
     public LocalDateTime getLocalDateTimeCellValue() {
         return null;
     }

     @Override
     public RichTextString getRichStringCellValue() {
         return null;
     }

     // Other methods can be implemented based on your needs
}


