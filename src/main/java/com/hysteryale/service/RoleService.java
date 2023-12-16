package com.hysteryale.service;

import com.hysteryale.model.Role;
import com.hysteryale.repository.RoleRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class RoleService {
    @Resource
    RoleRepository roleRepository;

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Role getRoleByRowName(String roleName){
        return roleRepository.findByRoleName(roleName);
    }
}
