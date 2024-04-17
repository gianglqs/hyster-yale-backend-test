package com.hysteryale.service;

import com.hysteryale.exception.CompetitorException.MissingForecastFileException;
import com.hysteryale.model.Clazz;
import com.hysteryale.model.Country;
import com.hysteryale.model.InterestRate;
import com.hysteryale.model.competitor.CompetitorColor;
import com.hysteryale.model.competitor.CompetitorPricing;
import com.hysteryale.model.competitor.ForeCastValue;
import com.hysteryale.repository.InterestRateRepository;
import com.hysteryale.utils.CheckRequiredColumnUtils;
import com.hysteryale.utils.ConvertDataExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
@SuppressWarnings("unchecked")
public class InterestRateService {

    @Autowired
    InterestRateRepository interestRateRepository;


    public List<InterestRate> getAllInterestRate() {
        return interestRateRepository.findAll();
    }

    public List<InterestRate> getInterestRateByBankName(String bankName) {
        return interestRateRepository.getInterestRateByBankName(bankName);
    }

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

        double interestRate2020 =ConvertDataExcelUtils.convertDataFromExcelToDouble(row.getCell(ORDER_COLUMNS_NAME.get("2020")));
        double interestRate2021 =ConvertDataExcelUtils.convertDataFromExcelToDouble(row.getCell(ORDER_COLUMNS_NAME.get("2021")));
        double interestRate2022 =ConvertDataExcelUtils.convertDataFromExcelToDouble(row.getCell(ORDER_COLUMNS_NAME.get("2022")));

        Optional<InterestRate> existingInterestRate = interestRateRepository.getInterestRateByCountry(countryName);
        if(CheckRequiredColumnUtils.INTEREST_RATE_COUNTRY_ROW.contains(countryName)){
            if(existingInterestRate.isPresent()) {
                // assigning values for CompetitorPricing
                InterestRate ir = existingInterestRate.get();
                ir.setBankName(countryName+" Central Bank");
                ir.setCountry(countryName);
//            ir.setCurrentRate(((interestRate2022-interestRate2021)/2000)*100);
//            ir.setPreviousRate(((interestRate2021-interestRate2020)/1000)*100);
                ir.setCurrentRate(interestRate2022);
                ir.setPreviousRate(interestRate2021);

                ir.setUpdateDate(lastUpdatedDate);
                interestRateList.add(ir);
            }else{
                // assigning values for CompetitorPricing
                InterestRate ir = new InterestRate();
                ir.setBankName(countryName+" Central Bank");
                ir.setCountry(countryName);
//            ir.setCurrentRate(((interestRate2022-interestRate2021)/2000)*100);
//            ir.setPreviousRate(((interestRate2021-interestRate2020)/1000)*100);
                ir.setCurrentRate(interestRate2022);
                ir.setPreviousRate(interestRate2021);
                ir.setUpdateDate(lastUpdatedDate);

                interestRateList.add(ir);
            }
        }
        return interestRateList;
    }

    private InterestRate checkExistAndUpdateInterestRate(InterestRate interestRate) {
        Optional<InterestRate> dbInterestRate = interestRateRepository.getInterestRateByCountry(interestRate.getCountry());
        dbInterestRate.ifPresent(ir -> interestRate.setCountry(ir.getCountry()));
        return interestRate;
    }

}
