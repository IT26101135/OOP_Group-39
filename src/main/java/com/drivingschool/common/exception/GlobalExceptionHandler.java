/**
 * ============================================================
 * MEMBER   : IT26101135 - [Name]
 * MODULE   : Shared foundation
 * FILE     : GlobalExceptionHandler.java
 * WHAT IT DOES (simple words): Shows a friendly error page.
 * REAL-LIFE EXAMPLE: The receptionist explains what went wrong and what to do next.
 * OOP USED : Exception handling
 * CRUD     : Supports errors during CREATE, READ, UPDATE and DELETE
 * ============================================================
 */
package com.drivingschool.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.server.ResponseStatusException;

// SOLID: S - One shared place turns application problems into friendly pages.
@ControllerAdvice
public class GlobalExceptionHandler {

    private final ErrorPageSupport pages;

    // Receive the same page helper used for Spring's own HTTP errors.
    public GlobalExceptionHandler(ErrorPageSupport pages) {
        this.pages = pages;
    }

    private static final Logger LOG =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ValidationException.class)
    // Explains which information needs correcting.
    public ModelAndView handleValidation(ValidationException exception, HttpServletRequest request) {
        return errorPage(
                request,
                HttpStatus.BAD_REQUEST,
                "Please check your input",
                exception.getMessage()
        );
    }

    @ExceptionHandler(RecordNotFoundException.class)
    // Explains that the requested record could not be found.
    public ModelAndView handleNotFound(RecordNotFoundException exception, HttpServletRequest request) {
        return errorPage(
                request,
                HttpStatus.NOT_FOUND,
                "Record not found",
                exception.getMessage()
        );
    }

    @ExceptionHandler(DuplicateRecordException.class)
    // Explains that a record with those details already exists.
    public ModelAndView handleDuplicate(DuplicateRecordException exception, HttpServletRequest request) {
        return errorPage(
                request,
                HttpStatus.CONFLICT,
                "Record already exists",
                exception.getMessage()
        );
    }

    @ExceptionHandler(StorageException.class)
    // Logs the failure type and shows a safe message without private details.
    public ModelAndView handleStorage(StorageException exception, HttpServletRequest request) {
        // Log the failure type without exposing records or internal paths.
        Throwable cause = exception.getCause();

        LOG.error(
                "Storage operation failed. Cause type: {}",
                cause == null
                        ? exception.getClass().getSimpleName()
                        : cause.getClass().getSimpleName()
        );

        return errorPage(
                request,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Storage operation failed",
                "We could not complete the operation. "
                        + "Please contact the administrator."
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    // Sends visitors to login or explains why access is refused.
    public ModelAndView handleResponseStatus(
            ResponseStatusException exception, HttpServletRequest request) {

        int status = exception.getStatusCode().value();

        if (status == HttpStatus.UNAUTHORIZED.value()) {
            return new ModelAndView("redirect:/login?required");
        }

        if (status == HttpStatus.FORBIDDEN.value()) {
            return errorPage(
                request,
                    HttpStatus.FORBIDDEN,
                    "Access denied",
                    "You do not have permission to access this resource."
            );
        }

        // Preserve Spring's normal handling of other HTTP status errors.
        throw exception;
    }

    // Builds the shared error page with its correct HTTP status.
    private ModelAndView errorPage(HttpServletRequest request, HttpStatus status,
                                   String title, String message) {
        return pages.prepare(request, status, title, message);
    }
}
