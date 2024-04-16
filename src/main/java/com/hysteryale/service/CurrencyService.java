/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.service;

import com.hysteryale.exception.ExchangeRatesException;
import com.hysteryale.model.Currency;
import com.hysteryale.repository.CurrencyRepository;
import com.hysteryale.utils.EnvironmentUtils;
import com.hysteryale.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

@Service
@Slf4j
public class CurrencyService {
    @Resource
    CurrencyRepository currencyRepository;

    public void importCurrencies() throws IOException {

        log.info("========= Start importing Currencies ==========");
        String baseFolder = EnvironmentUtils.getEnvironmentValue("import-files.base-folder");
        String folderPath = baseFolder + EnvironmentUtils.getEnvironmentValue("import-files.currency");

        // Try to find if there is file in this folder
        List<String> files = FileUtils.getAllFilesInFolder(folderPath);

        //if there is a file, then use it to extract currency, all files in this folder can be used to do that because they have the same currencies
        if (!files.isEmpty()) {

            List<Currency> currencyList = new ArrayList<>();

            log.info("=== Use file " + files.get(0) + "to import currencies");

            FileInputStream is = new FileInputStream(folderPath + "/" + files.get(0));

            XSSFWorkbook workbook = new XSSFWorkbook(is);
            int numberOfSheets = workbook.getNumberOfSheets();
            if (numberOfSheets > 0) {
                Sheet sheet = workbook.getSheet("USD");

                for (int i = 8; i < 48; i++) {
                    Row row = sheet.getRow(i);
                    String currencyName = row.getCell(0).getStringCellValue().strip();
                    if (!currencyName.isEmpty()) {
                        String currencyCode = row.getCell(1).getStringCellValue().strip();
                        currencyList.add(new Currency(currencyCode, currencyName));
                    } else {
                        break;
                    }
                }
            }

            currencyRepository.saveAll(currencyList);
            log.info("Import Currencies Completed");
        }

    }

    public Currency getCurrenciesByName(String currencyName) throws ExchangeRatesException {
        Optional<Currency> optionalCurrencies = currencyRepository.getCurrenciesByName(currencyName);
        if (optionalCurrencies.isEmpty())
            throw new ExchangeRatesException("Unsupported currency " + currencyName, currencyName);
        return optionalCurrencies.get();
    }

    public Currency getCurrencies(String currencyName) {
        Optional<Currency> optionalCurrencies = currencyRepository.getCurrenciesByName(currencyName);
        return optionalCurrencies.orElse(null);
    }
}
