package com.hysteryale.service;

import com.hysteryale.model.Currency;
import com.hysteryale.model.Part;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.repository.PartRepository;
import com.hysteryale.response.ResponseObject;
import com.hysteryale.utils.ConvertDataFilterUtil;
import com.hysteryale.utils.DateUtils;
import com.hysteryale.utils.EnvironmentUtils;
import com.hysteryale.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class PartService extends BasedService {
    @Resource
    PartRepository partRepository;
    @Resource
    CurrencyService currencyService;

    private static final HashMap<String, Integer> powerBIExportColumns = new HashMap<>();

    public void getPowerBiColumnsName(Row row) {
        for (Cell cell : row) {
            String columnsName = cell.getStringCellValue();
            powerBIExportColumns.put(columnsName, cell.getColumnIndex());
        }
    }

    public Part mapExcelDataToPart(Row row) {
        String strCurrency = row.getCell(powerBIExportColumns.get("Currency")).getStringCellValue().strip();
        Currency currency = currencyService.getCurrenciesByName(strCurrency);

        String quoteId = row.getCell(powerBIExportColumns.get("Quote Number")).getStringCellValue();
        int quantity = (int) row.getCell(powerBIExportColumns.get("Quoted Quantity")).getNumericCellValue();

        String series = row.getCell(powerBIExportColumns.get("Series")).getStringCellValue();
        String partNumber = row.getCell(powerBIExportColumns.get("Part Number")).getStringCellValue();
        double listPrice = row.getCell(powerBIExportColumns.get("ListPrice")).getNumericCellValue();
        String modelCode = row.getCell(powerBIExportColumns.get("Model")).getStringCellValue();
        String clazz = row.getCell(powerBIExportColumns.get("Class"), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
        String region = row.getCell(powerBIExportColumns.get("Region"), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();

        double discountPercentage;
        Cell discountPercentageCell = row.getCell(powerBIExportColumns.get("Discount"));
        if (discountPercentageCell.getCellType() == CellType.NUMERIC) {
            discountPercentage = discountPercentageCell.getNumericCellValue();
        } else {
            discountPercentage = Double.parseDouble(discountPercentageCell.getStringCellValue().isEmpty() ? "0" : discountPercentageCell.getStringCellValue());
        }
        double discount = listPrice * (1 - (discountPercentage / 100));
        String billTo = row.getCell(powerBIExportColumns.get("Dealer")).getStringCellValue();

        double netPriceEach = row.getCell(powerBIExportColumns.get("Net Price")).getNumericCellValue();
        // double discountToCustomerPercentage
        double customerPrice;
        Cell customerPriceCell = row.getCell(powerBIExportColumns.get("Customer Price"));

        if (customerPriceCell.getCellType() == CellType.NUMERIC) {
            customerPrice = customerPriceCell.getNumericCellValue();
        } else {
            customerPrice = Double.parseDouble(customerPriceCell.getStringCellValue().isEmpty() ? "0" : customerPriceCell.getStringCellValue());
        }

        double extendedCustomerPrice;
        Cell extendedCustomerPriceCell = row.getCell(powerBIExportColumns.get("Ext Customer Price"));
        if (extendedCustomerPriceCell.getCellType() == CellType.NUMERIC) {
            extendedCustomerPrice = extendedCustomerPriceCell.getNumericCellValue();
        } else {
            extendedCustomerPrice = Double.parseDouble(extendedCustomerPriceCell.getStringCellValue().isEmpty() ? "0" : extendedCustomerPriceCell.getStringCellValue());
        }

        String orderNumber = row.getCell(powerBIExportColumns.get("Order Number")).getStringCellValue();

        // specify isSPED if the description contains "
        boolean isSPED =
                row.getCell(powerBIExportColumns.get("Part Description: English US"), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().contains("SPED") ||
                        row.getCell(powerBIExportColumns.get("Part Description: English UK"), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().contains("SPED");

        //optionType, orderBookedDate, orderRequestDate

        return new Part(quoteId, quantity, orderNumber, modelCode, series, partNumber, listPrice, discount, discountPercentage, billTo, netPriceEach, customerPrice, extendedCustomerPrice, currency, clazz, region, isSPED);
    }

    /**
     * Verify if Part is existed or not
     */
    public boolean isPartExisted(String modelCode, String partNumber, String orderNumber, Calendar recordedTime, String strCurrency) {
        int isPartExisted = partRepository.isPartExisted(modelCode, partNumber, orderNumber, recordedTime, strCurrency);
        return isPartExisted == 1;
    }

    public void importPartFromFile(String fileName, String filePath) throws IOException {
        logInfo("==== Importing " + fileName + " ====");
        InputStream is = new FileInputStream(filePath);
        XSSFWorkbook workbook = new XSSFWorkbook(is);

        Sheet sheet = workbook.getSheet("Export");
        List<Part> partList = new ArrayList<>();

        // get recordedTime for Part
        // fileName pattern: power bi Aug 23
        Pattern pattern = Pattern.compile("\\w{5} \\w{2} (\\w{3}) (\\d{2}).xlsx");
        Matcher matcher = pattern.matcher(fileName);
        String month;
        int year;
        if (matcher.find()) {
            month = matcher.group(1);
            year = 2000 + Integer.parseInt(matcher.group(2));
        } else
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File name is not in appropriate format");
        Calendar recordedTime = Calendar.getInstance();
        recordedTime.set(year, DateUtils.monthMap.get(month), 1);

        for (Row row : sheet) {
            if (row.getRowNum() == 0)
                getPowerBiColumnsName(row);
            else if (!row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().isEmpty()) {
                String modelCode = row.getCell(powerBIExportColumns.get("Model")).getStringCellValue();
                String partNumber = row.getCell(powerBIExportColumns.get("Part Number")).getStringCellValue();
                String orderNumber = row.getCell(powerBIExportColumns.get("Order Number")).getStringCellValue();
                String strCurrency = row.getCell(powerBIExportColumns.get("Currency")).getStringCellValue().strip();

                Part part = mapExcelDataToPart(row);
                if (!isPartExisted(modelCode, partNumber, orderNumber, recordedTime, strCurrency)) {
                    part.setRecordedTime(recordedTime);
                } else {
                    part.setRecordedTime(recordedTime);
                    updatePart(getPart(modelCode, partNumber, orderNumber, recordedTime, strCurrency), part);
                }
                partList.add(part);
            }
        }
        log.info("Number of Part in " + month + "-" + year + " save: " + partList.size());
        partRepository.saveAll(partList);
        partList.clear();
        updateStateImportFile(filePath);
    }

    public void importPart() throws IOException {
        String baseFolder = EnvironmentUtils.getEnvironmentValue("import-files.base-folder");
        String folderPath = baseFolder + EnvironmentUtils.getEnvironmentValue("import-files.bi-download");
        List<String> files = FileUtils.getAllFilesInFolder(folderPath);

        logInfo("Files: " + files);

        for (String fileName : files) {
            String filePath = folderPath + "/" + fileName;
            //check file has been imported ?
            if (isImported(filePath)) {
                logWarning("file '" + fileName + "' has been imported");
                continue;
            }
            importPartFromFile(fileName, filePath);
        }
    }

    public Part getPart(String modelCode, String partNumber, String orderNumber, Calendar recordedTime, String strCurrency) {
        Optional<Part> optionalPart = partRepository.getPart(modelCode, partNumber, orderNumber, recordedTime, strCurrency);
        return optionalPart.orElse(null);
    }

    private void updatePart(Part dbPart, Part filePart) {
        filePart.setId(dbPart.getId());
    }

    public List<String> getDistinctModelCodeByMonthYear(Calendar monthYear) {
        return partRepository.getDistinctModelCodeByMonthYear(monthYear);
    }

    public List<Part> getDistinctPart(String modelCode, String currency) {
        return partRepository.getDistinctPart(modelCode, currency);
    }

    public Double getAverageDealerNet(String region, String clazz, String series) {
        Double averageDealerNet = partRepository.getAverageDealerNet(region, clazz, series);
        return averageDealerNet != null ? averageDealerNet : 0;
    }

    public List<String> getAllPartNumbersByOrderNo(String orderNo) {
        return partRepository.getPartNumberByOrderNo(orderNo);
    }

    public Currency getCurrencyByOrderNo(String orderNo) {
        return partRepository.getCurrencyByOrderNo(orderNo);
    }


    public Map<String, Object> getPartByFilter(FilterModel filters) throws ParseException {
        Map<String, Object> result = new HashMap<>();

        Map<String, Object> filtersMap = ConvertDataFilterUtil.loadDataFilterIntoMap(filters);

        // get Parts by modelCode and orderNos
        List<Part> partList = partRepository.getPartForProductDimensionDetail(
                (String) filtersMap.get("modelCodeFilter"),
                (List<String>) filtersMap.get("orderNumberListFilter"),
                (Pageable) filtersMap.get("pageable")
        );
        result.put("listPart", partList);

        return result;
    }
}
