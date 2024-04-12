/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.repository;

import com.hysteryale.model.dealer.DealerProduct;
import com.hysteryale.model.dealer.DealerProductId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DealerProductRepository extends JpaRepository<DealerProduct, DealerProductId> {

    @Query("SELECT new com.hysteryale.model.dealer.DealerProduct (dp.product, COUNT(dp.quantity), AVG(dp.netRevenue)) " +
            "FROM DealerProduct dp " +
            "WHERE (:modelCode IS NULL OR dp.product.modelCode LIKE CONCAT ('%', :modelCode, '%')) " +
            "AND ((:classes) IS NULL OR dp.product.clazz.clazzName IN (:classes)) " +
            "AND ((:segment) IS NULL OR dp.product.segment IN (:segment)) " +
            "AND ((:family) IS NULL OR dp.product.family IN (:family)) " +
            "AND ((:series) IS NULL OR dp.product.series IN (:series)) " +
            "AND ((:dealerId) IS NULL OR dp.dealer.id = (:dealerId)) " +
            "GROUP BY dp.product ORDER BY COUNT(dp.quantity) DESC")
    Page<DealerProduct> getDealerProductByFilters(@Param("modelCode") String modelCode,
                                                  @Param("classes") List<String> classes,
                                                  @Param("segment") List<String> segment,
                                                  @Param("family") List<String> family,
                                                  @Param("series") List<String> series,
                                                  @Param("dealerId") Integer dealerId,
                                                  Pageable pageable);
}
