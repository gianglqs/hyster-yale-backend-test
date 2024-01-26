package com.hysteryale.service;

import com.hysteryale.utils.EnvironmentUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;

@Service
public class LoadImageService {

    public Resource getImage(String folder, String imageName) throws FileNotFoundException {
        String baseFolder = EnvironmentUtils.getEnvironmentValue("upload_files.base-folder");
        String pathFile = baseFolder + folder + "/" + imageName;
        File file = new File(pathFile);
        if (file.exists())
            return new FileSystemResource(file) {
            };
        throw new FileNotFoundException("Not found image!");
    }
}