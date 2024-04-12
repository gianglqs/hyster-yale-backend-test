/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.service;

import com.hysteryale.exception.IncorectFormatCellException;
import com.hysteryale.exception.MissingColumnException;
import com.hysteryale.model.Product;
import com.hysteryale.model.dealer.Dealer;
import com.hysteryale.model.dealer.DealerProduct;
import com.hysteryale.model.dealer.DealerProductId;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.model.payLoad.DealerPayload;
import com.hysteryale.repository.DealerProductRepository;
import com.hysteryale.repository.DealerRepository;
import com.hysteryale.utils.CheckRequiredColumnUtils;
import com.hysteryale.utils.ConvertDataExcelUtils;
import com.hysteryale.utils.ConvertDataFilterUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.*;

@Service
@Slf4j
@SuppressWarnings("unchecked")
public class DealerService {
    @Resource
    DealerRepository dealerRepository;
    @Resource
    ProductService productService;
    @Resource
    DealerProductRepository dealerProductRepository;

    public Dealer getDealerByName(List<Dealer> dealers, String name) {
        for (Dealer dealer : dealers) {
            if (keepLettersAndDigits(dealer.getName()).contains(keepLettersAndDigits(name)))
                return dealer;
        }
        return null;
    }

    private String keepLettersAndDigits(String input) {
        return input.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }

    public Page<Dealer> getDealerListing(DealerPayload payload, int pageNo) {
        Pageable pageable = PageRequest.of(pageNo - 1, 16);
        return dealerRepository.getDealerListingByFilter(payload.getDealerName(), pageable);
    }

    public Dealer getDealerById(int dealerId) {
        Optional<Dealer> optionalDealer = dealerRepository.findById(dealerId);
        return optionalDealer.orElse(null);
    }
    //import and save dealer to dealer listing
    public void importNewDealerFileByFile(String filePath) throws IOException, MissingColumnException {
        InputStream is = new FileInputStream(filePath);
        XSSFWorkbook workbook = new XSSFWorkbook(is);
        List<Dealer> dealerList = new LinkedList<>();
        Sheet sheet = workbook.getSheetAt(0);

        HashMap<String, Integer> DEALER_NAME = new HashMap<>();

        for (Row row : sheet) {
            if (row.getRowNum() == 0) {
                getDealerColumnName(row, DEALER_NAME);
                CheckRequiredColumnUtils.checkRequiredColumn(new ArrayList<>(DEALER_NAME.keySet()), CheckRequiredColumnUtils.DEALEAR_REQUIRED_COLUMN,"");
            } else if (!row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().isEmpty()) {
                Dealer newDealer = mapExcelDataIntoDealerListingObject(row, DEALER_NAME);
                dealerList.add(newDealer);
            }
        }
        dealerRepository.saveAll(dealerList);
    }

    // map excel data put into list dealer
    Dealer mapExcelDataIntoDealerListingObject(Row row, HashMap<String, Integer> ORDER_COLUMNS_NAME)  {
        Dealer dealer = new Dealer();
        // set billToCost
        String billtoCode = ConvertDataExcelUtils.convertDataFromExcelToString(row.getCell(ORDER_COLUMNS_NAME.get("BilltoCode")));
        dealer.setBilltoCode(billtoCode);

        String mkgGroup = ConvertDataExcelUtils.convertDataFromExcelToString(row.getCell(ORDER_COLUMNS_NAME.get("MkgGroup")));
        dealer.setBilltoCode(mkgGroup);

        // Series
        String dealerDivison= ConvertDataExcelUtils.convertDataFromExcelToString(row.getCell(ORDER_COLUMNS_NAME.get("DealerDivison"))) ;
        dealer.setDealerDivison(dealerDivison);

        String dealerName = ConvertDataExcelUtils.convertDataFromExcelToString(row.getCell(ORDER_COLUMNS_NAME.get("DealerName"))) ;
        dealer.setName(dealerName);

        String teritoryManager=ConvertDataExcelUtils.convertDataFromExcelToString(row.getCell(ORDER_COLUMNS_NAME.get("TerritoryManager"))) ;
        dealer.setTerritoryManager(teritoryManager);

        String areaBusinesssDirector=ConvertDataExcelUtils.convertDataFromExcelToString(row.getCell(ORDER_COLUMNS_NAME.get("AreaBusinesssDirector")));
        dealer.setAreaBusinesssDirector(areaBusinesssDirector);

        String bigTruckManager=ConvertDataExcelUtils.convertDataFromExcelToString(row.getCell(ORDER_COLUMNS_NAME.get("BigTruckManager")));
        dealer.setBigTruckManager(bigTruckManager);

        String aftermarketManager=ConvertDataExcelUtils.convertDataFromExcelToString(row.getCell(ORDER_COLUMNS_NAME.get("AftermarketManager")));
        dealer.setAftermarketManager(aftermarketManager);

        String aftermarketTechnicalServiceManager=ConvertDataExcelUtils.convertDataFromExcelToString(row.getCell(ORDER_COLUMNS_NAME.get("AftermarketTechnicalServiceManager")));
        dealer.setAftermarketTechnicalServiceManager(aftermarketTechnicalServiceManager);

        return dealer;
    }

    public void getDealerColumnName(Row row, HashMap<String, Integer> DEALER_COLUMNS_NAME) {
        for (int i = 0; i < 50; i++) {
            if (row.getCell(i) != null) {
                String columnName = row.getCell(i).getStringCellValue().trim();
                if (DEALER_COLUMNS_NAME.containsKey(columnName)) continue;
                DEALER_COLUMNS_NAME.put(columnName, i);
            }
        }
    }

    public Dealer getDealerByDealerName(String dealerName) {
        Optional<Dealer> optional = dealerRepository.getDealerByDealerName(dealerName);
        return optional.orElse(null);
    }

