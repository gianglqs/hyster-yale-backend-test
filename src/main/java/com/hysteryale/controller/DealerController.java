package com.hysteryale.controller;

import com.hysteryale.exception.InvalidFileFormatException;
import com.hysteryale.model.dealer.Dealer;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.model.payLoad.DealerPayload;
import com.hysteryale.repository.upload.FileUploadRepository;
import com.hysteryale.response.ResponseObject;
import com.hysteryale.service.DealerService;
import com.hysteryale.service.FileUploadService;
import com.hysteryale.utils.EnvironmentUtils;
import com.hysteryale.utils.FileUtils;
import com.hysteryale.utils.ModelUtil;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.Map;

@RestController
@RequestMapping(path = "/dealers")
public class DealerController {
    @Resource
    DealerService dealerService;
    @Resource
    FileUploadService fileUploadService;
    @Resource
    FileUploadRepository fileUploadRepository;

    @PostMapping()
    public Map<String, Object> getDealerListing(@RequestBody DealerPayload dealerPayload, @RequestParam(defaultValue = "1") int pageNo) {
        Page<Dealer> dealerPage = dealerService.getDealerListing(dealerPayload, pageNo);
        return Map.of(
                "dealerListing", dealerPage.getContent(),
                "pageNo", dealerPage.getNumber() + 1,
                "totalItems", dealerPage.getTotalElements()
        );
    }

    @GetMapping()
    public ResponseEntity<ResponseObject> getDealerDetails(@RequestParam int dealerId) {
        Dealer dealer = dealerService.getDealerById(dealerId);
        if(dealer != null)
            return ResponseEntity.status(200).body(new ResponseObject("", dealer));
        else
            return ResponseEntity.status(404).body(new ResponseObject("Dealer not found", null));
    }

    @PostMapping(path = "/import-dealer-product")
    public ResponseEntity<ResponseObject> importDealerProduct(@RequestParam("file") MultipartFile file, Authentication authentication) throws Exception {
        String baseFolder = EnvironmentUtils.getEnvironmentValue("public-folder");
        String baseFolderUploaded = EnvironmentUtils.getEnvironmentValue("upload_files.base-folder");
        String targetFolder = EnvironmentUtils.getEnvironmentValue("upload_files.dealer_product");

        //save file on disk
        String excelFileExtension = FileUtils.EXCEL_FILE_EXTENSION;
        String savedFileName = fileUploadService.saveFileUploaded(file, authentication, targetFolder, excelFileExtension, "dealer_product");
        String filePath = baseFolder + baseFolderUploaded + targetFolder + savedFileName;
        String fileUUID = fileUploadRepository.getFileUUIDByFileName(savedFileName);

        if (!FileUtils.isExcelFile(filePath)) {
            throw new InvalidFileFormatException(file.getOriginalFilename(), fileUUID);
        }
        dealerService.importDealerProductFromFile(fileUUID, filePath);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("Import successfully", fileUUID));
    }

    @PostMapping("/get-products")
    public Map<String, Object> getDealerProducts(@RequestBody FilterModel filters,
                                                 @RequestParam(defaultValue = "1") int pageNo,
                                                 @RequestParam(defaultValue = "10") int perPage) throws ParseException {
        filters.setPageNo(pageNo);
        filters.setPerPage(perPage);
        return dealerService.getDealerProductByFilters(filters);
    }
}
