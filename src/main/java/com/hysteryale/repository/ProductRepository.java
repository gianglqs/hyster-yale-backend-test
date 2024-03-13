package com.hysteryale.repository;

import com.hysteryale.model.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Transactional
public interface ProductRepository extends JpaRepository<Product, String> {
    @Query("SELECT DISTINCT a.plant FROM Product a WHERE a.plant IS NOT NULL AND a.plant <> ''")
    List<String> getPlants();

    @Query("SELECT a FROM Product a WHERE a.series = ?1")
    Optional<Product> findByMetaSeries(String metaSeries);

    @Query("SELECT DISTINCT substring(a.series, 2, 4) FROM Product a")
    List<String> getAllMetaSeries();

    @Query("SELECT DISTINCT a.clazz FROM Product a WHERE a.clazz IS NOT NULL AND a.clazz <> ''")
    List<String> getAllClass();

    @Query("SELECT DISTINCT p.segment FROM Product p WHERE p.segment IS NOT NULL AND p.segment <> ''")
    List<String> getAllSegments();

    @Query("SELECT DISTINCT p.modelCode FROM Product p")
    List<String> getAllModel();



    @Query(value = "SELECT p.plant FROM product p WHERE p.series = :series LIMIT 1", nativeQuery = true)
    String getPlantBySeries(@Param("series") String series);


    @Query("SELECT p FROM Product p WHERE " +
            "((:modelCode) IS Null OR LOWER(p.modelCode) LIKE LOWER(CONCAT('%', :modelCode, '%')))" +
            " AND ((:plants) IS NULL OR p.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(p.series, 2,3) IN (:metaSeries))" +
            " AND ((:classes) IS NULL OR p.clazz IN (:classes))" +
            " AND ((:segments) IS NULL OR p.segment IN (:segments))" +
            " AND ((:brands) IS NULL OR p.brand IN (:brands))" +
            " AND ((:truckTypes) IS NULL OR p.truckType IN (:truckTypes))" +
            " AND ((:family) IS NULL OR p.family IN (:family)) ORDER BY p.modelCode")
    List<Product> getDataByFilter(String modelCode,
                                  List<String> plants,
                                  List<String> metaSeries,
                                  List<String> classes,
                                  List<String> segments,
                                  List<String> brands,
                                  List<String> truckTypes,
                                  List<String> family,
                                  Pageable pageable
    );

    @Query("SELECT COUNT(p) FROM Product p WHERE " +
            "((:modelCode) IS Null OR LOWER(p.modelCode) LIKE LOWER(CONCAT('%', :modelCode, '%')))" +
            " AND ((:plants) IS NULL OR p.plant IN (:plants))" +
            " AND ((:metaSeries) IS NULL OR SUBSTRING(p.series, 2,3) IN (:metaSeries))" +
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

    @Query("SELECT DISTINCT p.brand FROM Product p WHERE p.brand IS NOT NULL AND p.brand <> ''")
    List<String> getAllBrands();

    @Query("SELECT DISTINCT p.family FROM Product p WHERE p.family IS NOT NULL AND p.family <> ''")
    List<String> getAllFamily();

    @Query("SELECT DISTINCT p.truckType FROM Product p WHERE p.truckType IS NOT NULL AND p.truckType <> ''")
    List<String> getAllTruckType();

    @Query("SELECT p FROM Product p WHERE p.modelCode = :modelCode")
    Optional<Product> getProductByModelCode(String modelCode);

    @Query("SELECT p FROM Product p WHERE p.modelCode = :modelCode AND p.series = :series")
    Optional<Product> findByModelCodeAndSeries(String modelCode, String series);

    @Query(value = "SELECT * FROM product p WHERE substring(p.series, 2, 4) = :metaSeries LIMIT 1", nativeQuery = true)
    Product getProductByMetaSeries(@Param("metaSeries") String metaSeries);

    @Query(value = "SELECT p.plant FROM product p WHERE p.model_code = :model_code LIMIT 1", nativeQuery = true)
    String getPlantByModelCode(@Param("model_code") String modelCode);

    @Query("SELECT p FROM Product p WHERE p.modelCode = :modelCode AND substring(p.series, 2, 4) = :metaSeries")
    List<Product> findByModelCodeAndMetaSeries(String modelCode, String metaSeries);

    @Query(value = "SELECT m.latest_modified_at FROM product m WHERE m.latest_modified_at is not null ORDER BY m.latest_modified_at DESC LIMIT 1", nativeQuery = true)
    Optional<LocalDateTime> getLatestUpdatedTime();
}
