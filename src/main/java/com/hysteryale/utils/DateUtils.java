package com.hysteryale.utils;

import com.hysteryale.exception.CannotExtractDateException;
import com.hysteryale.model.enums.FrequencyImport;

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
        monthString = monthString.toLowerCase();
        switch (monthString) {
            case "jan":
                return Month.JANUARY;
            case "feb":
                return Month.FEBRUARY;
            case "mar":
                return Month.MARCH;
            case "apr":
                return Month.APRIL;
            case "may":
                return Month.MAY;
            case "jun":
                return Month.JUNE;
            case "jul":
                return Month.JULY;
            case "aug":
                return Month.AUGUST;
            case "sep":
                return Month.SEPTEMBER;
            case "oct":
                return Month.OCTOBER;
            case "nov":
                return Month.NOVEMBER;
            case "dec":
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

    public static LocalDate extractMonthAndYear(String fileName) {
        //regex for type: Jar 2034
        String regex = "\\b(Jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)\\b.*\\b(\\d{4})\\b";

        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(fileName);

        if (matcher.find()) {
            String monthStr = matcher.group(1);
            int year = Integer.parseInt(matcher.group(2));
            Month month = getMonth(monthStr);
            return LocalDate.of(year, month, 1);
        } else {
            //regex for type:  03_18_2034
            regex = "\\d{2}_\\d{2}_\\d{4}";
            pattern = Pattern.compile(regex);
            matcher = pattern.matcher(fileName);
            if (matcher.find()) {
                String dateString = matcher.group();
                return LocalDate.parse(dateString, DateTimeFormatter.ofPattern("MM_dd_yyyy"));
            }
            return null;
        }
    }

    public static void main(String[] args) throws CannotExtractDateException {
        System.out.println(extractMonthAndYear("Cost_Data_11_19_2023_11_01_37 fake.xlsx"));
    }


    public static int extractYear(String fileName) throws CannotExtractDateException {
        String dateRegex = "\\d{4}";
        Matcher m = Pattern.compile(dateRegex).matcher(fileName);
        if (m.find()) {
            return Integer.parseInt(m.group());
        }
        throw new CannotExtractDateException("Can not extract Year from file name: '" + fileName + "'");
    }

    public static LocalDate extractYearFromFileName(String fileName) throws CannotExtractDateException {
        int year = extractYear(fileName);
        return LocalDate.of(year, 1, 1);
    }

    public static String convertLocalDateTimeToString(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ISO_DATE_TIME);
    }

    public static String convertLocalDateToString(LocalDate localDate, String frequency){
        if(frequency.equals(FrequencyImport.ANNUAL.getValue())){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy");
            return localDate.format(formatter);
        }
        if(frequency.equals(FrequencyImport.MONTHLY.getValue())){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");
            return localDate.format(formatter);
        }
        return "Ad hoc import";
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
