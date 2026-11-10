/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : CsrfFilter.java
WHAT IT DOES (simple words): Supports csrf filter for the account or course workflow.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.auth.filter;

import com.drivingschool.auth.service.CsrfTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
@Order(0)
public class CsrfFilter extends OncePerRequestFilter {

    private static final Set<String> SAFE_METHODS =
            Set.of("GET", "HEAD", "OPTIONS", "TRACE");

    private final CsrfTokenService csrfTokenService;

    // Receives the helpers this class needs so each job stays separate.
    public CsrfFilter(CsrfTokenService csrfTokenService) {
        this.csrfTokenService = csrfTokenService;
    }

    @Override
    // Checks write-request tokens so another website cannot submit forms as this user.
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        boolean requiresToken =
                !SAFE_METHODS.contains(request.getMethod());

        if (requiresToken && !csrfTokenService.isValid(request)) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Your form session is invalid or expired. "
                            + "Reload the form and try again."
            );
            return;
        }

        filterChain.doFilter(request, response);
    }
}