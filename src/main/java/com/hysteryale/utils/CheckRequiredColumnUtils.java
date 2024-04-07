package com.hysteryale.utils;

import com.hysteryale.exception.MissingColumnException;

import java.util.ArrayList;
import java.util.List;

public class CheckRequiredColumnUtils {

    // REQUIRED COLUMN
    public static final List<String> SHIPMENT_REQUIRED_COLUMN = List.of("Order number", "Series", "Model",
            "Serial Number", "Quantity", "Revenue", "Revenue - Other", "Discounts", "Additional Discounts",
            "Cash Discounts", "Cost of Sales", "Dealer Commisions", "Warranty", "COS - Other", "End Customer Name","Ship-to Country Code","Created On");

    public static final List<String> DEALEAR_REQUIRED_COLUMN = List.of("MkgGroup", "BilltoCode", "DealerDivison", "DealerName","TerritoryManager","AreaBusinesssDirector","BigTruckManager","AftermarketManager","AftermarketTechnicalServiceManager");

    public static final  List<String>  FORECAST_REQUIRED_COLUMN =List.of("Series /Segments","Description","Plant","Brand");

    public static final List<String> COMPETITOR_REQUIRED_COLUMN = List.of("Table Title", "Country", "Group", "Brand", "Region", "Class", "Origin", "Market Share", "Price (USD)");

    public static final List<String> PRODUCT_APAC_SERIAL_COLUMN = List.of("Hyster", "Plant", "Class", "Model", "Yale");
    public static final List<String> NOVO_REQUIRED_COLUMN = List.of("Quote Number:", "#", "Series Code", "Model Code", "Part Number", "Part Description", "List Price", "Net Price Each");
    public static final List<String> MACRO_REQUIRED_COLUMN = List.of(); // TODO:Nhan will complete it
    public static final List<String> EXCHANGE_RATE_REQUIRED_COLUMN = List.of(); // TODO:Nhan will complete it
    public static final List<String> PART_REQUIRED_COLUMN = List.of("Model", "Part Number", "Order Number", "Currency", "Quote Number", "Quoted Quantity", "Series",
            "Part Number", "ListPrice", "Model", "Class", "Region", "Discount", "Dealer", "Net Price", "Customer Price", "Ext Customer Price", "Order Number");
    public static final List<String> BOOKING_REQUIRED_COLUMN = List.of("ORDERNO", "SERIES", "BILLTO", "MODEL", "REGION", "DATE", "DEALERNAME", "CTRYCODE", "TRUCKCLASS", "ORDERTYPE", "DEALERPO");
    public static final List<String> BOOKING_FPA_REQUIRED_COLUMN = List.of("Order No.", "Revised Net Sales", "Revised Cost", "Inc_Cst#");
    public static final List<String> BOOKING_COST_DATA_REQUIRED_COLUMN = List.of("Order", "TOTAL MFG COST Going-To");
    public static final List<String> PRODUCT_DIMENSION_REQUIRED_COLUMN = List.of("Metaseries", "Model", "Segment", "Family_Name", "Truck_Type");
    public static final List<String> AOP_MARGIN_REQUIRED_COLUMN = List.of("MetaSeries", "Plant", "Region", "std,margin,%");


    // REQUIRED SHEET
    public static final String BOOKING_REQUIRED_SHEET = "NOPLDTA.NOPORDP,NOPLDTA.>Sheet1";
    public static final String BOOKING_COST_DATA_REQUIRED_SHEET = "Cost Data";
    public static final String SHIPMENT_REQUIRED_SHEET = "Sheet1";
    public static final String BOOKING_FPA_REQUIRED_SHEET = "Booking Margin Database";
    public static final String PRODUCT_APAC_SERIAL_REQUIRED_SHEET = "Master Summary";
    public static final String PRODUCT_DIMENSION_REQUIRED_SHEET = "Data";
    public static final String PART_REQUIRED_SHEET = "Export";
    public static final String AOP_MARGIN_REQUIRED_SHEET = "aop,dn,margin,%";
    public static final String RESIDUAL_VALUE_REQUIRED_SHEET = "RV APIC";


    public static void checkRequiredColumn(List<String> currentColumns, List<String> requiredColumns, String fileUUID) throws MissingColumnException {
        List<String> listMissingColumn = new ArrayList<>();
        for (String requiredColumn : requiredColumns) {
            if (requiredColumn.contains(","))
                continue; // it is regex -> it will handle in each department
            if (!currentColumns.contains(requiredColumn)) {
                listMissingColumn.add(requiredColumn);
            }
        }
        if (!listMissingColumn.isEmpty()) {
            throw new MissingColumnException(listMissingColumn.toString(), fileUUID);
        }
    }

}
