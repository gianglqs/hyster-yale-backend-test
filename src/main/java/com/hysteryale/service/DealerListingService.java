package com.hysteryale.service;

import com.hysteryale.exception.MissingColumnException;
import com.hysteryale.model.*;
import com.hysteryale.repository.DealerRepository;
import com.hysteryale.utils.CheckRequiredColumnUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
@Slf4j
public class DealerListingService {
    @Resource
    DealerRepository dealerRepository;

    public void importNewDealerFileByFile(String filePath) throws IOException, MissingColumnException {
        InputStream is = new FileInputStream(filePath);
        XSSFWorkbook workbook = new XSSFWorkbook(is);
        List<Dealer> dealerList = new LinkedList<>();
        Sheet sheet = workbook.getSheetAt(0);

        HashMap<String, Integer> DEALER_NAME = new HashMap<>();

        for (Row row : sheet) {
            if (row.getRowNum() == 0) {
                getDealerListingColumnName(row, DEALER_NAME);
                CheckRequiredColumnUtils.checkRequiredColumn(new ArrayList<>(DEALER_NAME.keySet()), CheckRequiredColumnUtils.DEALEAR_REQUIRED_COLUMN);
            } else if (!row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().isEmpty() && row.getRowNum() > 0) {
                Dealer newDealer = mapExcelDataIntoDealerListingObject(row, DEALER_NAME);

                if (newDealer == null)
                    continue;

                dealerList.add(newDealer);

            }
        }
        dealerRepository.saveAll(dealerList);

    }

    public String convertDataFromExcelToString(Cell cell){
        String result="";
        if(cell==null)
            result="";
        if (cell.getCellType() == CellType.STRING)
            result=cell.getStringCellValue();
        else
            result=String.valueOf(cell.getNumericCellValue());

        return result;
    }
    Dealer mapExcelDataIntoDealerListingObject(Row row, HashMap<String, Integer> ORDER_COLUMNS_NAME) throws MissingColumnException {
        Dealer dealer = new Dealer();

        // set billToCost
//        String billtoCode = convertDataFromExcelToString(row.getCell(ORDER_COLUMNS_NAME.get("BilltoCode")));
//        dealer.setBilltoCode(billtoCode);
//
//        String mkgGroup = convertDataFromExcelToString(row.getCell(ORDER_COLUMNS_NAME.get("MkgGroup")));
//        dealer.setBilltoCode(mkgGroup);
//
//        // Series
//        String dealerDivison= convertDataFromExcelToString(row.getCell(ORDER_COLUMNS_NAME.get("DealerDivison"))) ;
//        dealer.setDealerDivison(dealerDivison);
//
//        String dealerName = convertDataFromExcelToString(row.getCell(ORDER_COLUMNS_NAME.get("DealerName"))) ;
//        dealer.setDealerName(dealerName);
//
//        String teritoryManager=convertDataFromExcelToString(row.getCell(ORDER_COLUMNS_NAME.get("TerritoryManager"))) ;
//        dealer.setTerritoryManager(teritoryManager);
//
//        String areaBusinesssDirector=convertDataFromExcelToString(row.getCell(ORDER_COLUMNS_NAME.get("AreaBusinesssDirector")));
//        dealer.setAreaBusinesssDirector(areaBusinesssDirector);
//
//        String bigTruckManager=convertDataFromExcelToString(row.getCell(ORDER_COLUMNS_NAME.get("BigTruckManager")));
//        dealer.setBigTruckManager(bigTruckManager);
//
//        String aftermarketManager=convertDataFromExcelToString(row.getCell(ORDER_COLUMNS_NAME.get("AftermarketManager")));
//        dealer.setAftermarketManager(aftermarketManager);
//
//        String aftermarketTechnicalServiceManager=convertDataFromExcelToString(row.getCell(ORDER_COLUMNS_NAME.get("AftermarketTechnicalServiceManager")));
//        dealer.setAftermarketTechnicalServiceManager(aftermarketTechnicalServiceManager);

        return dealer;
    }

    public void getDealerListingColumnName(Row row,HashMap<String, Integer> DEALER_LISTING_COLUMNS_NAME) {
        for (int i = 0; i < 50; i++) {
            if (row.getCell(i) != null) {
                String columnName = row.getCell(i).getStringCellValue().trim();
                if (DEALER_LISTING_COLUMNS_NAME.containsKey(columnName)) continue;
                DEALER_LISTING_COLUMNS_NAME.put(columnName, i);
            }
        }
    }

}
