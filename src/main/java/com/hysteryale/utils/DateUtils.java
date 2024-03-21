package com.hysteryale.utils;

import com.hysteryale.exception.CannotExtractDateException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DateUtils {
    public static HashMap<String, Integer> monthMap = new HashMap<>() {{
        put("Jan", 0);
        put("Feb", 1);
        put("Mar", 2);
        put("Apr", 3);
        put("May", 4);
        put("Jun", 5);
        put("Jul", 6);
        put("Aug", 7);
        put("Sep", 8);
        put("Oct", 9);
        put("Nov", 10);
        put("Dec", 11);
    }};

    public static String[] getAllMonthsAsString() {
        String[] monthsOfYear = {"Apr", "Feb", "Jan", "May", "Aug", "Jul", "Jun", "Mar", "Sep", "Oct", "Nov", "Dec"};
        return monthsOfYear;
    }

    public static Month getMonth(String monthString) {
        switch (monthString) {
            case "Jan":
                return Month.JANUARY;
            case "Feb":
                return Month.FEBRUARY;
            case "Mar":
                return Month.MARCH;
            case "Apr":
                return Month.APRIL;
            case "May":
                return Month.MAY;
            case "Jun":
                return Month.JUNE;
            case "Jul":
                return Month.JULY;
            case "Aug":
                return Month.AUGUST;
            case "Sep":
                return Month.SEPTEMBER;
            case "Oct":
                return Month.OCTOBER;
            case "Nov":
                return Month.NOVEMBER;
            case "Dec":
                return Month.DECEMBER;
        }

        throw new IllegalArgumentException(monthString + "is not valid");
    }

    public static Month getMonth(int monthInt) {
        switch (monthInt) {
            case 1:
                return Month.JANUARY;
            case 2:
                return Month.FEBRUARY;
            case 3:
                return Month.MARCH;
            case 4:
                return Month.APRIL;
            case 5:
                return Month.MAY;
            case 6:
                return Month.JUNE;
            case 7:
                return Month.JULY;
            case 8:
                return Month.AUGUST;
            case 9:
                return Month.SEPTEMBER;
            case 10:
                return Month.OCTOBER;
            case 11:
                return Month.NOVEMBER;
            case 12:
                return Month.DECEMBER;
        }

        throw new IllegalArgumentException(monthInt + "is not valid");
    }

    public static LocalDate extractDate(String fileName) {
        String dateRegex = "\\d{2}_\\d{2}_\\d{4}";
        Matcher m = Pattern.compile(dateRegex).matcher(fileName);
        LocalDate date = null;
        if (m.find()) {
            String dateString = m.group();
            date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("dd_LL_yyyy"));
        } else {
            dateRegex = "\\d{4}";
            m = Pattern.compile(dateRegex).matcher(fileName);
            if (m.find()) {
                int year = Integer.parseInt(m.group());
                //if file name contains the month
                String monthRegrex = "\\b(?:Jan(?:uary)?|Feb(?:ruary)?|...|Dec(?:ember)?) (?:19[7-9]\\d|2\\d{3})(?=\\D|$)\n";
                Matcher monthMatcher = Pattern.compile(monthRegrex).matcher(fileName);
                if (monthMatcher.find()) {
                    String month = String.valueOf(getMonth(monthMatcher.group()));
                    date = LocalDate.of(year, getMonth(month), 1);
                }
            }
        }

        return date;
    }

    public static int extractYear(String fileName) throws CannotExtractDateException {
        String dateRegex = "\\d{4}";
        Matcher m = Pattern.compile(dateRegex).matcher(fileName);
        if (m.find()) {
            return Integer.parseInt(m.group());
        }
        throw new CannotExtractDateException("Can not extract Year from file name: '" + fileName + "'");
    }

    public static String convertLocalDateTimeToString(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ISO_DATE_TIME);
    }

    public static LocalDateTime getLastUpdatedTime(List<LocalDateTime> times) {
        LocalDateTime lastTime = null;
        for (LocalDateTime time : times) {
            if (lastTime == null || time.isAfter(lastTime))
                lastTime = time;
        }
        return lastTime;
    }

}
