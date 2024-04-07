package com.hysteryale.controller;

import com.hysteryale.exception.*;
import com.hysteryale.exception.CompetitorException.MissingForecastFileException;
import com.hysteryale.service.*;
import com.hysteryale.service.marginAnalyst.MarginAnalystMacroService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;

@RestController
public class ImportController {

    @Resource
    APICDealerService apicDealerService;
    @Resource
    BookingService bookingService;
    @Resource
    AOPMarginService aopMarginService;
    @Resource
    CurrencyService currencyService;
    @Resource
    ExchangeRateService exchangeRateService;
    @Resource
    CostUpliftService costUpliftService;

    @Resource
    ProductService productService;

    @Resource
    PartService partService;
    @Resource
    MarginAnalystMacroService marginAnalystMacroService;

    @Resource
    ImportService importService;

    @PostMapping(path = "/importAllData")
    void importAllData() throws IOException, IllegalAccessException, MissingColumnException, MissingSheetException, BlankSheetException, IncorectFormatCellException, ExchangeRatesException, InvalidFileNameException, MissingForecastFileException {
        importApicDealer();
        importCurrencies();
        importPart();
        importAOPMargin();
        importProductDimension();
        importOrder();
        importExchangeRate();
        importMarginAnalystMacro();
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
    void importPart() throws IOException, MissingColumnException, MissingSheetException, ExchangeRatesException, InvalidFileNameException, IncorectFormatCellException {
        partService.importPart();
    }

    @PostMapping(path = "/importAOPMargin")
    void importAOPMargin() throws IOException, IllegalAccessException, MissingColumnException {
        aopMarginService.importAOPMargin();
    }

    @PostMapping(path = "/importProductDimension")
    void importProductDimension() throws IOException, MissingColumnException {
        productService.importProductDimension();
    }

    @PostMapping(path = "/importOrder")
    void importOrder() throws IOException, IllegalAccessException, MissingColumnException, MissingSheetException, BlankSheetException {
        bookingService.importOrder();
    }

    @PostMapping(path = "/importExchangeRate")
    void importExchangeRate() throws IOException, IncorectFormatCellException, ExchangeRatesException {
        exchangeRateService.importExchangeRate();
    }

    @PostMapping(path = "/importCostUplift")
    void importCostUplift() throws IOException {
        costUpliftService.importCostUplift();
    }

    @PostMapping(path = "/importMarginAnalystMacro")
    void importMarginAnalystMacro() throws InvalidFileNameException {
        marginAnalystMacroService.importMarginAnalystMacro();
    }


    @PostMapping(path = "/importCompetitorPricing")
    void importCompetitorPricing() throws IOException, MissingForecastFileException {
        importService.importCompetitorPricing();
    }

    @PostMapping(path = "/importShipment")
    void importShipment() throws IOException, MissingColumnException, MissingSheetException, BlankSheetException {
        importService.importShipment();
    }

    /**
     * Extract Product (Model Code) from Part (in power bi files)
     */
    @PostMapping(path = "/importProductFromPart")
    void importProductFromPart() throws IOException {
        productService.extractProductFromPart();
    }


}