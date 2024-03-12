package com.hysteryale.service;

import com.hysteryale.exception.CanNotUpdateException;
import com.hysteryale.model.User;
import com.hysteryale.model.filters.AdminFilter;
import com.hysteryale.model.upload.FileUpload;
import com.hysteryale.repository.upload.FileUploadRepository;
import com.hysteryale.utils.EnvironmentUtils;
import com.hysteryale.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
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

        File file = new File("/" + baseFolder + "/" + encodedFileName);
        if (file.createNewFile()) {
            log.info("File " + encodedFileName + " created");
            multipartFile.transferTo(file);
            return "/" + baseFolder + "/" + encodedFileName;
        } else {
            log.info("Can not create new file: " + encodedFileName);
            throw new Exception("Can not create new file: " + encodedFileName);
        }
    }

    /**
     * @param multipartFile
     * @param authentication baseFolderUpload : /uploadFiles
     * @param targetFolder   :  /booked, /shipment, ....
     * @param extensionFile  : XLSX,...
     * @param screen         : booked, shipment,....
     * @return
     * @throws Exception
     */
    public String saveFileUploaded(MultipartFile multipartFile, Authentication authentication, String targetFolder, String extensionFile, String screen) throws Exception {
        String baseFolder = EnvironmentUtils.getEnvironmentValue("public-folder");
        String baseFolderUpload = EnvironmentUtils.getEnvironmentValue("upload_files.base-folder");
        String savedFolder = baseFolderUpload + targetFolder;
        Date uploadedTime = new Date();
        String strUploadedTime = (new SimpleDateFormat("ddMMyyyyHHmmss").format(uploadedTime));
        String encodedFileName = FileUtils.encoding(Objects.requireNonNull(multipartFile.getOriginalFilename())) + "_" + strUploadedTime + extensionFile;

        File file = new File(baseFolder + savedFolder + encodedFileName);
        if (file.createNewFile()) {
            log.info("File " + encodedFileName + " created");
            multipartFile.transferTo(file);
            saveFileUpLoadIntoDB(authentication, encodedFileName, screen, savedFolder + encodedFileName);
            return encodedFileName;
        } else {
            log.info("Can not create new file: " + encodedFileName);
            throw new Exception("Can not create new file: " + encodedFileName);
        }
    }

    public String upLoadImage(MultipartFile multipartFile, String targetFolder, Authentication authentication, String screen) throws Exception {
        String baseFolder = EnvironmentUtils.getEnvironmentValue("public-folder");
        String uploadFolder = baseFolder + targetFolder;

        Date uploadedTime = new Date();
        String strUploadedTime = (new SimpleDateFormat("ddMMyyyyHHmmss").format(uploadedTime));
        String encodedFileName = FileUtils.encoding(Objects.requireNonNull(multipartFile.getOriginalFilename())) + "_" + strUploadedTime + FileUtils.IMAGE_FILE_EXTENSION;
        String fileUploadedPath = uploadFolder + encodedFileName;
        File file = new File(fileUploadedPath);
        if (file.createNewFile()) {
            log.info("File " + encodedFileName + " created");
            multipartFile.transferTo(file);

            saveFileUpLoadIntoDB(authentication, encodedFileName, screen, targetFolder + encodedFileName);

            // check the file is an image
            if (!FileUtils.isImageFile(fileUploadedPath)) {
                log.info("File is not an image: " + encodedFileName);
                handleUpdatedFailure(encodedFileName, "File is not an image");
                throw new Exception("File is not an image: " + multipartFile.getOriginalFilename());
            }
            return encodedFileName;
        } else {
            log.info("Can not create new file: " + encodedFileName);
            throw new Exception("Can not save file: " + multipartFile.getOriginalFilename());
        }

    }

    public String upLoadImage(String filePath, String targetFolder, Authentication authentication, String screen) throws Exception {

        File imageFile = new File(filePath);
        if (!imageFile.exists())
            throw new FileNotFoundException("Could not found Image with path: " + filePath);

        String baseFolder = EnvironmentUtils.getEnvironmentValue("public-folder");
        String uploadFolder = baseFolder + targetFolder;

        Date uploadedTime = new Date();
        String strUploadedTime = (new SimpleDateFormat("ddMMyyyyHHmmss").format(uploadedTime));
        String encodedFileName = FileUtils.encoding(imageFile.getName()) + "_" + strUploadedTime + FileUtils.IMAGE_FILE_EXTENSION;
        String fileUploadedPath = uploadFolder + encodedFileName;
        File file = new File(fileUploadedPath);
        if (file.createNewFile()) {
            log.info("File " + encodedFileName + " created");

            compressedImage(imageFile, fileUploadedPath);
            saveFileUpLoadIntoDB(authentication, encodedFileName, screen, targetFolder + encodedFileName);

            // check the file is an image
            if (!FileUtils.isImageFile(fileUploadedPath)) {
                log.info("File is not an image: " + encodedFileName);
                throw new Exception("File is not an image: " + imageFile.getName());
            }
            return encodedFileName;
        } else {
            log.info("Can not create new file: " + encodedFileName);
            throw new Exception("Can not save file: " + imageFile.getName());
        }

    }



