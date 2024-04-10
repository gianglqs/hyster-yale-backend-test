package com.hysteryale.service.impl;

import com.hysteryale.exception.CannotExtractDateException;
import com.hysteryale.model.ImportTracking;
import com.hysteryale.model.ModelType;
import com.hysteryale.model.enums.FrequencyImport;
import com.hysteryale.model.enums.ImportTrackingStatus;
import com.hysteryale.model.enums.ModelTypeEnum;
import com.hysteryale.model.payLoad.ImportTrackingPayload;
import com.hysteryale.model.upload.FileUpload;
import com.hysteryale.repository.ModelTypeRepository;
import com.hysteryale.repository.upload.FileUploadRepository;
import com.hysteryale.repository.upload.ImportTrackingRepository;
import com.hysteryale.service.ImportTrackingService;
import com.hysteryale.utils.DateUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;

@Service
public class ImportTrackingServiceImp implements ImportTrackingService {

    @Resource
    private FileUploadRepository fileUploadRepository;

    @Resource
    private ImportTrackingRepository importTrackingRepository;

    @Resource
    private ModelTypeRepository modelTypeRepository;

    @Override
    public Map<String, Object> getDataByFilter(LocalDate time) {
        Map<String, Object> result = new HashMap<>();
        List<ImportTracking> importTrackingList = importTrackingRepository.findByMonthAndYear(
                Objects.requireNonNullElseGet(time, LocalDate::now));

        importTrackingList = removeNotLastImport(importTrackingList);
        List<ImportTrackingPayload> importTrackingPayloadList = adjustModel(importTrackingList);

        result.put("listImportTracking", importTrackingPayloadList);
        result.put("serverTimeZone", TimeZone.getDefault().getID());
        return result;
    }

    private List<ImportTracking> removeNotLastImport(List<ImportTracking> importTrackings) {
        Map<String, ImportTracking> groupModelTypeMap = new HashMap<>();

        for (ImportTracking importTracking : importTrackings) {
            String modelType = importTracking.getFileUpload().getModelType().getType();
            if (groupModelTypeMap.containsKey(modelType)) {
                ImportTracking importTrackingMap = groupModelTypeMap.get(modelType);
                if (importTracking.getFileUpload().getUploadedTime().isAfter(importTrackingMap.getFileUpload().getUploadedTime())) {
                    groupModelTypeMap.put(modelType, importTracking);
                }
            } else {
                groupModelTypeMap.put(modelType, importTracking);
            }
        }
        return new ArrayList<>(groupModelTypeMap.values());
    }

    private List<ImportTrackingPayload> adjustModel(List<ImportTracking> importTrackings) {


        List<ImportTrackingPayload> result = new ArrayList<>();
        List<String> listCompletedModel = new ArrayList<>();
        for (ImportTracking importTracking : importTrackings) {
            ImportTrackingPayload importTrackingPayload = new ImportTrackingPayload();

            importTrackingPayload.setFileType(importTracking.getFileUpload().getModelType().getType());

            importTrackingPayload.setImportBy(importTracking.getFileUpload().getUploadedBy().getName());

            importTrackingPayload.setImportAt(DateUtils.convertLocalDateTimeToString(importTracking.getFileUpload().getUploadedTime()));

            String encodeFileName = importTracking.getFileUpload().getFileName();
            String encodeFileNameWithoutDateTime = encodeFileName.split("_")[0];
            byte[] originalFileName = Base64.getDecoder().decode(encodeFileNameWithoutDateTime);
            importTrackingPayload.setFileName(new String(originalFileName));

            importTrackingPayload.setStatus(ImportTrackingStatus.COMPLETED.getValue());

            String frequency = importTracking.getFileUpload().getModelType().getFrequency().getType();
            importTrackingPayload.setFrequency(frequency);

            importTrackingPayload.setBelongToTime(DateUtils.convertLocalDateToString(importTracking.getBelongToTime(), frequency));

            result.add(importTrackingPayload);
            listCompletedModel.add(importTracking.getFileUpload().getModelType().getType());
        }


        List<ModelType> listAllModelType = modelTypeRepository.findAll();
        onlySelectElementForImportTracking(listAllModelType);
        List<ModelType> missingModels = getListMissingModel(listAllModelType, listCompletedModel);
        insertImportTrackingMissingModel(result, missingModels);

        return result;
    }

    private void onlySelectElementForImportTracking(List<ModelType> modelTypes) {
        modelTypes.removeIf(modelType -> (ModelTypeEnum.listModelTypeNotInImportTracking.contains(modelType.getType())));
    }

    private List<ModelType> getListMissingModel(List<ModelType> listALlModelType, List<String> listModelCompleted) {
        List<ModelType> result = new ArrayList<>();
        for (ModelType modelType : listALlModelType) {
            if (!listModelCompleted.contains(modelType.getType())) {
                result.add(modelType);
            }
        }
        return result;
    }


    private void insertImportTrackingMissingModel(List<ImportTrackingPayload> importTrackingPayloads, List<ModelType> missingModels) {

        for (ModelType missingModel : missingModels) {
            ImportTrackingPayload importTrackingPayload = new ImportTrackingPayload();
            importTrackingPayload.setFrequency(missingModel.getFrequency().getType());
            importTrackingPayload.setFileType(missingModel.getType());
            importTrackingPayload.setStatus(ImportTrackingStatus.UNFINISHED.getValue());
            importTrackingPayloads.add(importTrackingPayload);
        }
    }


    @Override
    public void updateImport(String fileUUID, String originalFileName, FrequencyImport frequency) throws CannotExtractDateException {
        Optional<FileUpload> fileUpload = fileUploadRepository.getFileUploadByUUID(fileUUID);
        if (fileUpload.isEmpty())
            throw new NoSuchElementException("Not found FileUpload with fileUUID: " + fileUUID);

        LocalDate date = null;
        if (frequency.equals(FrequencyImport.MONTHLY)) {
            //get month and year
            date = DateUtils.extractMonthAndYear(originalFileName);

        } else if (frequency.equals(FrequencyImport.ANNUAL)) {
            date = DateUtils.extractYearFromFileName(originalFileName);
        }

        ImportTracking importTracking = new ImportTracking();
        importTracking.setBelongToTime(date);
        importTracking.setFileUpload(fileUpload.get());

        importTrackingRepository.save(importTracking);
    }
}
