package com.hysteryale.service;

import com.hysteryale.exception.CanNotUpdateException;
import com.hysteryale.model.User;
import com.hysteryale.model.upload.FileUpload;
import com.hysteryale.repository.upload.FileUploadRepository;
import com.hysteryale.utils.EnvironmentUtils;
import com.hysteryale.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.annotation.Resource;
import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

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
     * @param authentication contains upload person's email
     * @return UUID string
     */
    public String saveFileUpload(String filePath, Authentication authentication) {
        // Find who uploads this file
        String uploadedByEmail = authentication.getName();
        Optional<User> optionalUploadedBy = userService.getActiveUserByEmail(uploadedByEmail);

        if (optionalUploadedBy.isPresent()) {
            User uploadedBy = optionalUploadedBy.get();
            FileUpload fileUpload = new FileUpload();

            // generate random UUID
            fileUpload.setUuid(UUID.randomUUID().toString());
            fileUpload.setUploadedBy(uploadedBy);
            fileUpload.setUploadedTime(LocalDateTime.now());

            // append suffix into fileName
            fileUpload.setFileName(filePath);

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

    // TODO: check path file
    public String saveFileUploaded(MultipartFile multipartFile, Authentication authentication, String baseFolder, String extensionFile, String modelType) throws Exception {

        Date uploadedTime = new Date();
        String strUploadedTime = (new SimpleDateFormat("ddMMyyyyHHmmss").format(uploadedTime));
        String encodedFileName = FileUtils.encoding(Objects.requireNonNull(multipartFile.getOriginalFilename())) + "_" + strUploadedTime + extensionFile;

        File file = new File(baseFolder + "/" + encodedFileName);
        if (file.createNewFile()) {
            log.info("File " + encodedFileName + " created");
            multipartFile.transferTo(file);
            saveFileUpLoadIntoDB(authentication, encodedFileName, modelType);
            return encodedFileName;
        } else {
            log.info("Can not create new file: " + encodedFileName);
            throw new Exception("Can not create new file: " + encodedFileName);
        }
    }

    public String upLoadImage(MultipartFile multipartFile, String targetFolder, Authentication authentication, String modelType) throws Exception {
        String baseFolder = EnvironmentUtils.getEnvironmentValue("public-folder");
        String uploadFolder = baseFolder + targetFolder;

        Date uploadedTime = new Date();
        String strUploadedTime = (new SimpleDateFormat("ddMMyyyyHHmmss").format(uploadedTime));
        String encodedFileName = FileUtils.encoding(Objects.requireNonNull(multipartFile.getOriginalFilename())) + "_" + strUploadedTime + "png";
        String fileUploadedPath = uploadFolder + encodedFileName;
        File file = new File(fileUploadedPath);
        if (file.createNewFile()) {
            log.info("File " + encodedFileName + " created");
            multipartFile.transferTo(file);

            saveFileUpLoadIntoDB(authentication, encodedFileName, modelType);

            // check the file is an image
            if (!FileUtils.isImageFile(fileUploadedPath)) {
                log.info("File is not an image: " + encodedFileName);
                throw new Exception("File is not an image: " + multipartFile.getOriginalFilename());
            }
            return encodedFileName;
        } else {
            log.info("Can not create new file: " + encodedFileName);
            throw new Exception("Can not save file: " + multipartFile.getOriginalFilename());
        }

    }

    private String saveFileUpLoadIntoDB(Authentication authentication, String encodeFileName, String modelType) {
        String uploadedByEmail = authentication.getName();
        Optional<User> optionalUploadedBy = userService.getActiveUserByEmail(uploadedByEmail);

        if (optionalUploadedBy.isPresent()) {
            User uploadedBy = optionalUploadedBy.get();
            FileUpload fileUpload = new FileUpload();

            // generate random UUID
            fileUpload.setUuid(UUID.randomUUID().toString());
            fileUpload.setUploadedBy(uploadedBy);
            fileUpload.setUploadedTime(LocalDateTime.now());

            fileUpload.setModelType(modelType);

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

    public void updateUploadedSuccessfully(String fileName) throws CanNotUpdateException {
        Optional<FileUpload> fileUploadOptional = fileUploadRepository.getFileUploadByFileName(fileName);
        if (fileUploadOptional.isEmpty())
            throw new CanNotUpdateException("Can not update time updated data");
        FileUpload fileUpload = fileUploadOptional.get();
        fileUpload.setUploadedTime(LocalDateTime.now());
        fileUpload.setSuccess(true);
        fileUploadRepository.save(fileUpload);
    }
}
