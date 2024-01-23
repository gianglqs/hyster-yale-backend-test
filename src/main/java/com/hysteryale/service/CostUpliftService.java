package com.hysteryale.service;

import com.hysteryale.model.CostUplift;
import com.hysteryale.repository.CostUpliftRepository;
import com.hysteryale.utils.DateUtils;
import com.hysteryale.utils.EnvironmentUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class CostUpliftService {
    @Resource
    CostUpliftRepository costUpliftRepository;

    public List<String> getAllFilesInFolder(String folderPath) {
        Pattern pattern = Pattern.compile("^(01. Bookings Register).*(.xlsx)$");

        List<String> fileList = new ArrayList<>();
        Matcher matcher;
        try {
            DirectoryStream<Path> folder = Files.newDirectoryStream(Paths.get(folderPath));
            for(Path path : folder) {
                matcher = pattern.matcher(path.getFileName().toString());
                if(matcher.matches())
                    fileList.add(path.getFileName().toString());
                else
                    log.error("Wrong formatted file's name: " + path.getFileName().toString());
            }
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        log.info("File list: " + fileList);
        return fileList;
    }

    public void importCostUplift() throws IOException {
        // Folder contains Excel file of Booking Order
        String baseFolder = EnvironmentUtils.getEnvironmentValue("import-files.base-folder");
        String folderPath = baseFolder + EnvironmentUtils.getEnvironmentValue("import-files.booking");
        // Get files in Folder Path
        List<String> fileList = getAllFilesInFolder(folderPath);
        List<CostUplift> costUpliftList = new ArrayList<>();

        for(String fileName : fileList) {
            log.info("{ Start importing file: '" + fileName + "'");
            InputStream is = new FileInputStream(folderPath + "/" + fileName);
            XSSFWorkbook workbook = new XSSFWorkbook(is);

            //Pattern for getting month and year in fileName
            Pattern pattern = Pattern.compile(".{24}(.{4}).*(\\d{4}).*");
            Matcher matcher = pattern.matcher(fileName);

            LocalDate date = LocalDate.now();

            // Assign date
            if(matcher.find()) {
                String month = matcher.group(1).strip().replace("-", "");
                int year = Integer.parseInt(matcher.group(2));
                date = LocalDate.of(year, DateUtils.getMonth(month), 1);
            }

            Sheet sheet = workbook.getSheet("Currency & Conversion");

            for(Row row : sheet) {
                if(!row.getCell(5, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().isEmpty()
                    && row.getRowNum() > 2 && row.getRowNum() < 9) {

                    String plantName = row.getCell(5).getStringCellValue();
                    double costUplift = row.getCell(6).getNumericCellValue();

                    CostUplift costUpliftObject = new CostUplift();
                    costUpliftObject.setPlant(plantName);
                    costUpliftObject.setCostUplift(costUplift);
                    costUpliftObject.setDate(date);

                    // Check if CostUplift with plantName and date is existed
                    if(costUpliftRepository.getCostUpliftByPlantAndDate(plantName, date).isEmpty())
                        costUpliftList.add(costUpliftObject);
                }
            }
            costUpliftRepository.saveAll(costUpliftList);
            log.info("CostUplift are newly saved or updated: " + costUpliftList.size());
            log.info("End importing }");
            costUpliftList.clear();
        }
    }
}
