package com.drivingschool.auth.interceptor;

import com.drivingschool.auth.annotation.RequireAdmin;
import com.drivingschool.auth.annotation.RequireLogin;
import com.drivingschool.auth.service.AuthSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.annotation.Annotation;

@Component
public class AccessInterceptor implements HandlerInterceptor {

    private final AuthSessionService authSessionService;

    public AccessInterceptor(AuthSessionService authSessionService) {
        this.authSessionService = authSessionService;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {

        // Static resources are not controller methods.
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // Administrator requirements always take priority.
        // A method annotation cannot weaken an admin-only class.
        if (requires(handlerMethod, RequireAdmin.class)) {
            authSessionService.requireAdmin(request);
        } else if (requires(handlerMethod, RequireLogin.class)) {
            authSessionService.requireLogin(request);
        }

        return true;
    }

    private boolean requires(
            HandlerMethod handler,
            Class<? extends Annotation> annotationType) {

        return AnnotatedElementUtils.hasAnnotation(
                handler.getMethod(),
                annotationType
        ) || AnnotatedElementUtils.hasAnnotation(
                handler.getBeanType(),
                annotationType
        );
    }
}