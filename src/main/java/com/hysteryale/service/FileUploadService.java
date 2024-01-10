package com.hysteryale.service;

import com.hysteryale.model.User;
import com.hysteryale.model.upload.FileUpload;
import com.hysteryale.repository.upload.FileUploadRepository;
import com.hysteryale.utils.EnvironmentUtils;
import com.hysteryale.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
@EnableTransactionManagement
public class FileUploadService {
    @Resource
    FileUploadRepository fileUploadRepository;
    @Resource
    UserService userService;

    /**
     * Save uploaded excel into disk and file information into db
     *
     * @param excelFile
     * @param authentication contains upload person's email
     * @return UUID string
     */
    public String saveFileUpload(MultipartFile excelFile, Authentication authentication) {
        // Find who uploads this file
        String uploadedByEmail = authentication.getName();
        Optional<User> optionalUploadedBy = userService.getActiveUserByEmail(uploadedByEmail);

        if (optionalUploadedBy.isPresent()) {
            User uploadedBy = optionalUploadedBy.get();
            FileUpload fileUpload = new FileUpload();
            Date uploadedTime = new Date();
            String strUploadedTime = (new SimpleDateFormat("ddMMyyyyHHmmss").format(uploadedTime));

            // generate random UUID
            fileUpload.setUuid(UUID.randomUUID().toString());
            fileUpload.setUploadedBy(uploadedBy);
            fileUpload.setUploadedTime(uploadedTime);

            // append suffix into fileName
            fileUpload.setFileName(FileUtils.encoding(Objects.requireNonNull(excelFile.getOriginalFilename())) + "_" + strUploadedTime + ".xlsx");

            // save information to db
            fileUploadRepository.save(fileUpload);

            return fileUpload.getUuid();
        } else
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cannot find user with email: " + uploadedByEmail);
    }

    /**
     * Save the excelFile into disk
     *
     * @return absolute filePath of multipartFile
     */
    public String saveFileUploadToDisk(MultipartFile multipartFile) throws Exception {
        String baseFolder = EnvironmentUtils.getEnvironmentValue("upload_files.base-folder");

        Date uploadedTime = new Date();
        String strUploadedTime = (new SimpleDateFormat("ddMMyyyyHHmmssSSS").format(uploadedTime));
        String encodedFileName = FileUtils.encoding(Objects.requireNonNull(multipartFile.getOriginalFilename())) + "_" + strUploadedTime + ".xlsx";

        File file = new File(baseFolder + "/" + encodedFileName);
        if (file.createNewFile()) {
            log.info("File " + encodedFileName + " created");
            multipartFile.transferTo(file);
            return baseFolder + "/" + encodedFileName;
        } else {
            log.info("Can not create new file: " + encodedFileName);
            throw new Exception("Can not create new file: " + encodedFileName);
        }
    }

    public String saveFileUploaded(MultipartFile multipartFile, Authentication authentication) throws Exception {
        String baseFolder = EnvironmentUtils.getEnvironmentValue("upload_files.base-folder");

        Date uploadedTime = new Date();
        String strUploadedTime = (new SimpleDateFormat("ddMMyyyyHHmmss").format(uploadedTime));
        String encodedFileName = FileUtils.encoding(Objects.requireNonNull(multipartFile.getOriginalFilename())) + "_" + strUploadedTime + ".xlsx";

        File file = new File(baseFolder + "/" + encodedFileName);
        if (file.createNewFile()) {
            log.info("File " + encodedFileName + " created");
            multipartFile.transferTo(file);
            saveFileUpLoadIntoDB(authentication, encodedFileName);
            return baseFolder + "/" + encodedFileName;
        } else {
            log.info("Can not create new file: " + encodedFileName);
            throw new Exception("Can not create new file: " + encodedFileName);
        }
    }

    private String saveFileUpLoadIntoDB(Authentication authentication, String encodeFileName ){
        String uploadedByEmail = authentication.getName();
        Optional<User> optionalUploadedBy = userService.getActiveUserByEmail(uploadedByEmail);

        if (optionalUploadedBy.isPresent()) {
            User uploadedBy = optionalUploadedBy.get();
            FileUpload fileUpload = new FileUpload();
            Date uploadedTime = new Date();

            // generate random UUID
            fileUpload.setUuid(UUID.randomUUID().toString());
            fileUpload.setUploadedBy(uploadedBy);
            fileUpload.setUploadedTime(uploadedTime);

            // append suffix into fileName
            fileUpload.setFileName(encodeFileName);

            // save information to db
            fileUploadRepository.save(fileUpload);

            return fileUpload.getUuid();
        } else
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cannot find user with email: " + uploadedByEmail);
    }






    /**
     * Delete a file in d disk
     */
    public boolean deleteFileInDisk(String filePath) {
        File file = new File(filePath);
        return file.delete();
    }


    /**
     * Getting the fileName by UUID String for reading from disk
     */
    public String getFileNameByUUID(String uuid) {
        String fileName = fileUploadRepository.getFileNameByUUID(uuid);
        log.info(fileName);
        if (fileName == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cannot find file name with uuid: " + uuid);
        return fileName;
    }
}
