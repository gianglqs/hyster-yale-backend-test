package com.hysteryale.controller;

import com.hysteryale.model.Product;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.model.importFailure.ImportFailure;
import com.hysteryale.repository.UserRepository;
import com.hysteryale.repository.importFailure.ImportFailureRepository;
import com.hysteryale.repository.upload.FileUploadRepository;
import com.hysteryale.response.ResponseObject;
import com.hysteryale.service.FileUploadService;
import com.hysteryale.service.ProductService;
import com.hysteryale.utils.EnvironmentUtils;
import com.hysteryale.utils.FileUtils;
import com.hysteryale.utils.LocaleUtils;
import com.hysteryale.utils.ModelUtil;
import javassist.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping("product")
public class ProductController {

    @Resource
    private ProductService productService;

    @Resource
    private FileUploadService fileUploadService;

    @Resource
    private UserRepository userRepository;

    @Resource
    FileUploadRepository fileUploadRepository;


    @Resource
    LocaleUtils localeUtils;

    @Resource
    ImportFailureRepository importFailureRepository;

    @PostMapping("/getData")
    public Map<String, Object> getDataByFilter(@RequestBody FilterModel filters,
                                               @RequestParam(defaultValue = "1") int pageNo,
                                               @RequestParam(defaultValue = "100") int perPage) throws java.text.ParseException {
        filters.setPageNo(pageNo);
        filters.setPerPage(perPage);
        return productService.getDataByFilter(filters);
    }

    @PutMapping(path = "/updateProduct", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public void updateProduct(@RequestParam("modelCode") String modelCode,
                              @RequestParam("series") String series,
                              @RequestParam(name = "image", required = false) MultipartFile image,
                              @RequestParam(name = "description", required = false) String description,
                              Authentication authentication) throws Exception {

        if (modelCode == null)
            throw new InvalidPropertiesFormatException("ModelCode was not found!");
        if (series == null)
            throw new InvalidPropertiesFormatException("Series was not found!");


        String savedImageName = null;
        String fileUUID = null;
        try {
            if (image != null) {
                String targetFolder = EnvironmentUtils.getEnvironmentValue("image-folder.product");
                savedImageName = fileUploadService.upLoadImage(image, targetFolder, authentication, ModelUtil.PRODUCT);
                fileUUID = fileUploadRepository.getFileUUIDByFileName(savedImageName);
            }
            productService.updateImageAndDescription(modelCode, series, savedImageName, description);
        } catch (Exception e) {
            fileUploadService.handleUpdatedFailure(fileUUID, e.getMessage());
            throw e;
        }
        fileUploadService.handleUpdatedSuccessfully(savedImageName);
    }

    @GetMapping("/getProductDetail")
    public Product getDataForProductDetail(@RequestParam String modelCode, @RequestParam String series) throws NotFoundException {
        return productService.getProductDimensionDetail(modelCode, series);
    }

    @PostMapping(path = "/importData", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ResponseObject> importProduct(@RequestParam("file") MultipartFile file, @RequestHeader("locale") String locale, Authentication authentication) throws Exception {

        String baseFolder = EnvironmentUtils.getEnvironmentValue("public-folder");
        String baseFolderUploaded = EnvironmentUtils.getEnvironmentValue("upload_files.base-folder");
        String targetFolder = EnvironmentUtils.getEnvironmentValue("upload_files.product");
        String savedFileName = fileUploadService.saveFileUploaded(file, authentication, targetFolder, FileUtils.EXCEL_FILE_EXTENSION, ModelUtil.PRODUCT);
        String fileUUID = fileUploadRepository.getFileUUIDByFileName(savedFileName);
        String filePath = baseFolder + baseFolderUploaded + targetFolder + savedFileName;

        if (!FileUtils.isExcelFile(filePath)) {
            fileUploadService.handleUpdatedFailure(fileUUID, "Uploaded file is not an Excel file");
            throw new Exception("Imported file is not Excel");
        }

        List<ImportFailure> importFailures = new ArrayList<>();

        try {
            if (file.getOriginalFilename().toLowerCase().contains("apac")) {
              importFailures =  productService.importBaseProduct(filePath, fileUUID);
            } else if (file.getOriginalFilename().toLowerCase().contains("dimension")) {
                importFailures = productService.importDimensionProduct(filePath,fileUUID);
            } else {
                fileUploadService.handleUpdatedFailure(fileUUID, "File name is invalid");
                throw new FileNotFoundException("File name is invalid");
            }
        } catch (Exception e) {
            if (e instanceof FileNotFoundException) {
                throw e;
            }
            fileUploadService.handleUpdatedFailure(fileUUID, e.getMessage());
            throw e;
        }

        fileUploadService.handleUpdatedSuccessfully(savedFileName);
        importFailureRepository.saveAll(importFailures);

        String message = localeUtils.getMessageImportComplete(importFailures, ModelUtil.PRODUCT, locale);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject(message, fileUUID));
    }

    @GetMapping(path = "/uploadImage")
    public void uploadImage(Authentication authentication) throws Exception {

        productService.uploadImage(authentication);
    }
}