//    public static void main(String[] args) throws IOException {
//
////        Tinify.setKey("L7MczDTDq2NMGwDgHJxcXL76S02JWgv6");
////
////        for (int i = 2; i < 10; i++) {
////            Tinify.fromFile("/home/oem/Documents/" + i + ".png").toFile("/home/oem/Documents/" + (i + 1) + ".png");
////        }
//        File file = new File("/home/oem/Downloads/Product Photos/Class 2/R1.25-1.8EX(W)2_C915_R2.00-3.00EX2_B925.jpg");
//
//        compressedFile(file, "/home/oem/Downloads/test-compressed-image/1.png", 1f);
//    }


    private  void compressedImage(File input, String des) throws IOException {
        File fileCompressed = new File(des);
        BufferedImage bimg = ImageIO.read(input);
        int width = bimg.getWidth();
        int height = bimg.getHeight();
        int maxWidth = 800;
        int maxHeight = 800;

        if (width >= height) {
            double rate = width / maxWidth;
            if (rate > 1) {
                width = maxWidth;
                height = (int) (height * (1 / rate));
            }
        } else {
            double rate = height / maxHeight;
            if (rate > 1) {
                height = maxHeight;
                width = (int) (width * (1 / rate));
            }
        }

        Thumbnails.of(input)
                .outputQuality(0.5)
                .size(width, height)
                .toFile(fileCompressed);

    }


    private String saveFileUpLoadIntoDB(Authentication authentication, String encodeFileName, String screen, String path) {
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
            fileUpload.setFileName(encodeFileName);
            fileUpload.setScreen(screen);
            fileUpload.setPath(path);
            fileUpload.setLoading(true);
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

    public void handleUpdatedSuccessfully(String fileName) throws CanNotUpdateException {
        if (fileName != null) {
            Optional<FileUpload> fileUploadOptional = fileUploadRepository.getFileUploadByFileName(fileName);
            if (fileUploadOptional.isEmpty())
                throw new CanNotUpdateException("Can not update status of file uploaded");

            FileUpload fileUpload = fileUploadOptional.get();
            fileUpload.setUploadedTime(LocalDateTime.now());
            fileUpload.setSuccess(true);
            fileUpload.setLoading(false);

            fileUploadRepository.save(fileUpload);
        }
    }

    public void handleUpdatedFailure(String fileName, String message) throws CanNotUpdateException {
        if (fileName != null) {
            Optional<FileUpload> fileUploadOptional = fileUploadRepository.getFileUploadByFileName(fileName);
            if (fileUploadOptional.isEmpty())
                throw new CanNotUpdateException("Can not update status of file uploaded");

            FileUpload fileUpload = fileUploadOptional.get();
            fileUpload.setMessage(message);
            fileUpload.setLoading(false);
            fileUploadRepository.save(fileUpload);
        }
    }


    public Map<String, Object> getDataForTable(AdminFilter filter, int pageNo, int perPage) {
        Map<String, Object> result = new HashMap<>();

        Pageable pageable = PageRequest.of(pageNo == 0 ? pageNo : pageNo - 1, perPage == 0 ? 100 : perPage);

        List<FileUpload> getFileUploadByFilter = fileUploadRepository.getFileUploadByFilter(convertFilter(filter.getFilter()), pageable);

        for (FileUpload fileUpload : getFileUploadByFilter) {
            fileUpload.getUploadedBy().setPassword(null);
            fileUpload.getUploadedBy().setLastLogin(null);
            fileUpload.setFileName(decodeFileName(fileUpload.getFileName()));
            //  fileUpload.getUploadedBy().setRole(null);
        }

        int countAll = fileUploadRepository.countAll(convertFilter(filter.getFilter()));

        result.put("listFileUploaded", getFileUploadByFilter);
        result.put("serverTimeZone", TimeZone.getDefault().getID());
        result.put("totalItems", countAll);
        return result;
    }

    private String convertFilter(String filter) {
        if (filter == null || filter.trim().equals(""))
            return null;
        return filter;
    }

    public String decodeFileName(String fileName) {
        String fileNameEncode = fileName.split("_")[0];
        return new String(Base64.getDecoder().decode(fileNameEncode));
    }


}