    /**
     * Load all Dealer information for assigning Dealer field in saving DealerProduct
     */
    public List<Dealer> getAllDealers() {
        return dealerRepository.getAllDealers();
    }

    private Dealer getDealerFromList(List<Dealer> dealerList, String dealerName) {
        dealerName = dealerName.toLowerCase();
        for(Dealer dealer : dealerList) {
            if(dealer.getName().toLowerCase().contains(dealerName))
                return dealer;
        }
        return null;
    }

    private Product getProductFromList(List<Product> productList, String modelCode, String series) {
        for(Product product : productList) {
            if(product.equals(modelCode, series))
                return product;
        }
        return null;
    }

    /**
     * Import list of Products' information which a dealer sells on the market.
     */
    public void importDealerProductFromFile (String fileUUID, String filePath) throws IOException, IncorectFormatCellException {
        InputStream is = new FileInputStream(filePath);

        IOUtils.setByteArrayMaxOverride(300000000);
        Workbook workbook = new XSSFWorkbook(is);
        Sheet sheet = workbook.getSheet("TOP 20 Database");

        List<Dealer> dealerList = getAllDealers();
        List<Product> productList = productService.getAllProducts();

        HashMap<String, Integer> columns = new HashMap<>();
        List<DealerProduct> dealerProductList = new ArrayList<>();
        for(Row row : sheet) {
            if(row.getRowNum() == 0) {
                for(Cell cell : row) {
                    columns.put(cell.getStringCellValue(), cell.getColumnIndex());
                }
            }
            else if(!isRowNoData(row, columns)) {
                checkImportedCellFormat(row, columns, fileUUID);

                String createdBy = row.getCell(columns.get("Created By")).getStringCellValue();
                String serialNumber = row.getCell(columns.get("Serial Number")).getStringCellValue();

                String modelCode = row.getCell(columns.get("Model")).getStringCellValue();
                String series = row.getCell(columns.get("Series from SAP")).getStringCellValue();
                Product product = getProductFromList(productList, modelCode, series);

                String dealerName = row.getCell(columns.get("End Customer Name")).getStringCellValue();
                if(dealerName.equals("AAL")) dealerName = "ADAPT-A-LIFT";
                dealerName = dealerName.replace(" AND ", " & ");
                Dealer dealer = getDealerFromList(dealerList, dealerName);

                int quantity = (int) row.getCell(columns.get("Quantity")).getNumericCellValue();
                double netRevenue = row.getCell(columns.get("Net Revenue")).getNumericCellValue();

                DealerProduct dealerProduct = new DealerProduct(
                        new DealerProductId(createdBy, serialNumber),
                        product, dealer, quantity, netRevenue
                );
                dealerProductList.add(dealerProduct);
                if(dealerProductList.size() > 10000) {
                    dealerProductRepository.saveAll(dealerProductList);
                    dealerProductList.clear();
                }
            }
        }
        dealerProductRepository.saveAll(dealerProductList);
        dealerProductList.clear();
    }

    /**
     * Check the valid cell's types respectively to the required field
     */
    private void checkImportedCellFormat(Row row, HashMap<String, Integer> columns, String fileUUID) throws IncorectFormatCellException {
        Cell modelCodeCell = row.getCell(columns.get("Model"));
        if(modelCodeCell.getCellType() != CellType.STRING)
            throw new IncorectFormatCellException(modelCodeCell.getRowIndex() + ":" + modelCodeCell.getColumnIndex(), fileUUID);

        Cell dealerNameCell = row.getCell(columns.get("End Customer Name"));
        if(dealerNameCell.getCellType() != CellType.STRING)
            throw new IncorectFormatCellException(dealerNameCell.getRowIndex() + ":" + dealerNameCell.getColumnIndex(), fileUUID);

        Cell quantityCell = row.getCell(columns.get("Quantity"));
        if(quantityCell.getCellType() != CellType.NUMERIC)
            throw new IncorectFormatCellException(quantityCell.getRowIndex() + ":" + quantityCell.getColumnIndex(), fileUUID);

        Cell netRevenueCell = row.getCell(columns.get("Net Revenue"));
        if(netRevenueCell.getCellType() != CellType.FORMULA && netRevenueCell.getCellType() != CellType.NUMERIC)
            throw new IncorectFormatCellException(netRevenueCell.getRowIndex() + ":" + netRevenueCell.getColumnIndex(), fileUUID);
    }

    /**
     * Function for checking if the 'Created By' or 'Serial Number' fields contain no data.
     * @return true if either one of both containing no data
     */
    private boolean isRowNoData(Row row, HashMap<String, Integer> columns) {
        return row.getCell(columns.get("Created By"), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().isEmpty() ||
                row.getCell(columns.get("Serial Number"), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().isEmpty();
    }

    public Map<String, Object> getDealerProductByFilters(FilterModel filters) throws ParseException {
        Pageable pageable = PageRequest.of(filters.getPageNo() - 1, filters.getPerPage());
        Map<String, Object> filterMap = ConvertDataFilterUtil.loadDataFilterIntoMap(filters);
        Page<DealerProduct> result = dealerProductRepository.getDealerProductByFilters(
                (String) filterMap.get("modelCodeFilter"), (List<String>) filterMap.get("classFilter"),
                (List<String>) filterMap.get("segmentFilter"), (List<String>) filterMap.get("familyFilter"),
                (List<String>) filterMap.get("metaSeriesFilter"), (Integer) filterMap.get("dealerId"), pageable
        );

        return Map.of(
                "pageNo", result.getNumber(),
                "totalItems", result.getTotalElements(),
                "listData", result.getContent()
        );
    }
}
