/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.utils;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class CurrencyFormatUtils {

    public static final DecimalFormat decimalFormatFourDigits = new DecimalFormat("0.0000");
    public static final DecimalFormat decimalFormatTwoDigits = new DecimalFormat("0.00");
    public static final DecimalFormat decimalFormatThreeDigits = new DecimalFormat("0.000");


    /**
     * Format decimal values to 2 or 4 digits     *
     */
    public static double formatDoubleValue(double original, DecimalFormat decimalFormat){
        //as default, we always round up
        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
        return Double.parseDouble(decimalFormat.format(original));
    }

}
