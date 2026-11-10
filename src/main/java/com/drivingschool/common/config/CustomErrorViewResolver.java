/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Shared foundation
FILE     : CustomErrorViewResolver.java
WHAT IT DOES (simple words): Explains errors and gives each user a safe next step.
REAL-LIFE EXAMPLE: A receptionist directs a visitor back to the right desk.
OOP USED : Encapsulation and exception handling
CRUD     : Supports errors during CREATE, READ, UPDATE and DELETE
============================================================
*/
package com.drivingschool.common.config;

import com.drivingschool.common.exception.ErrorPageSupport;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import java.util.Map;

@Component
// SOLID: S - This adapter turns Spring HTTP errors into the shared friendly page.
public class CustomErrorViewResolver implements ErrorViewResolver {
    private final ErrorPageSupport pages;

    // SOLID: D - Receive the shared helper through the constructor.
    public CustomErrorViewResolver(ErrorPageSupport pages) {
        this.pages = pages;
    }

    @Override
    // Select a safe explanation without exposing Spring's internal exception details.
    public ModelAndView resolveErrorView(HttpServletRequest request, HttpStatus status, Map<String, Object> model) {
        String title = "We couldn't complete your request";
        String message = "Please try again or contact the academy.";
        if (status == HttpStatus.BAD_REQUEST) {
            title = "Please check your input";
            message = "Some details are missing or invalid. Return to the form and check them.";
        } else if (status == HttpStatus.FORBIDDEN) {
            title = "Access denied";
            message = "You don't have access to this page. Return to your account to continue.";
        } else if (status == HttpStatus.NOT_FOUND) {
            title = "We could not find that page";
            message = "Check the address or continue from your own dashboard.";
        }
        return pages.prepare(request, status, title, message);
    }
}
