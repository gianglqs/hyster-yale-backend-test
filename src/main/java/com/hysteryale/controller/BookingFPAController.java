package com.hysteryale.controller;

import com.hysteryale.exception.InvalidFileFormatException;
import com.hysteryale.model.enums.FrequencyImport;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.model.importFailure.ImportFailure;
import com.hysteryale.repository.upload.FileUploadRepository;
import com.hysteryale.response.ResponseObject;
import com.hysteryale.service.BookingFPAService;
import com.hysteryale.service.FileUploadService;
import com.hysteryale.service.ImportTrackingService;
import com.hysteryale.utils.EnvironmentUtils;
import com.hysteryale.utils.FileUtils;
import com.hysteryale.utils.LocaleUtils;
import com.hysteryale.model.enums.ModelTypeEnum;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("bookingFPA")
public class BookingFPAController {

    @Resource
    private BookingFPAService bookingPFAService;
    @Resource
    FileUploadService fileUploadService;

    @Resource
    FileUploadRepository fileUploadRepository;

    @Resource
    LocaleUtils localeUtils;

    @Resource
    ImportTrackingService importTrackingService;

    @PostMapping("/getBookingMarginTrialTest")
    public Map<String, Object> getDataFinancialShipment(@RequestBody FilterModel filters,
                                                        @RequestParam(defaultValue = "1") int pageNo,
                                                        @RequestParam(defaultValue = "100") int perPage) throws ParseException {
        filters.setPageNo(pageNo);
        filters.setPerPage(perPage);

        return bookingPFAService.getBookingMarginTrialTest(filters);

    }

    @PostMapping(path = "/importNewBookingFPA", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ResponseObject> importNewDataShipment(MultipartFile file, @RequestHeader("locale") String locale, Authentication authentication) throws Exception {
        String baseFolder = EnvironmentUtils.getEnvironmentValue("public-folder");
        String baseFolderUploaded = EnvironmentUtils.getEnvironmentValue("upload_files.base-folder");
        String targetFolder = EnvironmentUtils.getEnvironmentValue("upload_files.bookingFPA");
        String excelFileExtension = FileUtils.EXCEL_FILE_EXTENSION;
        String savedFileName = fileUploadService.saveFileUploaded(file, authentication, targetFolder, excelFileExtension, ModelTypeEnum.BOOKING_FPA.getValue());
        String fileUUID = fileUploadRepository.getFileUUIDByFileName(savedFileName);
        String pathFile = baseFolder + baseFolderUploaded + targetFolder + savedFileName;

        if (!FileUtils.isExcelFile(pathFile)) {
            throw new InvalidFileFormatException(file.getOriginalFilename(), fileUUID);
        }

        InputStream inputStream = new FileInputStream(pathFile);
        List<ImportFailure> importFailures = bookingPFAService.importBookingFPA(inputStream, fileUUID);
        String message = localeUtils.getMessageImportComplete(importFailures, "Booking FP&A", locale);
        fileUploadService.handleUpdatedSuccessfully(savedFileName);
        // update ImportTracking
        importTrackingService.updateImport(fileUUID, file.getOriginalFilename(), FrequencyImport.AD_HOC_IMPORT);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject(message, fileUUID));
    }
}
