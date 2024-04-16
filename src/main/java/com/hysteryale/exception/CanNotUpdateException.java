/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.exception;

public class CanNotUpdateException extends Exception{
    public CanNotUpdateException(String name){
        super(name);
    }
}
