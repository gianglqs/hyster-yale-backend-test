package com.hysteryale.service;

import com.hysteryale.repository.upload.FileUploadRepository;
import com.hysteryale.utils.EnvironmentUtils;
import com.hysteryale.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@SpringBootTest
@Slf4j
public class FileUploadServiceTest {
    @Resource
    FileUploadService fileUploadService;
    @Resource
    FileUploadRepository fileUploadRepository;
    @Resource
    AuthenticationManager authenticationManager;

    @BeforeEach
    public void setUp() {
        String username = "admin@gmail.com";
        String password = "123456";

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        password
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    public void testSaveFileUpload() {
        String filePath = "import_files/competitor_pricing/Competitor Pricing Database.xlsx";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String fileUUID = fileUploadService.saveFileUpload(filePath, authentication);
        Assertions.assertNotNull(fileUUID);

        String dbFileName = fileUploadRepository.getFileNameByUUID(fileUUID);
        Assertions.assertEquals(filePath, dbFileName);
    }

    @Test
    public void testSaveFileUploadToDisk() throws Exception {
        String baseFolder = EnvironmentUtils.getEnvironmentValue("upload_files.base-folder");

        org.springframework.core.io.Resource fileResource = new ClassPathResource("import_files/novo/SN_AUD.xlsx");
        Assertions.assertNotNull(fileResource);

        MultipartFile file = new MockMultipartFile(
                "file",
                fileResource.getFilename(),
                MediaType.MULTIPART_FORM_DATA_VALUE,
                fileResource.getInputStream()
        );

        String filePath = fileUploadService.saveFileUploadToDisk(file);
        String encodedFileName = FileUtils.encoding(file.getOriginalFilename());

        Assertions.assertTrue(filePath.contains(baseFolder));
        Assertions.assertTrue(filePath.contains(encodedFileName));
    }

    @Test
    public void testSaveFileUploaded() throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String baseFolder = EnvironmentUtils.getEnvironmentValue("upload_files.base-folder");

        org.springframework.core.io.Resource fileResource = new ClassPathResource("import_files/novo/SN_AUD.xlsx");
        Assertions.assertNotNull(fileResource);

        MultipartFile file = new MockMultipartFile(
                "file",
                fileResource.getFilename(),
                MediaType.MULTIPART_FORM_DATA_VALUE,
                fileResource.getInputStream()
        );
        String excelFileExtension = FileUtils.EXCEL_FILE_EXTENSION;
        String filePath = fileUploadService.saveFileUploaded(file, authentication, baseFolder, excelFileExtension);
        String encodedFileName = FileUtils.encoding(file.getOriginalFilename());

        Assertions.assertTrue(filePath.contains(baseFolder));
        Assertions.assertTrue(filePath.contains(encodedFileName));
    }
}
