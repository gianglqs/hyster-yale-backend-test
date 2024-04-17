/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

public class ConvertDataExcelUtils {
    public static  String convertDataFromExcelToString(Cell cell){
        String result="";
        if(cell==null)
            return result;
        if (cell.getCellType() == CellType.STRING)
            result=cell.getStringCellValue();
        else if(cell.getCellType() == CellType.NUMERIC)
            result=String.valueOf(cell.getNumericCellValue());

        return result;
    }

    public static  double convertDataFromExcelToDouble(Cell cell){
        double result=0;
        if(cell==null)
            return result;
        if(cell.getCellType()==CellType.NUMERIC){
            result=cell.getNumericCellValue();
        }

        return result;
    }
}
