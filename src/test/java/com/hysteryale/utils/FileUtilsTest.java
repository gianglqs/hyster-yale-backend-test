/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.utils;

import org.apache.poi.poifs.filesystem.FileMagic;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilsTest {

    @Test
    void isExcelFile() throws IOException {
        InputStream is_Real = getClass().getClassLoader().getResourceAsStream("excelFileToTest.xlsx");
        InputStream is_Fake = getClass().getClassLoader().getResourceAsStream("fakeExcelFile.xlsx");

        assertEquals(true, (FileMagic.valueOf(is_Real) == FileMagic.OLE2) || (FileMagic.valueOf(is_Real) == FileMagic.OOXML));
        assertEquals(false, (FileMagic.valueOf(is_Fake) == FileMagic.OLE2) || (FileMagic.valueOf(is_Fake) == FileMagic.OOXML));
    }
}