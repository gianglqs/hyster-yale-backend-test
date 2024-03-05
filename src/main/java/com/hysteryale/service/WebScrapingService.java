package com.hysteryale.service;

import com.hysteryale.utils.EnvironmentUtils;
import com.microsoft.playwright.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class WebScrapingService {

    /**
     * Simple demo for scraping data with Playwright
     */
    public String scrapData(String url) {
        try (Playwright playwright = Playwright.create()) {
            try(Browser browser = playwright.chromium().launch()) {
                BrowserContext context = browser.newContext();
                Page page = context.newPage();
                page.navigate(url);

                boolean isStop = false;
                while(!isStop) {
                    try {
                        page.locator(".cps-block-content_btn-showmore").first().locator("a").first().click();
                    } catch (Exception e) {
                        log.error(e.getMessage());
                        isStop = true;
                    }
                }
                Locator productList = page.locator(".product-info");
                String fileLocation = writeToExcel(productList.all());
                context.close();
                return fileLocation;
            }
        }
    }

    private String writeToExcel(List<Locator> productList) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Product");

            // Create Headers and Set Column width for 2 columns
            Row header = sheet.createRow(0);
            sheet.setColumnWidth(0, 20000);
            sheet.setColumnWidth(1, 4000);

            // Create and set Headers style
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            XSSFFont font = (XSSFFont) workbook.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 16);
            font.setBold(true);
            headerStyle.setFont(font);

            // Create Headers and set Header's name
            Cell headerCell = header.createCell(0);
            headerCell.setCellValue("Product Name");
            headerCell.setCellStyle(headerStyle);

            headerCell = header.createCell(1);
            headerCell.setCellValue("Price");
            headerCell.setCellStyle(headerStyle);

            // Write value into Workbook
            int i = 1;
            for(Locator productItem : productList) {
                String productName = productItem.locator("h3").innerText();
                String productPrice = productItem.locator("p.product__price--show").innerText();
                log.info(productName + " --- " + productPrice);

                Row row = sheet.createRow(i);
                Cell cell = row.createCell(0);
                cell.setCellValue(productName);

                cell = row.createCell(1);
                cell.setCellValue(productPrice);
                i++;
            }

            // Export to Excel file
            String folderPath = EnvironmentUtils.getEnvironmentValue("BASE_FOLDER_UPLOAD");
            LocalDateTime localDate = LocalDateTime.now();
            String fileName = "scrap-data-"
                    + localDate.getDayOfMonth()
                    + localDate.getMonth()
                    + localDate.getYear()
                    + "-" + localDate.getHour()
                    + "-" + localDate.getMinute()
                    + "-" + localDate.getSecond();

            File outputFile = new File(folderPath + "/" + fileName + ".xlsx");
            String path = outputFile.getAbsolutePath();

            FileOutputStream outputStream = new FileOutputStream(path);
            workbook.write(outputStream);
            return path;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }
}
