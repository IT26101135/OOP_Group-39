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

    public NavigationInterceptor(
            AuthSessionService authSessionService,
            CsrfTokenService csrfTokenService) {

        this.authSessionService = authSessionService;
        this.csrfTokenService = csrfTokenService;
    }

    @Override
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