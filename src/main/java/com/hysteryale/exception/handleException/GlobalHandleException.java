package com.hysteryale.exception.handleException;

import com.hysteryale.exception.*;
import com.hysteryale.response.ErrorResponse;
import com.hysteryale.service.FileUploadService;
import com.hysteryale.utils.LocaleUtils;
import io.sentry.Sentry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

@ControllerAdvice
@DependsOn("getMessageFromJSONFile")
@Slf4j
public class GlobalHandleException extends ResponseEntityExceptionHandler {

    @Resource
    private HashMap<String, HashMap<String, HashMap<String, String>>> messagesMap;

    @Resource
    private FileUploadService fileUploadService;

    protected void logInfo(String message, Exception... exception) {
        log.info(message, exception);
        logWithSentry(message, exception);
    }

    protected void logDebug(String message, Exception... exception) {
        log.debug(message, exception);
        logWithSentry(message, exception);
    }

    protected void logError(String message, Exception... exception) {
        log.error(message, exception);
        logWithSentry(message, exception);
    }

    protected void logWarning(String message, Exception... exception) {
        log.warn(message, exception);
        logWithSentry(message, exception);
    }

    private void logWithSentry(String message, Exception... exception) {
        Sentry.captureMessage(message);
        if (exception != null && exception.length > 0) {
            Sentry.captureException(exception[0]);
        }
    }

    @ExceptionHandler(MissingColumnException.class)
    public ResponseEntity<ErrorResponse> handleMissingColumnException(MissingColumnException exception, WebRequest request) throws CanNotUpdateException {
        ServletRequestAttributes attributes = (ServletRequestAttributes) request;
        HttpServletRequest servletRequest = attributes.getRequest();
        String locale = servletRequest.getHeader("locale");
        String baseMessage = LocaleUtils.getMessage(messagesMap, locale, "failure", "missing_column");
        StringBuilder stringBuilder = new StringBuilder(baseMessage);
        stringBuilder.insert(baseMessage.length() - 1, exception.getMessage());
        fileUploadService.handleUpdatedFailure(exception.getFileUUID(), stringBuilder.toString());
        logError(stringBuilder.toString(), exception);
        return new ResponseEntity<>(new ErrorResponse(stringBuilder.toString()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MissingSheetException.class)
    public ResponseEntity<ErrorResponse> handleMissingSheetException(MissingSheetException exception, WebRequest request) throws CanNotUpdateException {
        ServletRequestAttributes attributes = (ServletRequestAttributes) request;
        HttpServletRequest servletRequest = attributes.getRequest();
        String locale = servletRequest.getHeader("locale");
        String baseMessage = LocaleUtils.getMessage(messagesMap, locale, "failure", "missing_sheet");
        StringBuilder stringBuilder = new StringBuilder(baseMessage);
        stringBuilder.insert(baseMessage.length() - 1, exception.getMessage());
        fileUploadService.handleUpdatedFailure(exception.getFileUUID(), stringBuilder.toString());
        logError(stringBuilder.toString(), exception);
        return new ResponseEntity<>(new ErrorResponse(stringBuilder.toString()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BlankSheetException.class)
    public ResponseEntity<ErrorResponse> handleBlankSheetException(BlankSheetException exception, WebRequest request) throws CanNotUpdateException {
        ServletRequestAttributes attributes = (ServletRequestAttributes) request;
        HttpServletRequest servletRequest = attributes.getRequest();
        String locale = servletRequest.getHeader("locale");
        String baseMessage = LocaleUtils.getMessage(messagesMap, locale, "failure", "blank_sheet");
        StringBuilder stringBuilder = new StringBuilder(baseMessage);
        stringBuilder.insert(baseMessage.length() - 1, exception.getMessage());
        fileUploadService.handleUpdatedFailure(exception.getFileUUID(), stringBuilder.toString());
        logError(stringBuilder.toString(), exception);
        return new ResponseEntity<>(new ErrorResponse(stringBuilder.toString()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidFileNameException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFileNameException(InvalidFileNameException exception, WebRequest request) throws CanNotUpdateException {
        ServletRequestAttributes attributes = (ServletRequestAttributes) request;
        HttpServletRequest servletRequest = attributes.getRequest();
        String locale = servletRequest.getHeader("locale");
        String baseMessage = LocaleUtils.getMessage(messagesMap, locale, "failure", "invalid_fileName");
        StringBuilder stringBuilder = new StringBuilder(baseMessage);
        stringBuilder.insert(baseMessage.length() - 1, exception.getMessage());
        fileUploadService.handleUpdatedFailure(exception.getFileUUID(), stringBuilder.toString());
        logError(stringBuilder.toString(), exception);
        return new ResponseEntity<>(new ErrorResponse(stringBuilder.toString()), HttpStatus.NOT_FOUND);
    }


}
