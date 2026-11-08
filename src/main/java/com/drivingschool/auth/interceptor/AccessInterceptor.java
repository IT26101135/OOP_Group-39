/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : AccessInterceptor.java
WHAT IT DOES (simple words): Supports access interceptor for the account or course workflow.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
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

    // Receives the helpers this class needs so each job stays separate.
    public AccessInterceptor(AuthSessionService authSessionService) {
        this.authSessionService = authSessionService;
    }

    @Override
    // Checks the controller access labels before the requested page may run.
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

    // Spring reads the login/admin labels already attached to controllers.
    // Class<? extends Annotation> means any annotation type; this helper never changes code.
    // Refuses access unless the current account meets the requires rule.
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