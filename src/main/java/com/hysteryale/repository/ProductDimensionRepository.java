package com.hysteryale.repository;

import com.hysteryale.model.ProductDimension;
import com.hysteryale.model.Shipment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;

@Transactional
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


    @Query("SELECT p FROM ProductDimension p WHERE " +
            "((:modelCode) IS Null OR p.modelCode = :modelCode )" +
            " AND ((:plants) IS NULL OR p.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(p.metaSeries, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR p.clazz IN (:classes))" +
            " AND ((:segments) IS NULL OR p.segment IN (:segments))" +
            " AND ((:brands) IS NULL OR p.brand IN (:brands))" +
            " AND ((:family) IS NULL OR p.family IN (:family))")
    List<ProductDimension> getDataByFilter(String modelCode,
                                           List<String> plants,
                                           List<String> metaSeries,
                                           List<String> classes,
                                           List<String> segments,
                                           List<String> brands,
                                           List<String> family,
                                           Pageable pageable
    );

    @Query("SELECT COUNT(p) FROM ProductDimension p WHERE " +
            "((:modelCode) IS Null OR p.modelCode = :modelCode )" +
            " AND ((:plants) IS NULL OR p.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(p.metaSeries, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR p.clazz IN (:classes))" +
            " AND ((:segments) IS NULL OR p.segment IN (:segments))" +
            " AND ((:brands) IS NULL OR p.brand IN (:brands))" +
            " AND ((:truckTypes) IS NULL OR p.truckType IN (:truckTypes))" +
            " AND ((:family) IS NULL OR p.family IN (:family))")
    long countAll(String modelCode,
                  List<String> plants,
                  List<String> metaSeries,
                  List<String> classes,
                  List<String> segments,
                  List<String> brands,
                  List<String> truckTypes,
                  List<String> family
    );

    @Query("SELECT p FROM ProductDimension p WHERE p.modelCode = ?1")
    Optional<ProductDimension> findByModelCode(String modelCode);

    @Query("SELECT DISTINCT p.brand FROM ProductDimension p ")
    List<String> getAllBrands();

    @Query("SELECT DISTINCT p.family FROM ProductDimension p ")
    List<String> getAllFamily();

    @Query("SELECT DISTINCT p.truckType FROM ProductDimension p ")
    List<String> getAllTruckType();

    @Modifying
    @Query("UPDATE ProductDimension p SET p.image = :imagePath WHERE p.modelCode = :modelCode")
    void updateImage(String modelCode, String imagePath);

    @Modifying
    @Query("UPDATE ProductDimension p SET p.image = :imagePath, p.description = :description WHERE p.modelCode = :modelCode")
    void updateImageAndDescription(String modelCode, String imagePath, String description);

    @Modifying
    @Query("UPDATE ProductDimension p SET p.description = :description WHERE p.modelCode = :modelCode")
    void updateDescription(String modelCode, String description);
}
