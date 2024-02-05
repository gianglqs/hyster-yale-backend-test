package com.hysteryale.repository;

import com.hysteryale.model.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
public interface ProductRepository extends JpaRepository<Product, String> {
    @Query("SELECT DISTINCT a.plant FROM Product a")
    List<String> getPlants();

    @Query("SELECT a FROM Product a WHERE a.metaSeries = ?1")
    Optional<Product> findByMetaSeries(String metaSeries);

    @Query("SELECT DISTINCT a.metaSeries FROM Product a")
    List<String> getAllMetaSeries();

    @Query("SELECT DISTINCT a.clazz FROM Product a")
    List<String> getAllClass();

    @Query("SELECT DISTINCT p.segment FROM Product p")
    List<String> getAllSegments();

    @Query("SELECT DISTINCT p.modelCode FROM Product p")
    List<String> getAllModel();

    @Query("SELECT p.modelCode FROM Product p WHERE p.metaSeries = :metaSeries")
    Optional<String> getModelByMetaSeries(String metaSeries);

    @Query(value = "SELECT p.plant FROM ProductDimension p WHERE p.metaSeries = :metaSeries LIMIT 1", nativeQuery = true)
    String getPlantByMetaSeries(@Param("metaSeries") String metaSeries);


    @Query("SELECT p FROM Product p WHERE " +
            "((:modelCode) IS Null OR p.modelCode = :modelCode )" +
            " AND ((:plants) IS NULL OR p.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(p.metaSeries, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR p.clazz IN (:classes))" +
            " AND ((:segments) IS NULL OR p.segment IN (:segments))" +
            " AND ((:brands) IS NULL OR p.brand IN (:brands))" +
            " AND ((:family) IS NULL OR p.family IN (:family)) ORDER BY p.modelCode")
    List<Product> getDataByFilter(String modelCode,
                                  List<String> plants,
                                  List<String> metaSeries,
                                  List<String> classes,
                                  List<String> segments,
                                  List<String> brands,
                                  List<String> family,
                                  Pageable pageable
    );

    @Query("SELECT COUNT(p) FROM Product p WHERE " +
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

    @Query("SELECT p FROM Product p WHERE p.modelCode = ?1")
    Optional<Product> findByModelCode(String modelCode);

    @Query("SELECT DISTINCT p.brand FROM Product p ")
    List<String> getAllBrands();

    @Query("SELECT DISTINCT p.family FROM Product p ")
    List<String> getAllFamily();

    @Query("SELECT DISTINCT p.truckType FROM Product p ")
    List<String> getAllTruckType();

    @Query("SELECT p FROM Product p WHERE p.modelCode = :modelCode")
    Optional<Product> getProductByModelCode(String modelCode);

    @Query("SELECT p FROM Product p WHERE p.modelCode = :modelCode AND p.metaSeries = :metaSeries")
    Optional<Product> findByModelCodeAndMetaSeries(String modelCode, String metaSeries);
}
