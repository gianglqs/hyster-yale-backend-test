package com.hysteryale.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.util.StringUtil;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class FileUtils {

    /**
     * Return all file names in a folder
     *
     * @param folderPath
     * @return @{@link List}
     */
    public static List<String> getAllFilesInFolder(String folderPath) {
        File file = new File(folderPath);
        List<String> fileNames = new ArrayList<>();

        if (file.isDirectory() && file.exists()) {
            if (file.canRead()) {
                for (File f : file.listFiles()) {
                    fileNames.add(f.getName());
                }
            } else {
                log.info(folderPath + " can't be read");
            }
        } else {
            log.info(folderPath + " does not exist or it is not a folder");
        }

        return new ArrayList<>(fileNames);
    }

    /**
     * Return all file names in a folder
     *
     * @param folderPath
     * @param @{@link    Pattern pattern}
     * @return @{@link List}
     */
    public static List<String> getAllFilesInFolderWithPattern(String folderPath, Pattern pattern) {
        //Pattern pattern = Pattern.compile("^(01. Bookings Register).*(.xlsx)$");

        List<String> fileList = new ArrayList<String>();
        Matcher matcher;
        try {
            DirectoryStream<Path> folder = Files.newDirectoryStream(Paths.get(folderPath));
            for (Path path : folder) {
                matcher = pattern.matcher(path.getFileName().toString());
                if (matcher.matches())
                    fileList.add(path.getFileName().toString());
                else
                    log.error("Wrong formatted file's name: " + path.getFileName().toString());
            }
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        log.info("File list: " + fileList);
        return fileList;
    }

    /**
     * Verify whether the file's name is Excel file or not
     */
    public static boolean isExcelFile(String filePath) throws IOException {
        if (StringUtil.isNotBlank(filePath)) {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath));
            //OLE2 is XLS and OOXML is XLSX
            return (FileMagic.valueOf(bis) == FileMagic.OLE2) || (FileMagic.valueOf(bis) == FileMagic.OOXML);
        } else {
            throw new FileNotFoundException(filePath + "does not exist");
        }
    }

    public static boolean isExcelFile(InputStream is) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(is);

        //OLE2 is XLS and OOXML is XLSX
        return (FileMagic.valueOf(bis) == FileMagic.OLE2) || (FileMagic.valueOf(bis) == FileMagic.OOXML);
    }

    public static boolean isImageFile(String path) {
        File file = new File(path);
        if (!file.exists())
            return false;
        Image img = null;
        try{
            img = ImageIO.read(file);
            return img != null && img.getWidth(null) > 0 && img.getHeight(null) > 0;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveFile(MultipartFile multipartFile, String uploadDirectory) throws IOException {
        String filePath = getPath(uploadDirectory, multipartFile.getOriginalFilename());
        // Ensure the directory exists
        File directory = new File(uploadDirectory);
        if (!directory.exists()) {
            directory.mkdirs(); // Create the directory and any necessary parent directories
        }
        multipartFile.transferTo(new File(filePath));
    }

    /**
     * @param baseFolder
     * @param file       or folder target
     * @return path of file or folder
     */
    public static String getPath(String baseFolder, String file) {
        return baseFolder + File.separator + file;
    }

    public static String encoding(String fileName) {
        //use encoding base64
        return Base64.getEncoder().encodeToString(fileName.getBytes());
    }

    public static String decoding(String encodedString) {
        //use encoding base64
        byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
        return new String(decodedBytes);
    }

    public static boolean checkFileNameValid(MultipartFile multipartFile, String regex) {
        String fileName;
        fileName = multipartFile.getOriginalFilename();
        assert fileName != null;
        return fileName.toLowerCase().contains(regex);
    }
}
