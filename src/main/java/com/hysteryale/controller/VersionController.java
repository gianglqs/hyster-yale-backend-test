/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.controller;

import com.hysteryale.model.versionTag.VersionApp;
import com.hysteryale.repository.VersionAppRepository;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("version")
public class VersionController {

    @Resource
    private VersionAppRepository versionAppRepository;

    @GetMapping("/updateVersion")
    public void updateVersion(@RequestParam String version, @RequestParam String type) {
        VersionApp versionApp = new VersionApp();
        versionApp.setVersion(version);
        versionApp.setType(type);
        versionAppRepository.save(versionApp);
    }

    @GetMapping("/getVersion")
    public Map<String, String> getDataVersion(@RequestParam boolean isNewDesign) {

        Map<String, String> result = new HashMap<>();
        Optional<VersionApp> optionalVersionAppBackend = versionAppRepository.getLatestVersion("backend");
        String versionBackend = "";
        if (optionalVersionAppBackend.isPresent())
            versionBackend = optionalVersionAppBackend.get().getVersion();


        String versionFrontend = "";
        if(isNewDesign){
            Optional<VersionApp> optionalVersionAppFrontend = versionAppRepository.getLatestVersion("frontend_new_design");
            if (optionalVersionAppFrontend.isPresent())
                versionFrontend = optionalVersionAppFrontend.get().getVersion();
        }else{
            Optional<VersionApp> optionalVersionAppFrontend = versionAppRepository.getLatestVersion("frontend");
            if (optionalVersionAppFrontend.isPresent())
                versionFrontend = optionalVersionAppFrontend.get().getVersion();
        }




        result.put("backend", versionBackend);
        result.put("frontend", versionFrontend);
        return result;
    }
}
