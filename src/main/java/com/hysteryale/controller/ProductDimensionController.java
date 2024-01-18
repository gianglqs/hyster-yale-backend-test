package com.hysteryale.controller;

import com.hysteryale.model.ProductDimension;
import com.hysteryale.model.filters.FilterModel;
import com.hysteryale.model.payLoad.UpdateProductDimensionPayLoad;
import com.hysteryale.service.FileUploadService;
import com.hysteryale.service.ProductDimensionService;
import com.hysteryale.utils.EnvironmentUtils;
import com.hysteryale.utils.FileUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
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

    @PutMapping("/updateProduct")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void updateProduct(@RequestBody UpdateProductDimensionPayLoad product, Authentication authentication) throws Exception {
        // save file into folder Upload ProductImage if any
        if (product.getImage() != null) {
            String baseFolder = EnvironmentUtils.getEnvironmentValue("upload_files.base-folder");
            String saveImageFolder = baseFolder + "/" + EnvironmentUtils.getEnvironmentValue("upload_files.product-images");

            String imagePath = fileUploadService.saveFileUploaded(product.getImage(), authentication, saveImageFolder);
            // check if it is an image or not
            if(FileUtils.isImageFile(imagePath)){
                //update
                productDimensionService.updateImageAndDescription(product.getModelCode(), imagePath, product.getDescription());
            }else{
                //delete file on disk
                fileUploadService.deleteFileInDisk(imagePath);

                // update
                productDimensionService.updateDescription(product.getModelCode(), product.getDescription());
            }
        }
        // update ProductDimension
    }
}
