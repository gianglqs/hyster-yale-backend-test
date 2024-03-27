package com.hysteryale.service;

import com.hysteryale.exception.MissingColumnException;
import com.hysteryale.model.*;
import com.hysteryale.repository.DealerListingRepository;
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
    DealerListingRepository dealerListingRepository;

    public void importNewDealerListingFileByFile(String filePath) throws IOException, MissingColumnException {
        InputStream is = new FileInputStream(filePath);
        XSSFWorkbook workbook = new XSSFWorkbook(is);
        List<DealerListing> dealerListingList = new LinkedList<>();
        Sheet sheet = workbook.getSheetAt(0);

        HashMap<String, Integer> DEALER_LISTING_NAME = new HashMap<>();

        for (Row row : sheet) {
            if (row.getRowNum() == 0) {
                getDealerListingColumnName(row, DEALER_LISTING_NAME);
                CheckRequiredColumnUtils.checkRequiredColumn(new ArrayList<>(DEALER_LISTING_NAME.keySet()), CheckRequiredColumnUtils.DEALEAR_LISTING_REQUIRED_COLUMN);
            } else if (!row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().isEmpty() && row.getRowNum() > 0) {
                DealerListing newDealerListing = mapExcelDataIntoDealerListingObject(row, DEALER_LISTING_NAME);

                if (newDealerListing == null)
                    continue;

                dealerListingList.add(newDealerListing);

            }
        }
        dealerListingRepository.saveAll(dealerListingList);

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
    DealerListing mapExcelDataIntoDealerListingObject(Row row, HashMap<String, Integer> ORDER_COLUMNS_NAME) throws MissingColumnException {
        DealerListing dealerListing = new DealerListing();

        // set billToCost
        String billtoCode = convertDataFromExcelToString(row.getCell(ORDER_COLUMNS_NAME.get("BilltoCode")));
        dealerListing.setBilltoCode(billtoCode);

        String mkgGroup = convertDataFromExcelToString(row.getCell(ORDER_COLUMNS_NAME.get("MkgGroup")));
        dealerListing.setBilltoCode(mkgGroup);

        // Series
        String dealerDivison= convertDataFromExcelToString(row.getCell(ORDER_COLUMNS_NAME.get("DealerDivison"))) ;
        dealerListing.setDealerDivison(dealerDivison);

        String dealerName = convertDataFromExcelToString(row.getCell(ORDER_COLUMNS_NAME.get("DealerName"))) ;
        dealerListing.setDealerName(dealerName);

        String teritoryManager=convertDataFromExcelToString(row.getCell(ORDER_COLUMNS_NAME.get("TerritoryManager"))) ;
        dealerListing.setTerritoryManager(teritoryManager);

        String areaBusinesssDirector=convertDataFromExcelToString(row.getCell(ORDER_COLUMNS_NAME.get("AreaBusinesssDirector")));
        dealerListing.setAreaBusinesssDirector(areaBusinesssDirector);

        String bigTruckManager=convertDataFromExcelToString(row.getCell(ORDER_COLUMNS_NAME.get("BigTruckManager")));
        dealerListing.setBigTruckManager(bigTruckManager);

        String aftermarketManager=convertDataFromExcelToString(row.getCell(ORDER_COLUMNS_NAME.get("AftermarketManager")));
        dealerListing.setAftermarketManager(aftermarketManager);

        String aftermarketTechnicalServiceManager=convertDataFromExcelToString(row.getCell(ORDER_COLUMNS_NAME.get("AftermarketTechnicalServiceManager")));
        dealerListing.setAftermarketTechnicalServiceManager(aftermarketTechnicalServiceManager);

        return dealerListing;
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
