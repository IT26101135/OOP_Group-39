/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : NavigationInterceptor.java
WHAT IT DOES (simple words): Supports navigation interceptor for the account or course workflow.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.auth.interceptor;

import com.drivingschool.auth.model.User;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.auth.service.CsrfTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Optional;

@Component
public class NavigationInterceptor implements HandlerInterceptor {

    private final AuthSessionService authSessionService;
    private final CsrfTokenService csrfTokenService;

    // Receives the helpers this class needs so each job stays separate.
    public NavigationInterceptor(
            AuthSessionService authSessionService,
            CsrfTokenService csrfTokenService) {

        this.authSessionService = authSessionService;
        this.csrfTokenService = csrfTokenService;
    }

    @Override
    // Adds shared navigation and security details after the controller prepares a page.
    public void postHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            ModelAndView modelAndView) {

        if (modelAndView == null) {
            return;
        }

        String viewName = modelAndView.getViewName();

        // Do not attach display information or tokens to redirects.
        if (modelAndView.getView() instanceof RedirectView
                || (viewName != null
                && viewName.startsWith("redirect:"))) {
            return;
        }

        Optional<User> currentUser =
                authSessionService.getCurrentUser(request);

        modelAndView.addObject(
                "navLoggedIn",
                currentUser.isPresent()
        );
        modelAndView.addObject(
                "navRole",
                currentUser.map(user -> user.getRole().name())
                        .orElse("PUBLIC")
        );
        modelAndView.addObject(
                "navDisplayName",
                currentUser.map(User::getName).orElse("")
        );
        modelAndView.addObject(
                "navHomeActive",
                "/".equals(request.getRequestURI()
                        .substring(request.getContextPath().length()))
        );

        // The same URL can render different navigation for different
        // sessions. Do not cache these personalized HTML responses.
        response.setHeader("Cache-Control", "no-store");

        if (currentUser.isPresent()) {
            modelAndView.addObject(
                    "csrfParameterName",
                    CsrfTokenService.PARAMETER_NAME
            );
            modelAndView.addObject(
                    "csrfToken",
                    csrfTokenService.getOrCreateToken(request)
            );
        }
    }
}