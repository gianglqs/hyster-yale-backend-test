/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.utils;

import java.math.RoundingMode;

public class PagingnatorUtils{

    /**
     * given: numberOfItemsPerPage
     * given: totalNumberOfItems
     * return the number of pages
     */
    public static int calculateNumberOfPages(int numberOfItemsPerPage, int totalNumberOfItems){
        Double result =  ((double)totalNumberOfItems / (double)numberOfItemsPerPage);
        return (int)Math.ceil(result) ;
    }

}
