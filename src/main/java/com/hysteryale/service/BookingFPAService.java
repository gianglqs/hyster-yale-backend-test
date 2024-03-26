package com.hysteryale.service;

import com.hysteryale.exception.IncorectFormatCellException;
import com.hysteryale.exception.MissingColumnException;
import com.hysteryale.exception.MissingSheetException;
import com.hysteryale.model.BookingFPA;
import com.hysteryale.model.filters.FilterModel;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public interface BookingFPAService {
    void importBookingFPA(InputStream is,String fileUUID) throws IOException, MissingSheetException, MissingColumnException, IncorectFormatCellException;

    Map<String, Object > getBookingMarginTrialTest(FilterModel filter) throws ParseException;

    List<BookingFPA> getBookingFPAByListOrderNo(List<String> orderNos);

    BookingFPA getBookingFPAByOrderNo(List<BookingFPA> bookingFPAs, String orderNo);
}
