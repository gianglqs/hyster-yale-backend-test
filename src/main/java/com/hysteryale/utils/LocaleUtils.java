package com.hysteryale.utils;

import java.util.HashMap;


public class LocaleUtils {

    public static String getMessage(HashMap<String, HashMap<String, HashMap<String, String>>> messageMap, String locale, String type, String key) {
        return messageMap.get(locale).get(type).get(key);
    }
}
