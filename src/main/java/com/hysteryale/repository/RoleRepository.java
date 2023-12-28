package com.hysteryale.repository;

import com.hysteryale.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    @Query("SELECT r FROM Role r WHERE r.roleName = :roleName ")
    Role findByRoleName(String roleName);
}
