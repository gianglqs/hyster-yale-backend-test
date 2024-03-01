package com.hysteryale.controller;

import com.hysteryale.exception.CanNotUpdateException;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.response.ResponseObject;
import com.hysteryale.service.BookingService;
import com.hysteryale.service.FileUploadService;
import com.hysteryale.utils.EnvironmentUtils;
import com.hysteryale.utils.FileUtils;
import com.hysteryale.utils.ModelUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class BookingController {

    @Resource
    BookingService bookingService;

    @Resource
    FileUploadService fileUploadService;

    private FilterModel filters;



    /**
     * Get BookingOrders based on filters and pagination
     *
     * @param filters from FE
     * @param pageNo  current page
     * @param perPage number of items per page
     */

    @PostMapping(path = "/bookingOrders", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getDataBooking(@RequestBody FilterModel filters,
                                              @RequestParam(defaultValue = "1") int pageNo,
                                              @RequestParam(defaultValue = "100") int perPage) throws java.text.ParseException {
        filters.setPageNo(pageNo);
        filters.setPerPage(perPage);
        this.filters = filters;
        return bookingService.getBookingByFilter(filters);

    }

    @PostMapping(path = "/importNewBooking", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ResponseObject> importNewDataBooking(@RequestParam("files") List<MultipartFile> fileList, Authentication authentication) {
        String fileNameBooking = "";
        String fileNameCostData = "";
        String baseFolder = EnvironmentUtils.getEnvironmentValue("upload_files.base-folder");

        boolean invalid = false;
        try {
            for (MultipartFile file : fileList) {

                //save file on disk
                if (FileUtils.isExcelFile(file.getInputStream())) {
                    String excelFileExtension = FileUtils.EXCEL_FILE_EXTENSION;
                    String fileNameSaved = fileUploadService.saveFileUploaded(file, authentication, baseFolder, excelFileExtension, ModelUtil.BOOKING);
                    // save file to disk
                    if (FileUtils.checkFileNameValid(file, "booked") || FileUtils.checkFileNameValid(file, "booking")) {
                        fileNameBooking = fileNameSaved;
                    } else if (FileUtils.checkFileNameValid(file, "cost_data")) {
                        fileNameCostData = fileNameSaved;
                    }
                }

            }
            // import

            if (!fileNameBooking.isEmpty()) {
                try {
                    bookingService.importNewBookingFileByFile(baseFolder + "/" + fileNameBooking);
                    fileUploadService.handleUpdatedSuccessfully(fileNameBooking);
                    invalid = true;
                } catch (CanNotUpdateException e) {
                    fileUploadService.handleUpdatedFailure(fileNameBooking, e.getMessage());
                }
            }

            if (!fileNameCostData.isEmpty()) {
                try {
                    bookingService.importCostData(baseFolder + "/" + fileNameCostData);
                    fileUploadService.handleUpdatedSuccessfully(fileNameCostData);
                    invalid = true;
                } catch (CanNotUpdateException e) {
                    fileUploadService.handleUpdatedFailure(fileNameCostData, e.getMessage());
                }
            }
            if (!invalid) {

                throw new Exception("No valid file found");
            }
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("Import successfully!", null));
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseObject(e.getMessage(), null));
        }
    }

}
