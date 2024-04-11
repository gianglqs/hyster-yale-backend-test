package com.hysteryale.service;

import com.hysteryale.exception.BlankSheetException;
import com.hysteryale.exception.MissingSheetException;
import com.hysteryale.model.ImportTracking;
import com.hysteryale.model.Product;
import com.hysteryale.model.ResidualValue;
import com.hysteryale.model.enums.ImportFailureType;
import com.hysteryale.model.enums.ImportTrackingStatus;
import com.hysteryale.model.enums.ModelTypeEnum;
import com.hysteryale.model.importFailure.ImportFailure;
import com.hysteryale.model.payLoad.ImportTrackingPayload;
import com.hysteryale.repository.ProductRepository;
import com.hysteryale.repository.ResidualValueRepository;
import com.hysteryale.service.impl.ImportTrackingServiceImp;
import com.hysteryale.service.impl.ResidualValueServiceImp;
import com.hysteryale.utils.CheckRequiredColumnUtils;
import com.hysteryale.utils.EnvironmentUtils;
import com.hysteryale.utils.FileUtils;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SpringBootTest
public class ImportTrackingServiceTest {

    @Resource
    private ImportTrackingService importTrackingService;

    /**
     * {@link com.hysteryale.service.impl.ImportTrackingServiceImp#getDataByFilter(LocalDate)}
     */
    @Test
    public void testGetDataByFilter_withoutFilterDate() {
        Map<String, Object> result = importTrackingService.getDataByFilter(null);

        Assertions.assertEquals(result.size(), 2);

        List<ImportTrackingPayload> importTrackingPayloads = (List<ImportTrackingPayload>) result.get("listImportTracking");
        Assertions.assertEquals(importTrackingPayloads.size(), 14);
        compareDataImportTracking(importTrackingPayloads, true);
    }

    private void compareDataImportTracking(List<ImportTrackingPayload> importTrackingPayloads, boolean withoutDate) {
        if (withoutDate) {
            int countImportTrackingPayloadHaveStatusUnfinished = 0;
            for (ImportTrackingPayload importTrackingPayload : importTrackingPayloads) {
                if (importTrackingPayload.getStatus().equals(ImportTrackingStatus.UNFINISHED.getValue()))
                    countImportTrackingPayloadHaveStatusUnfinished++;
            }
            Assertions.assertEquals(countImportTrackingPayloadHaveStatusUnfinished, 10);
        }else{
            int countImportTrackingPayloadHaveStatusUnfinished = 0;
            for (ImportTrackingPayload importTrackingPayload : importTrackingPayloads) {
                if (importTrackingPayload.getStatus().equals(ImportTrackingStatus.UNFINISHED.getValue()))
                    countImportTrackingPayloadHaveStatusUnfinished++;
            }
            Assertions.assertEquals(countImportTrackingPayloadHaveStatusUnfinished, 9);
        }
    }

    @Test
    public void testGetDataByFilter_withFilterDate() {
        Map<String, Object> result = importTrackingService.getDataByFilter(LocalDate.of(2023, 10, 10));

        Assertions.assertEquals(result.size(), 2);

        List<ImportTrackingPayload> importTrackingPayloads = (List<ImportTrackingPayload>) result.get("listImportTracking");
        Assertions.assertEquals(importTrackingPayloads.size(), 14);
        compareDataImportTracking(importTrackingPayloads, false);

    }


}
