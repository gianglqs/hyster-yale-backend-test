package com.hysteryale.controller;

import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.service.FileUploadService;
import com.hysteryale.service.ProductDimensionService;
import com.hysteryale.utils.EnvironmentUtils;
import com.hysteryale.utils.FileUtils;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;

@RestController()
@RequestMapping("product")
public class ProductDimensionController {

    @Resource
    private ProductDimensionService productDimensionService;

    @Resource
    private FileUploadService fileUploadService;

    @PostMapping("/getData")
    public Map<String, Object> getDataByFilter(@RequestBody FilterModel filters,
                                               @RequestParam(defaultValue = "1") int pageNo,
                                               @RequestParam(defaultValue = "100") int perPage) throws java.text.ParseException {
        filters.setPageNo(pageNo);
        filters.setPerPage(perPage);
        return productDimensionService.getDataByFilter(filters);
    }

    @PutMapping(path = "/updateProduct", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public void updateProduct(@RequestParam("modelCode") String modelCode,
                              @RequestParam(name = "image", required = false) MultipartFile image,
                              @RequestParam(name = "description", required = false) String description,
                              Authentication authentication) throws Exception {

        if (modelCode == null)
            throw new InvalidPropertiesFormatException("ModelCode was not found!");

        if (image != null) {
            String baseFolder = EnvironmentUtils.getEnvironmentValue("upload_files.base-folder");
            String saveImageFolder = baseFolder + "/" + EnvironmentUtils.getEnvironmentValue("upload_files.product-images");
            String imageFileExtension = FileUtils.IMAGE_FILE_EXTENSION;

            String savedImageName = fileUploadService.saveFileUploaded(image, authentication, saveImageFolder, imageFileExtension);
            String saveFilePath = saveImageFolder + "/" + savedImageName;

            if (FileUtils.isImageFile(saveFilePath)) {
                if (description != null) {
                    productDimensionService.updateImageAndDescription(modelCode, savedImageName, description);
                    return;
                }
                productDimensionService.updateImage(modelCode, savedImageName);
                return;
            }

            // else -> throws Exception
            throw new InvalidPropertiesFormatException("File is not Image!");
        }

        if (description == null)
            return;
        productDimensionService.updateDescription(modelCode, description);
    }
}
