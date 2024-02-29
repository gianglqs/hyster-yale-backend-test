package com.hysteryale.service;

import com.hysteryale.exception.CanNotUpdateException;
import com.hysteryale.model.User;
import com.hysteryale.model.upload.FileUpload;
import com.hysteryale.model.upload.UpdateHistory;
import com.hysteryale.repository.upload.FileUploadRepository;
import com.hysteryale.repository.upload.UpdateHistoryRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UpdateHistoryService {

    @Resource
    FileUploadRepository fileUploadRepository;

    @Resource
    UpdateHistoryRepository updateHistoryRepository;

    @Resource
    UserService userService;

    public void handleUpdatedSuccessfully(String fileName, String model, Authentication authentication) throws CanNotUpdateException {
        UpdateHistory updateHistory = new UpdateHistory();
        if (fileName != null) {
            Optional<FileUpload> fileUploadOptional = fileUploadRepository.getFileUploadByFileName(fileName);
            if (fileUploadOptional.isEmpty())
                throw new CanNotUpdateException("Can not update time updated data");
            updateHistory.setFileUpload(fileUploadOptional.get());
        }

        String updatedByEmail = authentication.getName();
        Optional<User> optionalUpdatedBy = userService.getActiveUserByEmail(updatedByEmail);
        if (optionalUpdatedBy.isPresent()) {
            User updatedBy = optionalUpdatedBy.get();
            updateHistory.setUser(updatedBy);
        }

        updateHistory.setModelType(model);
        updateHistory.setTime(LocalDateTime.now());
        updateHistory.setSuccess(true);

        updateHistoryRepository.save(updateHistory);
    }
}
