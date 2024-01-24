package com.hysteryale.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Month;

public class DateUtilsTest {

    @Test
    public void testGetMonthByString() {
        String monthString = "Jan";
        Assertions.assertEquals(Month.JANUARY, DateUtils.getMonth(monthString));

        monthString = "Dec";
        Assertions.assertEquals(Month.DECEMBER, DateUtils.getMonth(monthString));
    }

    @Test
    public void testGetMonthByString_notValidString() {
        String monthString = "sdlfhsdfkj";

        IllegalArgumentException exception =
                Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> DateUtils.getMonth(monthString));

        Assertions.assertEquals(monthString + "is not valid", exception.getMessage());
    }

    @Test
    public void testGetMonthByInt() {
        int monthInt = 5;
        Assertions.assertEquals(Month.MAY, DateUtils.getMonth(monthInt));

        monthInt = 8;
        Assertions.assertEquals(Month.AUGUST, DateUtils.getMonth(monthInt));
    }

    @Test
    public void testGetMonthByInt_notValid() {
        int monthInt = 123;

        IllegalArgumentException exception =
                Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> DateUtils.getMonth(monthInt)
                );

        Assertions.assertEquals(monthInt + "is not valid", exception.getMessage());
    }
}
