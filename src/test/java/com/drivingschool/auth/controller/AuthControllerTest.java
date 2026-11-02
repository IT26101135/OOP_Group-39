package com.drivingschool.auth.controller;

import com.drivingschool.auth.filter.CsrfFilter;
import com.drivingschool.auth.form.LoginForm;
import com.drivingschool.auth.form.RegistrationForm;
import com.drivingschool.auth.service.AuthService;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.auth.service.CsrfTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {

    private AuthService authService;
    private AuthSessionService sessionService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        sessionService = mock(AuthSessionService.class);

        CsrfTokenService csrf = new CsrfTokenService();

        mockMvc = MockMvcBuilders.standaloneSetup(
                        new AuthController(authService, sessionService, csrf))
                .addFilters(new CsrfFilter(csrf))
                .build();
    }

    @Test
    void registrationGetProvidesFormAndToken() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists(
                        "registrationForm", "csrfToken", "csrfParameterName"));
    }

    @Test
    void registrationWithoutTokenDoesNotReachService() throws Exception {
        mockMvc.perform(post("/register"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(authService, sessionService);
    }

    @Test
    void registrationUsesOnlyApprovedInput() throws Exception {
        MvcResult page = mockMvc.perform(get("/register")).andReturn();

        mockMvc.perform(post("/register")
                        .session(sessionOf(page))
                        .param("_csrf", tokenOf(page))
                        .param("name", "Test Student")
                        .param("email", "student@example.com")
                        .param("phone", "0771234567")
                        .param("password", "ExamplePass123!")
                        .param("confirmPassword", "ExamplePass123!")
                        .param("role", "ADMIN")
                        .param("id", "A001"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        verify(authService).registerStudent(
                "Test Student",
                "student@example.com",
                "0771234567",
                "ExamplePass123!"
        );
    }

    @Test
    void mismatchedPasswordsDoNotCreateAccount() throws Exception {
        MvcResult page = mockMvc.perform(get("/register")).andReturn();

        MvcResult result = mockMvc.perform(post("/register")
                        .session(sessionOf(page))
                        .param("_csrf", tokenOf(page))
                        .param("password", "ExamplePass123!")
                        .param("confirmPassword", "DifferentPass123!"))
                .andExpect(status().isBadRequest())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("formError"))
                .andReturn();

        RegistrationForm form = (RegistrationForm) result
                .getModelAndView().getModel().get("registrationForm");

        assertNull(form.getPassword());
        assertNull(form.getConfirmPassword());
        verifyNoInteractions(authService);
    }

    @Test
    void successfulLoginRedirectsHome() throws Exception {
        MvcResult page = mockMvc.perform(get("/login")).andReturn();

        when(sessionService.login(
                any(), eq("student@example.com"), eq("ExamplePass123!")
        )).thenReturn(true);

        mockMvc.perform(post("/login")
                        .session(sessionOf(page))
                        .param("_csrf", tokenOf(page))
                        .param("email", "student@example.com")
                        .param("password", "ExamplePass123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void failedLoginReplacesTokenAfterSessionInvalidation()
            throws Exception {

        MvcResult page = mockMvc.perform(get("/login")).andReturn();
        MockHttpSession oldSession = sessionOf(page);
        String oldToken = tokenOf(page);

        when(sessionService.login(any(), anyString(), anyString()))
                .thenAnswer(invocation -> {
                    jakarta.servlet.http.HttpServletRequest request =
                            invocation.getArgument(0);
                    request.getSession(false).invalidate();
                    return false;
                });

        MvcResult result = mockMvc.perform(post("/login")
                        .session(oldSession)
                        .param("_csrf", oldToken)
                        .param("email", "student@example.com")
                        .param("password", "WrongPassword"))
                .andExpect(status().isUnauthorized())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attribute(
                        "formError", "Email or password is incorrect."))
                .andReturn();

        assertTrue(oldSession.isInvalid());
        assertNotSame(oldSession, result.getRequest().getSession(false));
        assertNotEquals(oldToken, tokenOf(result));

        LoginForm form = (LoginForm) result.getModelAndView()
                .getModel().get("loginForm");

        assertNull(form.getPassword());
    }

    @Test
    void logoutRequiresTokenAndDelegatesToSessionService()
            throws Exception {

        mockMvc.perform(post("/logout"))
                .andExpect(status().isForbidden());

        verify(sessionService, never()).logout(any());

        MvcResult page = mockMvc.perform(get("/login")).andReturn();

        mockMvc.perform(post("/logout")
                        .session(sessionOf(page))
                        .param("_csrf", tokenOf(page)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?loggedOut"));

        verify(sessionService).logout(any());
    }

    private MockHttpSession sessionOf(MvcResult result) {
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private String tokenOf(MvcResult result) {
        return (String) result.getModelAndView()
                .getModel().get("csrfToken");
    }
}