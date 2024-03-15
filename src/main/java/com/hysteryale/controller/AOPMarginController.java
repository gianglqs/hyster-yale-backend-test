package com.hysteryale.controller;

import com.hysteryale.response.ResponseObject;
import com.hysteryale.service.AOPMarginService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@RestController()
@RequestMapping("aopmargin")
public class AOPMarginController {

    @Resource
    private AOPMarginService aopMarginService;

    @PostMapping(path = "/importData", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ResponseObject> importProduct(@RequestParam("file") MultipartFile file, Authentication authentication) throws Exception {

       aopMarginService.importAOPMargin(file, authentication);

        return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("Import data successfully", null));
    }
}
