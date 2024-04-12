package com.hysteryale.exception.handleException;

import com.hysteryale.exception.*;
import com.hysteryale.exception.CompetitorException.CompetitorColorNotFoundException;
import com.hysteryale.exception.CompetitorException.MissingForecastFileException;
import com.hysteryale.exception.UserException.EmailNotFoundException;
import com.hysteryale.exception.UserException.ExistingEmailException;
import com.hysteryale.exception.UserException.PasswordException;
import com.hysteryale.exception.UserException.UserIdNotFoundException;
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
        locale = locale == null ? "en" : locale;
        String baseMessage = LocaleUtils.getMessage(messagesMap, locale, "failure", "missing_column");
        StringBuilder stringBuilder = new StringBuilder(baseMessage);
        stringBuilder.insert(baseMessage.length(), exception.getMessage());
        fileUploadService.handleUpdatedFailure(exception.getFileUUID(), stringBuilder.toString());
        logError(stringBuilder.toString(), exception);
        return new ResponseEntity<>(new ErrorResponse(stringBuilder.toString()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MissingSheetException.class)
    public ResponseEntity<ErrorResponse> handleMissingSheetException(MissingSheetException exception, WebRequest request) throws CanNotUpdateException {
        ServletRequestAttributes attributes = (ServletRequestAttributes) request;
        HttpServletRequest servletRequest = attributes.getRequest();
        String locale = servletRequest.getHeader("locale");
        locale = locale == null ? "en" : locale;
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
        locale = locale == null ? "en" : locale;
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
        locale = locale == null ? "en" : locale;
        String baseMessage = LocaleUtils.getMessage(messagesMap, locale, "failure", "invalid_fileName");
        StringBuilder stringBuilder = new StringBuilder(baseMessage);
        stringBuilder.insert(baseMessage.length() - 1, exception.getMessage());
        fileUploadService.handleUpdatedFailure(exception.getFileUUID(), stringBuilder.toString());
        logError(stringBuilder.toString(), exception);
        return new ResponseEntity<>(new ErrorResponse(stringBuilder.toString()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidFileFormatException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFileFormatException(InvalidFileFormatException exception, WebRequest request) throws CanNotUpdateException {
        ServletRequestAttributes attributes = (ServletRequestAttributes) request;
        HttpServletRequest servletRequest = attributes.getRequest();
        String locale = servletRequest.getHeader("locale");
        locale = locale == null ? "en" : locale;
        String baseMessage = LocaleUtils.getMessage(messagesMap, locale, "failure", "file-is-not-excel");
        fileUploadService.handleUpdatedFailure(exception.getFileUUID(), baseMessage);
        logError(baseMessage, exception);
        return new ResponseEntity<>(new ErrorResponse(baseMessage), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CannotExtractYearException.class)
    public ResponseEntity<ErrorResponse> handleExtractYearFromFileNameException(CannotExtractYearException exception, WebRequest request) throws CanNotUpdateException {
        ServletRequestAttributes attributes = (ServletRequestAttributes) request;
        HttpServletRequest servletRequest = attributes.getRequest();
        String locale = servletRequest.getHeader("locale");
        locale = locale == null ? "en" : locale;
        String baseMessage = LocaleUtils.getMessage(messagesMap, locale, "failure",  "can-not-extract-year");
        StringBuilder stringBuilder = new StringBuilder(baseMessage);
        stringBuilder.insert(baseMessage.length() - 1, exception.getMessage());
        fileUploadService.handleUpdatedFailure(exception.getFileUUID(), stringBuilder.toString());
        logError(stringBuilder.toString(), exception);
        return new ResponseEntity<>(new ErrorResponse(stringBuilder.toString()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CanNotExtractMonthAnhYearException.class)
    public ResponseEntity<ErrorResponse> handleCannotExtractMonthAndYearException(CanNotExtractMonthAnhYearException exception, WebRequest request) throws CanNotUpdateException {
        ServletRequestAttributes attributes = (ServletRequestAttributes) request;
        HttpServletRequest servletRequest = attributes.getRequest();
        String locale = servletRequest.getHeader("locale");
        locale = locale == null ? "en" : locale;
        String baseMessage = LocaleUtils.getMessage(messagesMap, locale, "failure", "can-not-extract-month-and-year");
        String message = String.format(baseMessage, exception.getMessage());
        fileUploadService.handleUpdatedFailure(exception.getFileUUID(), message);
        logError(message, exception);
        return new ResponseEntity<>(new ErrorResponse(message), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IncorectFormatCellException.class)
    public ResponseEntity<ErrorResponse> handleIncorrectFormatCellException(IncorectFormatCellException exception, WebRequest request) throws CanNotUpdateException {
        ServletRequestAttributes attributes = (ServletRequestAttributes) request;
        HttpServletRequest servletRequest = attributes.getRequest();
        String locale = servletRequest.getHeader("locale");
        locale = locale == null ? "en" : locale;
        String baseMessage = LocaleUtils.getMessage(messagesMap, locale, "failure", "incorrect_cell_format");
        StringBuilder stringBuilder = new StringBuilder(baseMessage);
        stringBuilder.insert(baseMessage.length(), exception.getMessage());
        fileUploadService.handleUpdatedFailure(exception.getFileUUID(), stringBuilder.toString());
        logError(stringBuilder.toString(), exception);
        return new ResponseEntity<>(new ErrorResponse(stringBuilder.toString()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SeriesNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSeriesNotFoundException(SeriesNotFoundException exception, WebRequest request) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) request;
        HttpServletRequest servletRequest = attributes.getRequest();
        String locale = servletRequest.getHeader("locale");
        locale = locale == null ? "en" : locale;
        String baseMessage = LocaleUtils.getMessage(messagesMap, locale, "failure", "series_not_found");
        StringBuilder stringBuilder = new StringBuilder(baseMessage);
        stringBuilder.insert(baseMessage.length(), exception.getSeries());
        logError(stringBuilder.toString(), exception);
        return new ResponseEntity<>(new ErrorResponse(stringBuilder.toString()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ExchangeRatesException.class)
    public ResponseEntity<ErrorResponse> handleExchangeRatesException(ExchangeRatesException exception, WebRequest request) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) request;
        HttpServletRequest servletRequest = attributes.getRequest();
        String locale = servletRequest.getHeader("locale");
        locale = locale == null ? "en" : locale;

        String errorKey = "unexpected_error";
        String currency = "";
        if(exception.getMessage().contains("Unsupported currency before")) {
            errorKey = "unsupported_currency_before_2020";
            currency = exception.getUnsupportedCurrency();
        }
        else if(exception.getMessage().contains("Unsupported currency")) {
            errorKey = "unsupported_currency";
            currency = exception.getUnsupportedCurrency();
        }
        else if(exception.getMessage().contains("Inactive API Keys. Please check API Keys expired date"))
            errorKey = "inactive_api_key";
        else if(exception.getMessage().contains("Plan updated required"))
            errorKey = "plan_update_required";

        String baseMessage = LocaleUtils.getMessage(messagesMap, locale, "failure", errorKey) + currency;
        logError(baseMessage, exception);
        return new ResponseEntity<>(new ErrorResponse(baseMessage), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ExistingEmailException.class)
    public ResponseEntity<ErrorResponse> handleExistingEmailException(ExistingEmailException exception, WebRequest request) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) request;
        HttpServletRequest servletRequest = attributes.getRequest();
        String locale = servletRequest.getHeader("locale");
        locale = locale == null ? "en" : locale;

        String baseMessage = LocaleUtils.getMessage(messagesMap, locale, "failure", "existing_email");
        StringBuilder stringBuilder = new StringBuilder(baseMessage);
        stringBuilder.insert(baseMessage.length(), exception.getEmail());
        logError(stringBuilder.toString(), exception);
        return new ResponseEntity<>(new ErrorResponse(stringBuilder.toString()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EmailNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEmailNotFoundException(EmailNotFoundException exception, WebRequest request) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) request;
        HttpServletRequest servletRequest = attributes.getRequest();
        String locale = servletRequest.getHeader("locale");
        locale = locale == null ? "en" : locale;

        String baseMessage = LocaleUtils.getMessage(messagesMap, locale, "failure", "not_found_email");
        StringBuilder stringBuilder = new StringBuilder(baseMessage);
        stringBuilder.insert(baseMessage.length(), exception.getEmail());
        logError(stringBuilder.toString(), exception);
        return new ResponseEntity<>(new ErrorResponse(stringBuilder.toString()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserIdNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserIdNotFoundException(UserIdNotFoundException exception, WebRequest request) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) request;
        HttpServletRequest servletRequest = attributes.getRequest();
        String locale = servletRequest.getHeader("locale");
        locale = locale == null ? "en" : locale;

        String baseMessage = LocaleUtils.getMessage(messagesMap, locale, "failure", "user_id_not_found");
        StringBuilder stringBuilder = new StringBuilder(baseMessage);
        stringBuilder.insert(baseMessage.length(), exception.getUserId());
        logError(stringBuilder.toString(), exception);
        return new ResponseEntity<>(new ErrorResponse(stringBuilder.toString()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PasswordException.class)
    public ResponseEntity<ErrorResponse> handlePasswordException(PasswordException exception, WebRequest request) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) request;
        HttpServletRequest servletRequest = attributes.getRequest();
        String locale = servletRequest.getHeader("locale");
        locale = locale == null ? "en" : locale;

        // wrong_old_password
        // weak_password
        String baseMessage = LocaleUtils.getMessage(messagesMap, locale, "failure", exception.getType());
        log.error(exception.getMessage(), exception);
        return new ResponseEntity<>(new ErrorResponse(baseMessage), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CompetitorColorNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCompetitorColorNotFoundException(CompetitorColorNotFoundException exception, WebRequest request) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) request;
        HttpServletRequest servletRequest = attributes.getRequest();
        String locale = servletRequest.getHeader("locale");
        locale = locale == null ? "en" : locale;

        String baseMessage = LocaleUtils.getMessage(messagesMap, locale, "failure", "competitor_color_not_found");
        StringBuilder stringBuilder = new StringBuilder(baseMessage);
        stringBuilder.insert(baseMessage.length(), exception.getId());
        logError(stringBuilder.toString(), exception);
        return new ResponseEntity<>(new ErrorResponse(stringBuilder.toString()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CannotCreateFileException.class)
    public ResponseEntity<ErrorResponse> handleCannotCreateFileException(CannotCreateFileException exception, WebRequest request) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) request;
        HttpServletRequest servletRequest = attributes.getRequest();
        String locale = servletRequest.getHeader("locale");
        locale = locale == null ? "en" : locale;

        String baseMessage = LocaleUtils.getMessage(messagesMap, locale, "failure", "cannot_create_file");
        StringBuilder stringBuilder = new StringBuilder(baseMessage);
        stringBuilder.insert(baseMessage.length(), exception.getFileName());
        logError(stringBuilder.toString(), exception);
        return new ResponseEntity<>(new ErrorResponse(stringBuilder.toString()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MissingForecastFileException.class)
    public ResponseEntity<ErrorResponse> handleMissingForecastFileException(MissingForecastFileException exception, WebRequest request) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) request;
        HttpServletRequest servletRequest = attributes.getRequest();
        String locale = servletRequest.getHeader("locale");
        locale = locale == null ? "en" : locale;

        String baseMessage = LocaleUtils.getMessage(messagesMap, locale, "failure", "missing_forecast_file");
        logError(baseMessage, exception);
        return new ResponseEntity<>(new ErrorResponse(baseMessage), HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception exception, WebRequest request) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) request;
        HttpServletRequest servletRequest = attributes.getRequest();
        String locale = servletRequest.getHeader("locale");
        locale = locale == null ? "en" : locale;
        String baseMessage = LocaleUtils.getMessage(messagesMap, locale, "failure", "unexpected_error");
        StringBuilder stringBuilder = new StringBuilder(baseMessage);
        stringBuilder.insert(baseMessage.length(), exception.getMessage());
        logError(stringBuilder.toString(), exception);
        return new ResponseEntity<>(new ErrorResponse(baseMessage), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
