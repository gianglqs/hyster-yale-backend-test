/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.Beans;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hysteryale.exception.InvalidFolderException;
import com.hysteryale.utils.EnvironmentUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableScheduling
@DependsOn("environmentUtils")
public class LocaleBean {

    private HashMap<String, HashMap<String, HashMap<String, String>>> locale = new HashMap<>();

    @Bean
    public HashMap<String, HashMap<String, HashMap<String, String>>> getMessageFromJSONFile() throws IOException {
        loadMessage();
        return locale;
    }

    @Scheduled(fixedRate = 10000)
    private void reloadMessage() throws IOException {
        loadMessage();
    }

    private void loadMessage() throws IOException {
        String localFolder = "locale";
        File messageFolder = new File(localFolder);
        if (!messageFolder.exists())
            throw new FileNotFoundException("Not found folder " + localFolder);

        if (!messageFolder.isDirectory())
            throw new InvalidFolderException(localFolder + " is not a Folder");


        ObjectMapper mapper = new ObjectMapper();
        TypeReference<Map<String, Object>> typeReference = new TypeReference<>() {
        };
        HashMap<String, HashMap<String, HashMap<String, String>>> tempLocale = new HashMap<>();
        File[] files = messageFolder.listFiles();
        for (File file : files) {
            Map<String, Object> jsonParse = mapper.readValue(file, typeReference);
            Object message = jsonParse.get("message");
            HashMap<String, HashMap<String, String>> messageMapper = (HashMap<String, HashMap<String, String>>) message;
            String fileName = FilenameUtils.removeExtension(file.getName());
            tempLocale.put(fileName, messageMapper);
        }
        locale = tempLocale;
    }


}
