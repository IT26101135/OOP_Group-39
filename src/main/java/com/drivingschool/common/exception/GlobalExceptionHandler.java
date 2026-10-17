package com.drivingschool.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ValidationException.class)
    public ModelAndView handleValidation(ValidationException exception) {
        return errorPage(
                HttpStatus.BAD_REQUEST,
                "Invalid information",
                exception.getMessage()
        );
    }

    @ExceptionHandler(RecordNotFoundException.class)
    public ModelAndView handleNotFound(RecordNotFoundException exception) {
        return errorPage(
                HttpStatus.NOT_FOUND,
                "Record not found",
                exception.getMessage()
        );
    }

    @ExceptionHandler(DuplicateRecordException.class)
    public ModelAndView handleDuplicate(DuplicateRecordException exception) {
        return errorPage(
                HttpStatus.CONFLICT,
                "Record already exists",
                exception.getMessage()
        );
    }

    @ExceptionHandler(StorageException.class)
    public ModelAndView handleStorage(StorageException exception) {
        // Log the failure type without exposing records or internal paths.
        Throwable cause = exception.getCause();

        LOG.error(
                "Storage operation failed. Cause type: {}",
                cause == null
                        ? exception.getClass().getSimpleName()
                        : cause.getClass().getSimpleName()
        );

        return errorPage(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Storage operation failed",
                "We could not complete the operation. "
                        + "Please contact the administrator."
        );
    }

    private ModelAndView errorPage(
            HttpStatus status,
            String title,
            String message) {

        ModelAndView page = new ModelAndView("common/error");

        page.setStatus(status);
        page.addObject("statusCode", status.value());
        page.addObject("title", title);
        page.addObject("message", message);

        return page;
    }
}