package com.hysteryale.utils;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.ArrayList;
import java.util.List;

public class CheckFormatFile {
    public static boolean checkFormatFileFollowTitleColumns(Sheet sheet, List<String> titleColumns,int rowTitle) {
//        Row headerRow = sheet.getRow(rowTitle);
//
//
//            for (int i = 0; i < titleColumns.size()+1; i++) {
//                String cell = headerRow.getCell(i).getStringCellValue();
//                if (!titleColumns.contains(cell)) {
//                    String missingColumn="";
//                    missingColumn+=cell+", ";
//                    throw new RuntimeException("File missing column "+missingColumn) ;
//                }
//        }
//        return true;

        Row headerRow = sheet.getRow(rowTitle);
        if (headerRow == null) {
            return false;
        }
        for (int i = 0; i < titleColumns.size()+1; i++) {
            if(headerRow.getCell(i)==null){
                throw new RuntimeException("File missing column "+titleColumns.get(i));
            }

            if (titleColumns.contains(headerRow.getCell(i).getStringCellValue())) {
                return true;
            }else{
                throw new RuntimeException("File missing column "+titleColumns.get(i));
            }
        }
        return false;
    }

}
