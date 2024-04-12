/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.utils;

import java.util.Arrays;
import java.util.List;

public class PlantUtil {
    public static List<String> getUSPlant() {
        String[] arrPlant = {"Greenville", "Berea", "Ramos", "Craigavon", "Nijmegen", "Masate", "Brazil"};
        List<String> listPlant = Arrays.asList(arrPlant);
        return listPlant;
    }
}
