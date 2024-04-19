package com.hysteryale.controller;
import com.hysteryale.model.InterestRate;
import com.hysteryale.model.User;
import com.hysteryale.model.filters.InterestRateFilterModel;
import com.hysteryale.service.InterestRateService;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
public class InterestRateController {
    @Resource
    InterestRateService interestRateService;

    @PostMapping("/testImport")
    public ResponseEntity<String> getDatafromInterestRateFile() {
        try {
            interestRateService.importInterestRateFromFile("import_files/interest_rate/API_FR.INR.RINR_DS2_en_excel_v2_119.xls");
            return ResponseEntity.status(HttpStatus.OK).body("Data imported successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error occurred while importing data: " + e.getMessage());
        }
    }

//    @GetMapping("/getAllInterestRate")
//    public ResponseEntity<List<InterestRate>> getAllInterestRate(InterestRateFilterModel filterModel) throws Exception {
//        try{
//            List<InterestRate> interestRates=interestRateService.getAllInterestRate();
//            if(interestRates!=null){
//                return ResponseEntity.status(HttpStatus.OK).body(interestRates);
//            }else{
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
//            }
//        }catch(Exception e){
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
//        }
//    }

    @GetMapping("/getAllInterestRate")
    public Map<String, Object> getAllInterestRate(
                                                 @RequestParam(defaultValue = "") String search,
                                                 @RequestParam(defaultValue = "1") int pageNo,
                                                 @RequestParam(defaultValue = "100") int perPage) throws Exception {
        InterestRateFilterModel filterModel = new InterestRateFilterModel();
        filterModel.setBankName(search);
        filterModel.setPageNo(pageNo);
        filterModel.setPerPage(perPage);
        return  interestRateService.getListInterestRateByFilter(filterModel);

    }


//    @GetMapping("/getInterestRateByBankName")
//    public ResponseEntity<List<InterestRate>> getInterestRateByBankName(@RequestParam("bankName") String bankName) {
//        try {
//            List<InterestRate> interestRates = interestRateService.getInterestRateByBankName(bankName);
//            if (interestRates != null) {
//                return ResponseEntity.status(HttpStatus.OK).body(interestRates);
//            } else {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
//            }
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
//        }
//    }

}
