package com.hysteryale.repository;

import com.hysteryale.model.ProductDimension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductDimensionRepository extends JpaRepository<ProductDimension, String> {
    @Query("SELECT DISTINCT a.plant FROM ProductDimension a")
    List<String> getPlants();

    @Query("SELECT a FROM ProductDimension a WHERE a.metaSeries = ?1")
    Optional<ProductDimension> findByMetaSeries(String metaSeries);

    @Query("SELECT DISTINCT a.metaSeries FROM ProductDimension a")
    List<String> getAllMetaSeries();

    @Query("SELECT DISTINCT a.clazz FROM ProductDimension a")
    List<String> getAllClass();

    @Query("SELECT DISTINCT p.segment FROM ProductDimension p")
    List<String> getAllSegments();

    @Query("SELECT DISTINCT p.modelCode FROM ProductDimension p")
    List<String> getAllModel();

    @Query("SELECT p.modelCode FROM ProductDimension p WHERE p.metaSeries = :metaSeries")
    Optional<String> getModelByMetaSeries(String metaSeries);

    @Query("SELECT p.plant FROM ProductDimension p WHERE p.metaSeries = ?1")
    String getPlantByMetaSeries(String metaSeries);
}
