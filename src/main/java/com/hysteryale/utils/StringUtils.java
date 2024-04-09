package com.hysteryale.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    /**
     * To check if a password is strong enough to use
     *
     * @param password
     * @return true if password is strong
     */
    public static boolean checkPasswordStreng(String password) {
        boolean containLowerChar = false, containUpperChar = false;
        boolean containDigit = false, containSpecialChar = false, minLength = false;

        String special_chars = "!(){}[]:;<>?,@#$%^&*+=_-~`|./'";
        String strength;

        for (char ch : password.toCharArray()) {
            if (Character.isLowerCase(ch)) {
                containLowerChar = true;
            }
            if (Character.isUpperCase(ch)) {
                containUpperChar = true;
            }
            if (Character.isDigit(ch)) {
                containDigit = true;
            }
            if (special_chars.contains(String.valueOf(ch))) {
                containSpecialChar = true;
            }
        }
        if (password.length() >= 8) {
            minLength = true;
        }

        // if all the conditions passed then password is strong
        return minLength && containDigit && containUpperChar && containSpecialChar && containLowerChar;
    }

    public static boolean isSeries(String series) {
        String regex = "[A-Z][A-Z0-9]{3}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(series);
        return matcher.matches();
    }

    public static boolean compareString(String str1, String str2) {
        if (!str1.contains("-"))
            return str1.equals(str2);
        return compareStringWithRange(str1, str2);
    }

    public static boolean compareStringWithRange(String strHasRange, String str) {

        String regexHasRage = "^([A-Z]+)(\\d+(\\.\\d+)?)-(\\d+(\\.\\d+)?)([A-Z0-9]+)$";
        String regexHasNoRage = "^([A-Z]+)(\\d+(\\.\\d+)?)([A-Z0-9]+)$";

        Matcher matcher1 = Pattern.compile(regexHasRage).matcher(strHasRange);
        Matcher matcher2 = Pattern.compile(regexHasNoRage).matcher(str);

        if (matcher1.matches() && matcher2.matches()) {
            // has range
            String preChar1 = matcher1.group(1);
            double rangeStart1 = Double.parseDouble(matcher1.group(2));
            double rangeEnd1 = Double.parseDouble(matcher1.group(4));
            String suffixChar1 = matcher1.group(6);

            //has no range
            String preChar2 = matcher2.group(1);
            double number2 = Double.parseDouble(matcher2.group(2));
            String suffixChar2 = matcher2.group(4);

            // compare
            if (!preChar1.equals(preChar2) || !suffixChar1.equals(suffixChar2))
                return false;

            return number2 >= rangeStart1 && number2 <= rangeEnd1;
        }

        return false;
    }

    public static Integer isNumber(String str) {
        String regex = "\\d+";
        Matcher matcher = Pattern.compile(regex).matcher(str);
        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(0));
        } else {
            return null;
        }
    }

}
