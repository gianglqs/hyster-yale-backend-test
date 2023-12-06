package com.hysteryale.controller;

import com.hysteryale.exception.MissingColumnException;
import com.hysteryale.service.*;
import com.hysteryale.service.marginAnalyst.MarginAnalystMacroService;
import com.hysteryale.service.marginAnalyst.MarginAnalystService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;

@RestController
public class ImportController {

    @Resource
    APICDealerService apicDealerService;
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

    @Resource
    ProductDimensionService productDimensionService;

    @Resource
    PartService partService;
    @Resource
    MarginAnalystMacroService marginAnalystMacroService;

    @Resource
    ImportService importService;

    @PostMapping(path = "/importAllData")
    void importAllData() throws IOException, IllegalAccessException, MissingColumnException {
        importApicDealer();
        importCurrencies();
        importPart();
        importAOPMargin();
        importProductDimension();
        importOrder();
        importExchangeRate();
        importMarginAnalystMacro();
        importMarginAnalystData();
        importCompetitorPricing();
        importShipment();
    }

    @PostMapping(path = "/importApicDealer")
    void importApicDealer() throws IOException, IllegalAccessException {
        apicDealerService.importAPICDealer();
    }

    @PostMapping(path = "/importCurrencies")
    void importCurrencies() throws IOException {
        currencyService.importCurrencies();
    }

    @PostMapping(path = "/importPart")
    void importPart() throws IOException {
        partService.importPart();
    }

    @PostMapping(path = "/importAOPMargin")
    void importAOPMargin() throws IOException, IllegalAccessException {
        aopMarginService.importAOPMargin();
    }

    @PostMapping(path = "/importProductDimension")
    void importProductDimension() throws IOException, IllegalAccessException {
        productDimensionService.importProductDimension();
    }

    @PostMapping(path = "/importOrder")
    void importOrder() throws IOException, IllegalAccessException, MissingColumnException {
        bookingOrderService.importOrder();
    }

    @PostMapping(path = "/importExchangeRate")
    void importExchangeRate() throws IOException {
        exchangeRateService.importExchangeRate();
    }

    @PostMapping(path = "/importCostUplift")
    void importCostUplift() throws IOException {
        costUpliftService.importCostUplift();
    }

    @PostMapping(path = "/importMarginAnalystMacro")
    void importMarginAnalystMacro() {
        marginAnalystMacroService.importMarginAnalystMacro();
    }

    @PostMapping(path = "/importMarginAnalystData")
    void importMarginAnalystData() throws IOException {
        marginAnalystService.importMarginAnalystData();
    }

    @PostMapping(path = "/importCompetitorPricing")
    void importCompetitorPricing() throws IOException {
        importService.importCompetitorPricing();
    }

    @PostMapping(path = "/importShipment")
    void importShipment() throws IOException, MissingColumnException {
        importService.importShipment();
    }


}