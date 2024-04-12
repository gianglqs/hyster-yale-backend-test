/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.service;

import com.hysteryale.exception.IncorectFormatCellException;
import com.hysteryale.model.dealer.Dealer;
import com.hysteryale.model.dealer.DealerProduct;
import com.hysteryale.model.dealer.DealerProductId;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.model.payLoad.DealerPayload;
import com.hysteryale.repository.DealerProductRepository;
import com.hysteryale.repository.DealerRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.*;

@SpringBootTest
@Slf4j
@SuppressWarnings("unchecked")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DealerServiceTest {
    @Resource
    DealerService dealerService;
    @Resource
    DealerRepository dealerRepository;
    @Resource
    DealerProductRepository dealerProductRepository;
    @Test
    public void testGetDealerListing() {
        DealerPayload payload = new DealerPayload();
        int pageNo = 0;
        int perPage = 16;

        // Expected
        Pageable pageable = PageRequest.of(pageNo, 16);
        Page<Dealer> expected = dealerRepository.getDealerListingByFilter(payload.getDealerName(), pageable);

        // Actual
        Page<Dealer> result = dealerService.getDealerListing(payload, pageNo + 1);

        // Assertions
        Assertions.assertEquals(pageNo, result.getNumber());
        Assertions.assertEquals(perPage, result.getSize());
        Assertions.assertEquals(expected.getContent().size(), result.getContent().size());
    }

    @Test
    public void testGetDealerById() {
        int dealerId = 1;
        Dealer result = dealerService.getDealerById(dealerId);
        Assertions.assertNotNull(result);

        int dealerId_notFound = 9999;
        Dealer result_notFound = dealerService.getDealerById(dealerId_notFound);
        Assertions.assertNull(result_notFound);
    }

    @Test
    public void testGetDealerProduct_byClass() throws ParseException {
        String clazz = "Class 1";

        FilterModel filters = new FilterModel();
        filters.setClasses(List.of(clazz));
        filters.setPageNo(1);
        filters.setPerPage(100);

        Map<String, Object> result = dealerService.getDealerProductByFilters(filters);
        List<DealerProduct> dataList = (List<DealerProduct>) result.get("listData");
        for(DealerProduct dp : dataList) {
            Assertions.assertEquals(clazz, dp.getProduct().getClazz().getClazzName());
        }
    }

    @Test
    public void testGetDealerProduct_bySeries() throws ParseException {
        String series = "A099";

        FilterModel filters = new FilterModel();
        filters.setMetaSeries(List.of(series));
        filters.setPageNo(1);
        filters.setPerPage(100);

        Map<String, Object> result = dealerService.getDealerProductByFilters(filters);
        List<DealerProduct> dataList = (List<DealerProduct>) result.get("listData");
        for(DealerProduct dp : dataList) {
            Assertions.assertEquals(series, dp.getProduct().getSeries());
        }
    }

    @Test
    @Order(2)
    public void testGetDealerProductByFilters_byModelCode() throws ParseException {
        String modelCode = "E80XN";

        FilterModel filters = new FilterModel();
        filters.setModelCode(modelCode);
        filters.setPageNo(1);
        filters.setPerPage(100);

        Map<String, Object> result = dealerService.getDealerProductByFilters(filters);
        List<DealerProduct> dataList = (List<DealerProduct>) result.get("listData");
        for(DealerProduct dp : dataList) {
            Assertions.assertEquals(modelCode, dp.getProduct().getModelCode());
        }
    }

    @Test
    @Order(1)
    public void testImportDealerProducts() throws IncorectFormatCellException, IOException {
        String filePath = "import_files/dealer/10 years data of Top 20 API Dealer v5.xlsx";
        dealerService.importDealerProductFromFile("", filePath);

        InputStream is = new FileInputStream(filePath);
        Workbook workbook = new XSSFWorkbook(is);
        Sheet sheet = workbook.getSheet("Top 20 Database");

        HashMap<String, Integer> columns = new HashMap<>();
        for(Cell cell : sheet.getRow(0))
            columns.put(cell.getStringCellValue(), cell.getColumnIndex());

        Random random = new Random();
        for(int i = 0; i < 100; i++) {
            int rowIndex = random.nextInt(1000);
            Row row = sheet.getRow(rowIndex);
            while(row.getCell(columns.get("Serial Number")).getStringCellValue().isEmpty()) {
                rowIndex = random.nextInt(1000);
                row = sheet.getRow(rowIndex);
            }
            assertDealerProductValue(row, columns);
        }
    }

    private void assertDealerProductValue(Row row, HashMap<String, Integer> columns) {
        String createdBy = row.getCell(columns.get("Created By")).getStringCellValue();
        String serialNumber = row.getCell(columns.get("Serial Number")).getStringCellValue();
        String modelCode = row.getCell(columns.get("Model")).getStringCellValue();

        String dealerName = row.getCell(columns.get("End Customer Name")).getStringCellValue();
        if(dealerName.equals("AAL")) dealerName = "ADAPT-A-LIFT";
        dealerName = dealerName.replace(" AND ", " & ");

        Optional<DealerProduct> optional = dealerProductRepository.findById(new DealerProductId(createdBy, serialNumber));
        Assertions.assertFalse(optional.isEmpty());
        DealerProduct dealerProduct = optional.get();

        if(dealerProduct.getProduct() != null) Assertions.assertEquals(modelCode, dealerProduct.getProduct().getModelCode());
        if(dealerProduct.getDealer() != null)  {
            log.info(dealerProduct.getDealer().getName() + " - " + dealerName);
            Assertions.assertTrue(dealerProduct.getDealer().getName().toLowerCase().contains(dealerName.toLowerCase()));
        }
    }

    //@Test
    public void testGetDealerColumnName() throws IOException {
        String filePath = "";
        XSSFWorkbook workbook = new XSSFWorkbook(filePath);
        Sheet sheet = workbook.getSheetAt(0);
        HashMap<String, Integer> dealerColumnsName = new HashMap<>();
        for (Row row : sheet) {
            dealerService.getDealerColumnName(row, dealerColumnsName);
        }

        Assertions.assertTrue(dealerColumnsName.containsKey(""));
    }
}



