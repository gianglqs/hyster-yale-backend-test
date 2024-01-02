package com.hysteryale.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import javax.annotation.Resource;

@DataJpaTest
public class ImportBookingTest {
    @Resource
    BookingOrderService bookingOrderService;

    @Test
    void testImport(){}
}
