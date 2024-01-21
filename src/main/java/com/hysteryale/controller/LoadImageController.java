package com.hysteryale.controller;

import com.hysteryale.service.LoadImageService;
import com.hysteryale.utils.EnvironmentUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.FileNotFoundException;

@RestController()
@RequestMapping("loadImage")
public class LoadImageController {

    @Resource
    private LoadImageService loadImageService;

    @GetMapping(path="/product/{imageName}",produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<org.springframework.core.io.Resource> loadProductImage(@PathVariable String imageName) throws FileNotFoundException {
        String productFolder = EnvironmentUtils.getEnvironmentValue("upload_files.product-images");
        return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(loadImageService.getImage(productFolder, imageName));
    }

    @GetMapping(path="/part/{imageName}",produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<org.springframework.core.io.Resource> loadPartImage(@PathVariable String imageName) throws FileNotFoundException {
        String partFolder = EnvironmentUtils.getEnvironmentValue("upload_files.part-images");
        return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(loadImageService.getImage(partFolder,imageName));
    }
}
