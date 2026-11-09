/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Shared foundation
FILE     : ErrorStatusMappingTest.java
WHAT IT DOES (simple words): Checks status codes and safe recovery destinations.
REAL-LIFE EXAMPLE: A receptionist directs a visitor back to the right desk.
OOP USED : Encapsulation and exception handling
CRUD     : Supports errors during CREATE, READ, UPDATE and DELETE
============================================================
*/
package com.drivingschool.common.exception;

import com.drivingschool.auth.model.Role;
import com.drivingschool.auth.model.User;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.common.config.CustomErrorViewResolver;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import java.util.HashMap;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ErrorStatusMappingTest {
    // A bad number must explain the input problem and return to its own GET form.
    @Test
    void badRequestUsesFriendlyInputPage() {
        ModelAndView page = resolve(HttpStatus.BAD_REQUEST, null);
        assertEquals("Please check your input", page.getModel().get("title"));
        assertEquals("/admin/packages", page.getModel().get("retryUrl"));
    }

    // Access errors must preserve the view name used by the existing security tests.
    @Test
    void forbiddenKeepsCommonErrorView() { resolve(HttpStatus.FORBIDDEN, null); }

    // Missing pages must keep their real 404 status.
    @Test
    void missingPageKeeps404() { resolve(HttpStatus.NOT_FOUND, null); }

    // A real server error must keep 500 and hide internal exception text.
    @Test
    void serverErrorKeeps500() {
        ModelAndView page = resolve(HttpStatus.INTERNAL_SERVER_ERROR, null);
        assertFalse(page.getModel().get("message").toString().contains("secret"));
    }

    // Visitors need login rather than a student payment link.
    @Test
    void visitorReturnsToLogin() { assertEquals("/login", resolve(HttpStatus.FORBIDDEN, null).getModel().get("recoveryUrl")); }

    // Students return to the existing profile dashboard.
    @Test
    void studentReturnsToProfile() { assertEquals("/student/profile", resolve(HttpStatus.FORBIDDEN, Role.STUDENT).getModel().get("recoveryUrl")); }

    // Instructors return to their lesson dashboard.
    @Test
    void instructorReturnsToLessons() { assertEquals("/instructor/dashboard", resolve(HttpStatus.FORBIDDEN, Role.INSTRUCTOR).getModel().get("recoveryUrl")); }

    // Administrators return to their own dashboard.
    @Test
    void adminReturnsToAdminDashboard() { assertEquals("/admin/dashboard", resolve(HttpStatus.FORBIDDEN, Role.ADMIN).getModel().get("recoveryUrl")); }

    // Even damaged account storage must leave the server-error page usable.
    @Test
    void serverErrorSurvivesFailedAccountLookup() {
        AuthSessionService sessions = mock(AuthSessionService.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        when(sessions.getCurrentUser(request)).thenThrow(new StorageException("Unreadable accounts", new java.io.IOException("Test failure")));
        ErrorPageSupport pages = new ErrorPageSupport(sessions);
        ModelAndView page = pages.prepare(request, HttpStatus.INTERNAL_SERVER_ERROR, "Error", "Try again");
        assertEquals("common/error", page.getViewName());
        assertEquals("/login", page.getModel().get("recoveryUrl"));
    }

    // Supply a tiny mocked session, keeping every test away from saved project data.
    private ModelAndView resolve(HttpStatus status, Role role) {
        AuthSessionService sessions = mock(AuthSessionService.class);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/admin/packages");
        if (role == null) when(sessions.getCurrentUser(request)).thenReturn(Optional.empty());
        else {
            User account = mock(User.class);
            when(account.getRole()).thenReturn(role);
            when(sessions.getCurrentUser(request)).thenReturn(Optional.of(account));
        }
        CustomErrorViewResolver resolver = new CustomErrorViewResolver(new ErrorPageSupport(sessions));
        HashMap<String, Object> internal = new HashMap<>();
        internal.put("message", "secret database details");
        ModelAndView page = resolver.resolveErrorView(request, status, internal);
        assertEquals("common/error", page.getViewName());
        assertEquals(status, page.getStatus());
        assertEquals(status.value(), page.getModel().get("statusCode"));
        return page;
    }
}
