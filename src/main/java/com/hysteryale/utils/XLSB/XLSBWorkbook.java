/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.utils.XLSB;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.binary.XSSFBSharedStringsTable;
import org.apache.poi.xssf.binary.XSSFBSheetHandler;
import org.apache.poi.xssf.binary.XSSFBStylesTable;
import org.apache.poi.xssf.eventusermodel.XSSFBReader;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class XLSBWorkbook {
    XSSFBReader r;
    XSSFBSharedStringsTable sst;
    XSSFBStylesTable xssfbStylesTable;
    XSSFBReader.SheetIterator it;

    public void openFile(String xlsbFilePath) throws OpenXML4JException, IOException, SAXException {
        OPCPackage pkg = OPCPackage.open(xlsbFilePath, PackageAccess.READ_WRITE);

        r = new XSSFBReader(pkg);
        sst = new XSSFBSharedStringsTable(pkg);
        xssfbStylesTable = r.getXSSFBStylesTable();
        it = (XSSFBReader.SheetIterator) r.getSheetsData();
    }

    public Sheet getAOPFSheet() throws IOException {
        TestSheetHandler testSheetHandler = new TestSheetHandler();
        while (it.hasNext()) {
            InputStream is = it.next();
            if(it.getSheetName().contains("AOPF"))
            {
                log.info("Found sheet: " + it.getSheetName());
                testSheetHandler.startSheet();
                XSSFBSheetHandler sheetHandler = new XSSFBSheetHandler(
                        is,
                        xssfbStylesTable,
                        it.getXSSFBSheetComments(),
                        sst,
                        testSheetHandler,
                        new DataFormatter(),
                        false
                );
                sheetHandler.parse();
                testSheetHandler.endSheet(it.getSheetName());
            }
        }
        return testSheetHandler.getSheet();
    }

    public Sheet getSheet(String sheetName) throws IOException {
        TestSheetHandler testSheetHandler = new TestSheetHandler();
        while (it.hasNext()) {
            InputStream is = it.next();
            if(it.getSheetName().equals(sheetName))
            {
                log.info("Found sheet: " + sheetName);
                testSheetHandler.startSheet();
                XSSFBSheetHandler sheetHandler = new XSSFBSheetHandler(
                        is,
                        xssfbStylesTable,
                        it.getXSSFBSheetComments(),
                        sst,
                        testSheetHandler,
                        new DataFormatter(),
                        false
                );
                sheetHandler.parse();
                testSheetHandler.endSheet(sheetName);
            }
        }
        return testSheetHandler.getSheet();
    }
}
