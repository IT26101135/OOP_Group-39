package com.drivingschool.auth.interceptor;

import com.drivingschool.auth.model.Role;
import com.drivingschool.auth.model.User;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.auth.service.CsrfTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NavigationInterceptorTest {

    private AuthSessionService sessionService;
    private NavigationInterceptor interceptor;

    @BeforeEach
    void setUp() {
        sessionService = mock(AuthSessionService.class);
        interceptor = new NavigationInterceptor(
                sessionService,
                new CsrfTokenService()
        );
    }

    @Test
    void anonymousHomeGetsPublicNavigationWithoutCreatingSession() {
        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/");
        MockHttpServletResponse response =
                new MockHttpServletResponse();
        ModelAndView page = new ModelAndView("home/index");

        when(sessionService.getCurrentUser(request))
                .thenReturn(Optional.empty());

        interceptor.postHandle(request, response, new Object(), page);

        assertEquals(false, page.getModel().get("navLoggedIn"));
        assertEquals("PUBLIC", page.getModel().get("navRole"));
        assertEquals("", page.getModel().get("navDisplayName"));
        assertEquals(true, page.getModel().get("navHomeActive"));
        assertFalse(page.getModel().containsKey("csrfToken"));
        assertNull(request.getSession(false));
        assertEquals("no-store", response.getHeader("Cache-Control"));
    }

    @Test
    void authenticatedStudentGetsNameAndValidLogoutToken() {
        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/");
        ModelAndView page = new ModelAndView("home/index");

        User student = mock(User.class);
        when(student.getName()).thenReturn("Demo Student");
        when(student.getRole()).thenReturn(Role.STUDENT);
        when(sessionService.getCurrentUser(request))
                .thenReturn(Optional.of(student));

        interceptor.postHandle(
                request,
                new MockHttpServletResponse(),
                new Object(),
                page
        );

        assertEquals(true, page.getModel().get("navLoggedIn"));
        assertEquals("STUDENT", page.getModel().get("navRole"));
        assertEquals("Demo Student",
                page.getModel().get("navDisplayName"));

        request.setParameter(
                (String) page.getModel().get("csrfParameterName"),
                (String) page.getModel().get("csrfToken")
        );

        assertTrue(new CsrfTokenService().isValid(request));
        assertFalse(page.getModel().containsValue(student));
        verify(student, never()).getPasswordHash();
    }

    @Test
    void administratorGetsAdminRole() {
        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/");
        ModelAndView page = new ModelAndView("home/index");

        User admin = mock(User.class);
        when(admin.getName()).thenReturn("System Admin");
        when(admin.getRole()).thenReturn(Role.ADMIN);
        when(sessionService.getCurrentUser(request))
                .thenReturn(Optional.of(admin));

        interceptor.postHandle(
                request,
                new MockHttpServletResponse(),
                new Object(),
                page
        );

        assertEquals("ADMIN", page.getModel().get("navRole"));
        assertEquals(true, page.getModel().get("navLoggedIn"));
    }

    @Test
    void homeDetectionSupportsContextPath() {
        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/academy/");
        request.setContextPath("/academy");

        when(sessionService.getCurrentUser(request))
                .thenReturn(Optional.empty());

        ModelAndView home = new ModelAndView("home/index");
        interceptor.postHandle(
                request, new MockHttpServletResponse(),
                new Object(), home
        );

        assertEquals(true, home.getModel().get("navHomeActive"));

        request.setRequestURI("/academy/login");
        ModelAndView login = new ModelAndView("auth/login");

        interceptor.postHandle(
                request, new MockHttpServletResponse(),
                new Object(), login
        );

        assertEquals(false, login.getModel().get("navHomeActive"));
    }

    @Test
    void redirectsAndResponsesWithoutViewsDoNotLoadAccountOrToken() {
        MockHttpServletRequest request =
                new MockHttpServletRequest("POST", "/logout");
        MockHttpServletResponse response =
                new MockHttpServletResponse();

        ModelAndView namedRedirect =
                new ModelAndView("redirect:/login?loggedOut");
        ModelAndView redirectView =
                new ModelAndView(new RedirectView("/login"));

        interceptor.postHandle(
                request, response, new Object(), namedRedirect);
        interceptor.postHandle(
                request, response, new Object(), redirectView);
        interceptor.postHandle(
                request, response, new Object(), null);

        verifyNoInteractions(sessionService);
        assertTrue(namedRedirect.getModel().isEmpty());
        assertTrue(redirectView.getModel().isEmpty());
        assertNull(request.getSession(false));
    }
}