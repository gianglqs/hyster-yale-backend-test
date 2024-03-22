package com.hysteryale.Beans;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hysteryale.exception.InvalidFolderException;
import com.hysteryale.model.json.MessageJSON;
import com.hysteryale.utils.EnvironmentUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableScheduling
public class LocaleBean {

    private Map<String, MessageJSON> locale = new HashMap<>();

    @Bean
    public Map<String, MessageJSON> getMessageFromJSONFile() throws IOException {
        loadMessage();
        return locale;
    }

    @Scheduled(fixedRate = 10000)
    private void reloadMessage() throws IOException {
        loadMessage();
    }

    private void loadMessage() throws IOException {
        String baseFolderLocale = EnvironmentUtils.getEnvironmentValue("locale.base-folder");
        String folderMessagePath = baseFolderLocale + EnvironmentUtils.getEnvironmentValue("locale.message");

        File messageFolder = new File(folderMessagePath);
        if (!messageFolder.exists())
            throw new FileNotFoundException("Not found folder " + folderMessagePath);

        if (!messageFolder.isDirectory())
            throw new InvalidFolderException(folderMessagePath + " is not a Folder");

        Map<String, MessageJSON> tempLocale = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<MessageJSON> typeReference = new TypeReference<>() {
        };

        File[] files = messageFolder.listFiles();
        for (File file : files) {
            MessageJSON messageJSON = mapper.readValue(file, typeReference);
            String fileName = FilenameUtils.removeExtension(file.getName());
            tempLocale.put(fileName, messageJSON);
        }
        System.out.println(tempLocale);
        locale = tempLocale;

    }

}
