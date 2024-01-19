package com.hysteryale.controller;

import com.hysteryale.service.LoadImageService;
import org.apache.coyote.Response;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.FileNotFoundException;
import java.io.InputStream;

@RestController()
@RequestMapping("loadImage")
public class LoadImageController {

    @Resource
    private LoadImageService loadImageService;

    @GetMapping(path="/product/{imageName}",produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<org.springframework.core.io.Resource> loadProductImage(@PathVariable String imageName) throws FileNotFoundException {
        return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(loadImageService.getImageProduct(imageName));
    }
}
