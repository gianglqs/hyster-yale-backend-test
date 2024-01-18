package com.hysteryale.service;

import com.hysteryale.exception.MissingColumnException;
import com.hysteryale.model.BookingOrder;
import com.hysteryale.repository.ProductDimensionRepository;
import com.hysteryale.repository.bookingorder.BookingOrderRepository;
import com.hysteryale.utils.EnvironmentUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;




public class BookingOrderServiceTest {
    @InjectMocks
    BookingOrderService bookingOrderService;
    @Mock
    BookingOrderRepository bookingOrderRepository;

    @Mock
    ProductDimensionRepository productDimensionRepository;

    @Mock
    ProductDimensionService productDimensionService;

    @Mock
    RegionService regionService;

    @Mock
    Environment environment;

    @Mock
    EnvironmentUtils environmentUtils;



    private AutoCloseable autoCloseable;
    List<BookingOrder> bookingOrderList = new ArrayList<>();

    @BeforeEach
    void setUp() throws IOException, MissingColumnException {
        autoCloseable = MockitoAnnotations.openMocks(this);
        createMockedBookingOrderList();
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }


    void createMockedBookingOrderList() throws IOException, MissingColumnException {
        InputStream is = new FileInputStream("import_files/booked/BOOKED ORDER January 2023 Final_.xlsx");
        XSSFWorkbook workbook = new XSSFWorkbook(is);

        Sheet orderSheet = workbook.getSheet("NOPLDTA.NOPORDP,NOPLDTA.>Sheet1");
        HashMap<String, Integer> ORDER_COLUMNS_NAME = new HashMap<>();
        for (Row row : orderSheet) {
            if (row.getRowNum() == 0)
                bookingOrderService.getOrderColumnsName(row, ORDER_COLUMNS_NAME);
            else if (!row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().isEmpty()
                    && row.getRowNum() > 0) {
                BookingOrder newBookingOrder = bookingOrderService.mapExcelDataIntoOrderObject(row, ORDER_COLUMNS_NAME);
                bookingOrderList.add(newBookingOrder);
            }
        }
    }

    void createMockedRegionList(){

    }

    @Test
    void testGetAllFilesInFolder() {
        // GIVEN
        String folderPath = "import_files/booking";
        int expectedListSize = 16;

        List<String> fileList = bookingOrderService.getAllFilesInFolder(folderPath);
        Assertions.assertEquals(expectedListSize, fileList.size());
    }

    @Test
    void testGetAllBookingOrders() {

        // WHEN
        Mockito.when(bookingOrderRepository.findAll()).thenReturn(bookingOrderList);
        List<BookingOrder> result = bookingOrderService.getAllBookingOrders();

        // THEN
        Mockito.verify(bookingOrderRepository).findAll();
        Assertions.assertFalse(result.isEmpty());
    }


    void testImportBookingOrder() throws MissingColumnException, IOException, IllegalAccessException {
      //  bookingOrderService.importOrder();
    }


    @Test
    void checkOldDate() {
        assertEquals(true, bookingOrderService.checkOldData("Apr", "2023"));
        assertEquals(false, bookingOrderService.checkOldData("Sep", "2023"));
        assertEquals(false, bookingOrderService.checkOldData("Nov", "2023"));

    }
}
