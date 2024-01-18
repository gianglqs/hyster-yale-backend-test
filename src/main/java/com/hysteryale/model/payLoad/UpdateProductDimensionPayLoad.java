package com.hysteryale.model.payLoad;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class UpdateProductDimensionPayLoad {
    private String modelCode;
    private MultipartFile image;
    private String description;
}
