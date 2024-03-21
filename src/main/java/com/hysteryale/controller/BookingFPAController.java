package com.hysteryale.controller;

import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.response.ResponseObject;
import com.hysteryale.service.BookingFPAService;
import com.hysteryale.service.FileUploadService;
import com.hysteryale.utils.EnvironmentUtils;
import com.hysteryale.utils.FileUtils;
import com.hysteryale.utils.ModelUtil;
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
import java.util.Map;

@RestController
@RequestMapping("bookingFPA")
public class BookingFPAController {

    @Resource
    private BookingFPAService bookingPFAService;
    @Resource
    FileUploadService fileUploadService;

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
    public ResponseEntity<ResponseObject> importNewDataShipment(MultipartFile file, Authentication authentication) throws Exception {
        String baseFolder = EnvironmentUtils.getEnvironmentValue("public-folder");
        String baseFolderUploaded = EnvironmentUtils.getEnvironmentValue("upload_files.base-folder");
        String targetFolder = EnvironmentUtils.getEnvironmentValue("upload_files.bookingFPA");
        String excelFileExtension = FileUtils.EXCEL_FILE_EXTENSION;
        String fileName = fileUploadService.saveFileUploaded(file, authentication, targetFolder, excelFileExtension, ModelUtil.BOOKING_FPA);
        String pathFile = baseFolder + baseFolderUploaded + targetFolder + fileName;
        if (FileUtils.isExcelFile(pathFile)) {
            try {
                InputStream inputStream = new FileInputStream(pathFile);
                bookingPFAService.importBookingFPA(inputStream);

                //       fileUploadService.handleUpdatedSuccessfully(fileName);
                return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("Import data successfully", null));

            } catch (Exception e) {
                //       fileUploadService.handleUpdatedFailure(fileName, e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseObject(e.getMessage(), null));
            }
        } else {
            //     fileUploadService.handleUpdatedFailure(fileName, "Uploaded file is not an Excel file");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseObject("Uploaded file is not an Excel file", null));
        }
    }
}
