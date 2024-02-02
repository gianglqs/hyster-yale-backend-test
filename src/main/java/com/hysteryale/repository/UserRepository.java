package com.hysteryale.repository;

import com.hysteryale.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface UserRepository extends PagingAndSortingRepository<User, Integer> {
    @Query("SELECT a FROM User a WHERE LOWER(a.email) = LOWER(?1)")
    Optional<User> getUserByEmail(String email);
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN TRUE ELSE FALSE END FROM User a WHERE LOWER(a.email) = LOWER(?1)")
    boolean isEmailExisted(String email);
    @Query("SELECT a FROM User a WHERE LOWER(a.email) = LOWER(?1) AND a.isActive = true")
    Optional<User> getActiveUserByEmail(String email);
    @Query("SELECT a FROM User a WHERE CONCAT(LOWER(a.name), LOWER(a.email)) LIKE CONCAT('%', LOWER(?1), '%')")
    Page<User> searchUser(String searchString, Pageable pageable);
}
