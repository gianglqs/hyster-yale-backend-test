package com.hysteryale.service;

import com.hysteryale.model.*;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.model.filters.InterestRateFilterModel;
import com.hysteryale.repository.InterestRateRepository;
import com.hysteryale.repository.RegionRepository;
import com.hysteryale.utils.CheckRequiredColumnUtils;
import com.hysteryale.utils.ConvertDataExcelUtils;
import com.hysteryale.utils.ConvertDataFilterUtil;
import com.hysteryale.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@SuppressWarnings("unchecked")
public class InterestRateService {

    @Autowired
    InterestRateRepository interestRateRepository;

    @Autowired
    RegionRepository regionRepository;

    public List<InterestRate> getAllInterestRate() {
        return interestRateRepository.findAll();
    }

    public Map<String, Object> getListInterestRateByFilter(InterestRateFilterModel filter) throws ParseException {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> filterMap = ConvertDataFilterUtil.loadInterestRateDataFilterIntoMap(filter);
        List<Object[]> getData;
        if (filter.getRegions() == null || filter.getRegions().isEmpty()) {
            getData=interestRateRepository.selectAllForInterestRate((String) filterMap.get("bankNameFilter"));
        } else {
            getData = interestRateRepository.selectAllForInterestRateByFilter((String) filterMap.get("bankNameFilter"), (List<String>) filterMap.get("regionFilter"));
        }

        //object =Map<String,Object>
        Map<String, List<Object>> resultList=new HashMap<>();
        List<Object> list=new ArrayList<>();
        for(Object[] data:getData) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", data[0]);
            map.put("bankName",data[1]);
            map.put("country",data[2]);
            map.put("currentRate",data[3]);
            map.put("previousRate",data[4]);
            map.put("updateDate",data[5]);
            map.put("regionId",data[6]);
            map.put("regionName",data[7]);
            map.put("code",data[8]);
            list.add(map);
        }
        result.put("listInterestRate",list);
        return result;
    }



    // import data from file excel world bank to databse
    public void importInterestRateFromFile(String filePath) throws Exception {
        InputStream is = new FileInputStream(filePath);
        HSSFWorkbook workbook = new HSSFWorkbook(is);
        HashMap<String, Integer> interestRateColumnName = new HashMap<>();

        Sheet sheet = workbook.getSheetAt(0);
        List<InterestRate> listInterestRate=new ArrayList<>();
//        Date lastUpdatedDate=null;

        Row dateUpdateRow = sheet.getRow(1);
        Date lastUpdatedDate = null;
        if (dateUpdateRow != null) {
            Cell dateCell = dateUpdateRow.getCell(1);
            if (dateCell != null && dateCell.getCellType() == CellType.NUMERIC) {
                lastUpdatedDate =  dateCell.getDateCellValue();
            }else{
                throw new Exception("This is not date type");
            }
        }
        for (Row row : sheet) {
            if (row.getRowNum() == 3) {
                interestRateColumnName = getInterestRateColumnName(row);
            } else if (row.getRowNum() > 3&& !ConvertDataExcelUtils.convertDataFromExcelToString(row.getCell(interestRateColumnName.get("Country Name"))).isEmpty()) {
                listInterestRate.addAll(mapExcelDataIntoInterestRateObject(row, interestRateColumnName, lastUpdatedDate));
            }
        }
        interestRateRepository.saveAll(listInterestRate);
    }

    public HashMap<String, Integer> getInterestRateColumnName(Row row) {
        HashMap<String, Integer> competitorColumnName = new HashMap<>();
        for (Cell cell : row)
            competitorColumnName.put(cell.getStringCellValue(), cell.getColumnIndex());
        return competitorColumnName;
    }

    public List<InterestRate> mapExcelDataIntoInterestRateObject(Row row, HashMap<String, Integer> ORDER_COLUMNS_NAME, Date lastUpdatedDate) {
        List<InterestRate> interestRateList = new ArrayList<>();
        String countryName =row.getCell(ORDER_COLUMNS_NAME.get("Country Name"),Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();

        int lastCellNum = row.getLastCellNum();
        double interestRateBefore = ConvertDataExcelUtils.convertDataFromExcelToDouble(row.getCell(lastCellNum - 2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK));
        double interestRateCurrent = ConvertDataExcelUtils.convertDataFromExcelToDouble(row.getCell(lastCellNum - 1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK));

        Optional<InterestRate> existingInterestRate = interestRateRepository.getInterestRateByCountry(countryName);
        if(CheckRequiredColumnUtils.INTEREST_RATE_COUNTRY_ROW.contains(countryName)){
            if(existingInterestRate.isPresent()) {
                // assigning values for InterestRate
                if(countryName.equals("Hong Kong SAR, China")){
                    countryName="Hong Kong";
                }
                if(countryName.equals("Korea, Rep.")){
                    countryName="South Korea";
                }
                if(countryName.equals("Viet Nam")){
                    countryName="Vietnam";
                }
                InterestRate ir = existingInterestRate.get();
                ir.setBankName(countryName+" Central Bank");
                ir.setCountry(countryName);
//            ir.setCurrentRate(((interestRate2022-interestRate2021)/2000)*100);
//            ir.setPreviousRate(((interestRate2021-interestRate2020)/1000)*100);
                ir.setCurrentRate(interestRateCurrent);
                ir.setPreviousRate(interestRateBefore);

                ir.setUpdateDate(lastUpdatedDate);
                interestRateList.add(ir);
            }else{
                // assigning values for InterestRate
                if(countryName.equals("Hong Kong SAR, China")){
                    countryName="Hong Kong";
                }
                if(countryName.equals("Korea, Rep.")){
                    countryName="South Korea";
                }
                if(countryName.equals("Viet Nam")){
                    countryName="Vietnam";
                }
                InterestRate ir = new InterestRate();
                ir.setBankName(countryName+" Central Bank");
                ir.setCountry(countryName);
//            ir.setCurrentRate(((interestRate2022-interestRate2021)/2000)*100);
//            ir.setPreviousRate(((interestRate2021-interestRate2020)/1000)*100);
                ir.setCurrentRate(interestRateCurrent);
                ir.setPreviousRate(interestRateBefore);
                ir.setUpdateDate(lastUpdatedDate);
                interestRateList.add(ir);
            }
        }
        return interestRateList;
    }


    private static List<String> checkListData(List<String> data) {
        return data == null || data.isEmpty() ? null : data;
    }

    private static String checkStringData(String data) {
        return data == null || data.isEmpty() ? null : data;
    }


}
