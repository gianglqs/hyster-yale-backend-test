package com.hysteryale.controller;

import com.hysteryale.model.marginAnalyst.MarginAnalystData;
import com.hysteryale.model.marginAnalyst.MarginAnalystSummary;
import com.hysteryale.model_h2.IMMarginAnalystData;
import com.hysteryale.repository_h2.IMMarginAnalystDataRepository;
import com.hysteryale.service.BookingOrderService;
import com.hysteryale.service.FileUploadService;
import com.hysteryale.service.PartService;
import com.hysteryale.service.marginAnalyst.IMMarginAnalystDataService;
import com.hysteryale.service.marginAnalyst.MarginAnalystMacroService;
import com.hysteryale.service.marginAnalyst.MarginAnalystService;
import com.hysteryale.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class MarginAnalystController {

    @Resource
    MarginAnalystService marginAnalystService;
    @Resource
    IMMarginAnalystDataService IMMarginAnalystDataService;
    @Resource
    FileUploadService fileUploadService;
    @Resource
    MarginAnalystMacroService marginAnalystMacroService;
    @Resource
    PartService partService;

    @GetMapping(path = "/marginAnalystData/getDealers")
    public Map<String, List<Map<String, String>>> getDealersInMarginAnalystData() {
        return marginAnalystService.getDealersFromMarginAnalystData();
    }

    @PostMapping(path = "/marginAnalystData", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<MarginAnalystData>> getMarginAnalystData(@RequestBody MarginAnalystData marginAnalystData) {
        if(marginAnalystData.getDealer().isEmpty())
            return marginAnalystService.getMarginAnalystData(marginAnalystData.getModelCode(), marginAnalystData.getCurrency().getCurrency(), marginAnalystData.getMonthYear());
        return marginAnalystService.getMarginDataForAnalysisByDealer(marginAnalystData.getModelCode(), marginAnalystData.getCurrency().getCurrency(), marginAnalystData.getMonthYear(), marginAnalystData.getDealer());
    }

    @PostMapping(path = "/marginAnalystSummary", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, MarginAnalystSummary> getMarginAnalystSummary(@RequestBody MarginAnalystData marginAnalystData) {
        return marginAnalystService.getMarginAnalystSummary(marginAnalystData.getModelCode(), marginAnalystData.getCurrency().getCurrency(), marginAnalystData.getMonthYear());
    }

    /**
     * Calculate MarginAnalystData and MarginAnalystSummary based on user's uploaded file
     */
    @PostMapping(path = "/estimateMarginAnalystData", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> estimateMarginAnalystData(@RequestBody IMMarginAnalystData marginData) throws Exception {

        String currency = marginData.getCurrency();
        String orderNumber = marginData.getOrderNumber();
        String fileUUID = marginData.getFileUUID();
        String plant = marginData.getPlant();
        Integer type = marginData.getType();

        if(plant.equals("HYM") || plant.equals("SN") || plant.equals("Ruyi") || plant.equals("Maximal") || plant.equals("Staxx")) {
            log.info("Start calculating non-US plant");

            IMMarginAnalystDataService.calculateNonUSMarginAnalystData(fileUUID, plant, currency);
            IMMarginAnalystDataService.calculateNonUSMarginAnalystSummary(fileUUID, plant, currency, "monthly", type);
            IMMarginAnalystDataService.calculateNonUSMarginAnalystSummary(fileUUID, plant, currency, "annually", type);
        } else {
            log.info("Start calculating US plant");
            IMMarginAnalystDataService.calculateUSPlantMarginData(currency, orderNumber, fileUUID);
        }

        List<IMMarginAnalystData> imMarginAnalystDataList =
                IMMarginAnalystDataService.getIMMarginAnalystData(
                        marginData.getModelCode(), marginData.getCurrency(),
                        marginData.getFileUUID(), marginData.getOrderNumber(),
                        marginData.getType()
                );

        Map<String, Object> imMarginAnalystSummaryMap =
                IMMarginAnalystDataService.getIMMarginAnalystSummary(
                        marginData.getModelCode(), marginData.getCurrency(),
                        marginData.getFileUUID(), marginData.getOrderNumber(),
                        marginData.getType()
                );
        return Map.of(
                "MarginAnalystData", imMarginAnalystDataList,
                "MarginAnalystSummary", imMarginAnalystSummaryMap
        );
    }

    /**
     * Check the plant of model code included in file
     */
    @PostMapping(path = "marginData/checkFilePlant", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> checkPlantOfFile(@RequestBody MultipartFile file, Authentication authentication) throws Exception {
        String filePath = fileUploadService.saveFileUploadToDisk(file);

        // Verify the Excel file
        if (FileUtils.isExcelFile(filePath)) {
            String fileUUID = fileUploadService.saveFileUpload(file, authentication);

            String plant = IMMarginAnalystDataService.checkPlantOfFile(fileUUID);
            String plantType = "US Plant";
            if(plant.equals("Maximal") || plant.equals("SN") || plant.equals("HYM") || plant.equals("Ruyi") || plant.equals("Staxx"))
                plantType = "Non-US Plant";

            return Map.of(
                    "plant", plant,
                    "plantType", plantType,
                    "fileUUID", fileUUID
            );
        } else {
            fileUploadService.deleteFileInDisk(filePath);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded file is not an Excel file");
        }
    }

    @PostMapping(path = "/importMacroFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public void importMacroFile(@RequestBody MultipartFile file, Authentication authentication) throws Exception {

        String filePath = fileUploadService.saveFileUploadToDisk(file);

        // Verify the Excel file
        if (FileUtils.isExcelFile(filePath)) {
            String originalFileName = file.getOriginalFilename();
            String fileUUID = fileUploadService.saveFileUpload(file, authentication);

            log.info(file.getContentType());

            marginAnalystMacroService.importMarginAnalystMacroFromFile(originalFileName, filePath);

        } else {
            fileUploadService.deleteFileInDisk(filePath);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded file is not an Excel file");
        }
    }

    @PostMapping(path = "/importPowerBiFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public void importPowerBiFile(@RequestBody MultipartFile file, Authentication authentication) throws Exception {
        String filePath = fileUploadService.saveFileUploadToDisk(file);

        // Verify the Excel file
        if (FileUtils.isExcelFile(filePath)) {
            String originalFileName = file.getOriginalFilename();
            String fileUUID = fileUploadService.saveFileUpload(file, authentication);

            log.info(file.getContentType());

            partService.importPartFromFile(originalFileName, filePath);

        } else {
            fileUploadService.deleteFileInDisk(filePath);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded file is not an Excel file");
        }
    }

    @PostMapping(path = "/getEstimateMarginAnalystData")
    Map<String, Object> getIMMarginAnalystData(@RequestBody IMMarginAnalystData imMarginAnalystData) throws IOException {
        log.info(imMarginAnalystData.getModelCode() + " " + imMarginAnalystData.getCurrency() + " " +
                imMarginAnalystData.getFileUUID()+ " " + imMarginAnalystData.getOrderNumber()+ " " +
                imMarginAnalystData.getType());
        List<IMMarginAnalystData> imMarginAnalystDataList =
                IMMarginAnalystDataService.getIMMarginAnalystData(
                        imMarginAnalystData.getModelCode(), imMarginAnalystData.getCurrency(),
                        imMarginAnalystData.getFileUUID(), imMarginAnalystData.getOrderNumber(),
                        imMarginAnalystData.getType()
                );

        Map<String, Object> imMarginAnalystSummaryMap =
                IMMarginAnalystDataService.getIMMarginAnalystSummary(
                        imMarginAnalystData.getModelCode(), imMarginAnalystData.getCurrency(),
                        imMarginAnalystData.getFileUUID(), imMarginAnalystData.getOrderNumber(),
                        imMarginAnalystData.getType()
                );

        return Map.of(
                "MarginAnalystData", imMarginAnalystDataList,
                "MarginAnalystSummary", imMarginAnalystSummaryMap
        );
    }
}

