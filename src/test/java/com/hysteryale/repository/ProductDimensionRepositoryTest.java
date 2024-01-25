package com.hysteryale.repository;

import com.hysteryale.model.ProductDimension;
import com.hysteryale.repository.ProductDimensionRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@DataJpaTest
public class ProductDimensionRepositoryTest {

    @Resource
    ProductDimensionRepository productDimensionRepository;

    //@Test
    void findAll(){
        List<ProductDimension> getAll = productDimensionRepository.findAll();
        Assertions.assertNotEquals(getAll.size(), 0);
    }

    //@Test
    void findByMetaSeries(){
        Optional<ProductDimension> findByMetaSeries = productDimensionRepository.findByMetaSeries("3C4");
        List<ProductDimension> findAll = productDimensionRepository.findAll();
        Assertions.assertEquals(findByMetaSeries.isPresent(), true);
    }

}
