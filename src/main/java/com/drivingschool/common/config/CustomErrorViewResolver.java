// Module owner: IT26101135
package com.drivingschool.common.config;

import com.drivingschool.auth.service.AuthSessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;
import java.util.HashMap;

/** Resolves browser error dispatches to JSPs; JSON stays with Boot's JSON handler. */
@Component
public class CustomErrorViewResolver implements ErrorViewResolver {

    private final AuthSessionService authSessionService;

    public CustomErrorViewResolver(AuthSessionService authSessionService) {
        this.authSessionService = authSessionService;
    }

    @Override
    public ModelAndView resolveErrorView(
            HttpServletRequest request,
            HttpStatus status,
            Map<String, Object> model) {
        Map<String, Object> viewModel = new HashMap<>(model);
        viewModel.put("navRole", authSessionService.getCurrentUser(request)
                .map(user -> user.getRole().name()).orElse("PUBLIC"));
        String view = switch (status) {
            case NOT_FOUND -> "common/error-404";
            case FORBIDDEN -> "common/error-403";
            default -> "common/error-500";
        };
        return new ModelAndView(view, viewModel, status);
    }
}
