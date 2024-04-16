/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.model.payLoad;

import com.hysteryale.model.Booking;
import com.hysteryale.model.BookingFPA;
import com.hysteryale.model.Shipment;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingMarginTrialTestPayLoad {

    private Booking booking;
    private Shipment shipment;
    private BookingFPA bookingFPA;
}
