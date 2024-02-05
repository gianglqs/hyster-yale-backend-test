package com.hysteryale.controller;

import com.hysteryale.model.Product;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.response.ResponseObject;
import com.hysteryale.service.FileUploadService;
import com.hysteryale.service.ProductService;
import com.hysteryale.utils.EnvironmentUtils;
import com.hysteryale.utils.FileUtils;
import javassist.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;

@RestController()
@RequestMapping("product")
public class ProductController {

    @Resource
    private ProductService productService;

    @Resource
    private FileUploadService fileUploadService;

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
                              @RequestParam(name = "image", required = false) MultipartFile image,
                              @RequestParam(name = "description", required = false) String description,
                              Authentication authentication) throws Exception {

        if (modelCode == null)
            throw new InvalidPropertiesFormatException("ModelCode was not found!");

        String imageName = null;
        if (image != null) {
            String baseFolder = EnvironmentUtils.getEnvironmentValue("public-folder");
            String targetFolder = EnvironmentUtils.getEnvironmentValue("image-folder.product");
            String saveImageFolder = baseFolder + targetFolder;

            String savedImageName = fileUploadService.upLoadImage(image, targetFolder);
            String saveFilePath = saveImageFolder + savedImageName;

            if (FileUtils.isImageFile(saveFilePath)) {
                imageName = targetFolder + savedImageName;
            }
        }
        productService.updateImageAndDescription(modelCode, imageName, description);
    }

    @GetMapping("/getProductDetail")
    public Product getDataForProductDetail(@RequestParam String modelCode) throws NotFoundException, IOException {
        return productService.getProductDimensionDetail(modelCode);
    }

    @PostMapping(path="/importData", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ResponseObject> importProduct(@RequestBody MultipartFile file, Authentication authentication) throws Exception {
        String baseFolder = EnvironmentUtils.getEnvironmentValue("upload_files.base-folder");
        String excelFileExtension = FileUtils.EXCEL_FILE_EXTENSION;
        if (!FileUtils.isExcelFile(file.getInputStream()))
            throw new InvalidPropertiesFormatException("Importing file is not Excel file");

        String pathFileUploaded = fileUploadService.saveFileUploaded(file, authentication, baseFolder, excelFileExtension);
        productService.importProduct(pathFileUploaded);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("Import data successfully", null));
    }
}