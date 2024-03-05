package com.hysteryale.controller;

import com.hysteryale.model.Product;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.response.ResponseObject;
import com.hysteryale.service.FileUploadService;
import com.hysteryale.service.ProductService;
import com.hysteryale.utils.EnvironmentUtils;
import com.hysteryale.utils.FileUtils;
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
        String saveFilePath = null;

        try {
            if (image != null) {
                String baseFolder = EnvironmentUtils.getEnvironmentValue("public-folder");
                String targetFolder = EnvironmentUtils.getEnvironmentValue("image-folder.product");
                String saveImageFolder = baseFolder + targetFolder;
                savedImageName = fileUploadService.upLoadImage(image, targetFolder, authentication, ModelUtil.PRODUCT);
                saveFilePath = saveImageFolder + savedImageName;
            }
            productService.updateImageAndDescription(modelCode, series, savedImageName, description);
        } catch (Exception e) {
            fileUploadService.handleUpdatedFailure(savedImageName, e.getMessage());
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
    public ResponseEntity<ResponseObject> importProduct(@RequestParam("files") List<MultipartFile> fileList, Authentication authentication) throws Exception {
        // check 2 files is EXCEL
        for (MultipartFile file : fileList) {
            if (!FileUtils.isExcelFile(file.getInputStream()))
                throw new InvalidPropertiesFormatException(file.getOriginalFilename() + " is not Excel file");
        }

        productService.importProduct(fileList, authentication);

        return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("Import data successfully", null));
    }
}