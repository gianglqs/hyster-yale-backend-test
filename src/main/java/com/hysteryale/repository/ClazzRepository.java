package com.hysteryale.repository;

import com.hysteryale.model.Clazz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ClazzRepository extends JpaRepository<Clazz, Integer> {

    @Query("SELECT c FROM Clazz c WHERE c.clazzName = ?1")
    Optional<Clazz> getClazzByClazzName(String clazzName);

    @Query("SELECT c.clazzName FROM Clazz c WHERE c.clazzName != '' AND c.clazzName IS NOT NULL ORDER BY c.clazzName ASC")
    List<String> getAllClasses();
}
