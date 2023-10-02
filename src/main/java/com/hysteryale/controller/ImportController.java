package com.hysteryale.controller;

import com.hysteryale.service.*;
import com.hysteryale.service.impl.MarginAnalystServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;

@RestController
public class ImportController {
    @Resource
    MetaSeriesService metaSeriesService;
    @Resource
    APICDealerService apicDealerService;
    @Resource
    APACSerialService apacSerialService;
    @Resource
    BookingOrderService bookingOrderService;
    @Resource
    AOPMarginService aopMarginService;
    @Resource
    CurrencyService currencyService;
    @Resource
    ExchangeRateService exchangeRateService;
    @Resource
    CostUpliftService costUpliftService;

    @Resource
    MarginAnalystService marginAnalystService;

    String curencyFolder = "import_files/currency_exchangerate";

    @PostMapping(path = "/import")
    void importData() throws IOException, IllegalAccessException, ParseException {
        //metaSeriesService.importMetaSeries();
        //apicDealerService.importAPICDealer();
        //apacSerialService.importAPACSerial();
        //aopMarginService.importAOPMargin();
        bookingOrderService.importOrder();
        //currencyService.importCurrencies(curencyFolder);
        //exchangeRateService.importExchangeRate();
        //costUpliftService.importCostUplift();
        //partService.importPart();
        //marginAnalystService.importMarginAnalystData();
    }
}
