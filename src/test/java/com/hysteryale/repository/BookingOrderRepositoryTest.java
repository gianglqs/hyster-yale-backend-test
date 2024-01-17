package com.hysteryale.repository;

import com.hysteryale.model.BookingOrder;
import com.hysteryale.repository.bookingorder.BookingOrderRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@DataJpaTest
public class BookingOrderRepositoryTest {

    @Resource
    BookingOrderRepository bookingOrderRepository;

    @Resource
    TestEntityManager entityManager;


    @Test
    void getBookingOrderByOrderNo() {
        String orderNo = "H54334A";
        BookingOrder booking = new BookingOrder(orderNo, "QUOCBAO", "A3C4");
        entityManager.persist(booking);
        Optional<BookingOrder> retrievedBooking = bookingOrderRepository.getBookingOrderByOrderNo(orderNo);

        Assertions.assertTrue(retrievedBooking.isPresent());
        Assertions.assertEquals(retrievedBooking.get(), booking);
    }

    @Test
    void getAllDealerName() {
        List<String> listDealerName = bookingOrderRepository.getAllDealerName();
        Assertions.assertEquals(listDealerName.size(), 15);
        Assertions.assertNotEquals(listDealerName.size(), 2);

    }

    @Test
    void getAllModel() {
        List<String> listModel = bookingOrderRepository.getAllModel();
        Assertions.assertEquals(listModel.size(), 33);
        Assertions.assertNotEquals(listModel.size(), 5);
    }

    @Test
    void getOrderForOutline() {
        List<BookingOrder> getListOutLineActualNoneFilter = bookingOrderRepository.getOrderForOutline(null, null, null, null, null, null, null, null, null, null, PageRequest.of(0, 100));
        Assertions.assertEquals(getListOutLineActualNoneFilter.size(), 35);

        List<BookingOrder> getListOutLineActualWithFilterByRegion_Pacific = bookingOrderRepository.getOrderForOutline(List.of("Pacific"), null, null, null, null, null, null, null, null, null, PageRequest.of(0, 100));
        Assertions.assertEquals(getListOutLineActualWithFilterByRegion_Pacific.size(), 6);

        List<BookingOrder> getListOutLineActualWithFilterByPlant_Greenville = bookingOrderRepository.getOrderForOutline(null, List.of("Greenville"), null, null, null, null, null, null, null, null, PageRequest.of(0, 100));
        Assertions.assertEquals(getListOutLineActualWithFilterByPlant_Greenville.size(), 3);

        List<BookingOrder> getListOutLineActualWithFilterByMetaSeries_826 = bookingOrderRepository.getOrderForOutline(null, null, List.of("826"), null, null, null, null, null, null, null, PageRequest.of(0, 100));
        Assertions.assertEquals(getListOutLineActualWithFilterByMetaSeries_826.size(), 1);

        List<BookingOrder> getListOutLineActualWithFilterByClass_Class2 = bookingOrderRepository.getOrderForOutline(null, null, null, List.of("Class 2"), null, null, null, null, null, null, PageRequest.of(0, 100));
        Assertions.assertEquals(getListOutLineActualWithFilterByClass_Class2.size(), 7);

        List<BookingOrder> getListOutLineActualWithFilterByModel_FBR18SZ = bookingOrderRepository.getOrderForOutline(null, null, null, null, List.of("FBR18SZ"), null, null, null, null, null, PageRequest.of(0, 100));
        Assertions.assertEquals(getListOutLineActualWithFilterByModel_FBR18SZ.size(), 1);
    }

    @Test
    void getSumAllOrderForOutline() {
        List<BookingOrder> listOutLineActual = bookingOrderRepository.getSumAllOrderForOutline(null, null, null, null, null, null, null, null, null, null);
        System.out.println(listOutLineActual.get(0).toString());
        Assertions.assertEquals(listOutLineActual.size(), 1);
        Assertions.assertEquals(listOutLineActual.get(0).getQuantity(), 70);
    }

}
