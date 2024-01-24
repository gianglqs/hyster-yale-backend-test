package com.hysteryale.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class EnvironmentUtilsTest {
    @Test
    public void testGetEnvironmentValue() {
        String propertyKey = "server.port";
        Assertions.assertEquals("8080", EnvironmentUtils.getEnvironmentValue(propertyKey));

        propertyKey = "spring.datasource.driverClassName";
        Assertions.assertEquals("org.h2.Driver", EnvironmentUtils.getEnvironmentValue(propertyKey));
    }

    @Test
    public void testGetEnvironmentValue_notFoundPropertyKey() {
        String propertyKey = "abc123123";
        Assertions.assertNull(EnvironmentUtils.getEnvironmentValue(propertyKey));
    }
}
