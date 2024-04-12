/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.controller;

import com.hysteryale.model.Role;
import com.hysteryale.service.RoleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;

@RestController
public class RoleController {
    @Resource
    RoleService roleService;

    @GetMapping(path = "/roles")
    public Map<String, List<Role>> getAllRoles() {
        Map<String, List<Role>> roles = new HashMap<>();
        roles.put("roles", roleService.getAllRoles());
        return roles;
    }
}
