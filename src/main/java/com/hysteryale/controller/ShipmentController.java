package com.hysteryale.controller;

import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.response.ResponseObject;
import com.hysteryale.service.FileUploadService;
import com.hysteryale.service.ImportService;
import com.hysteryale.service.ShipmentService;
import com.hysteryale.service.UpdateHistoryService;
import com.hysteryale.utils.EnvironmentUtils;
import com.hysteryale.utils.FileUtils;
import com.hysteryale.utils.ModelUtil;
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
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Map;

@RestController
public class ShipmentController {

    @Resource
    ShipmentService shipmentService;

    @Resource
    ImportService importService;
    @Resource
    FileUploadService fileUploadService;

    @Resource
    UpdateHistoryService updateHistoryService;

    @PostMapping("/getShipmentData")
    public Map<String, Object> getDataFinancialShipment(@RequestBody FilterModel filters,
                                                        @RequestParam(defaultValue = "1") int pageNo,
                                                        @RequestParam(defaultValue = "100") int perPage) throws ParseException {
        filters.setPageNo(pageNo);
        filters.setPerPage(perPage);


        return shipmentService.getShipmentByFilter(filters);

    }

    @PostMapping(path = "/importNewShipment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ResponseObject> importNewDataShipment(@RequestBody MultipartFile file, Authentication authentication) {

        try {
            InputStream is = file.getInputStream();
            String modelType = ModelUtil.SHIPMENT;
            if (FileUtils.isExcelFile(is)) {
                // save file in folder tmp/UploadFiles
                String baseFolder = EnvironmentUtils.getEnvironmentValue("upload_files.base-folder");
                String excelFileExtension = FileUtils.EXCEL_FILE_EXTENSION;
                String fileName = fileUploadService.saveFileUploaded(file, authentication, baseFolder, excelFileExtension, modelType);

                InputStream inputStream = new FileInputStream(baseFolder + "/" + fileName);
                importService.importShipmentFileOneByOne(inputStream);

                updateHistoryService.handleUpdatedSuccessfully(fileName, ModelUtil.SHIPMENT, authentication);
                return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("Import data successfully", null));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseObject("Uploaded file is not an Excel file", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseObject(e.getMessage(), null));
        }
    }
}
