package com.drivingschool.auth.interceptor;

import com.drivingschool.auth.annotation.RequireAdmin;
import com.drivingschool.auth.annotation.RequireLogin;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AccessInterceptorTest {

    private AuthSessionService sessionService;
    private AtomicInteger protectedCalls;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        sessionService = mock(AuthSessionService.class);
        protectedCalls = new AtomicInteger();

        mockMvc = MockMvcBuilders.standaloneSetup(
                        new ExampleController(protectedCalls),
                        new AdminController(protectedCalls)
                )
                .addInterceptors(new AccessInterceptor(sessionService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void publicEndpointDoesNotRequireAuthentication() throws Exception {
        mockMvc.perform(get("/test-access/public"))
                .andExpect(status().isOk())
                .andExpect(view().name("home/index"));

        verifyNoInteractions(sessionService);
    }

    @Test
    void anonymousUserCannotExecuteLoginProtectedEndpoint()
            throws Exception {

        when(sessionService.requireLogin(any()))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED
                ));

        mockMvc.perform(get("/test-access/account"))
                .andExpect(status().isUnauthorized())
                .andExpect(view().name("common/error"));

        assertEquals(0, protectedCalls.get());
    }

    @Test
    void successfulLoginCheckAllowsControllerExecution()
            throws Exception {

        mockMvc.perform(get("/test-access/account"))
                .andExpect(status().isOk())
                .andExpect(view().name("home/index"));

        verify(sessionService).requireLogin(any());
        verify(sessionService, never()).requireAdmin(any());
        assertEquals(1, protectedCalls.get());
    }

    @Test
    void anonymousUserCannotExecuteAdminEndpoint() throws Exception {
        when(sessionService.requireAdmin(any()))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED
                ));

        mockMvc.perform(get("/test-access/admin"))
                .andExpect(status().isUnauthorized())
                .andExpect(view().name("common/error"));

        assertEquals(0, protectedCalls.get());
    }

    @Test
    void studentCannotExecuteAdminEndpoint() throws Exception {
        when(sessionService.requireAdmin(any()))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.FORBIDDEN
                ));

        mockMvc.perform(get("/test-access/admin"))
                .andExpect(status().isForbidden())
                .andExpect(view().name("common/error"));

        assertEquals(0, protectedCalls.get());
    }

    @Test
    void successfulAdminCheckAllowsControllerExecution()
            throws Exception {

        mockMvc.perform(get("/test-access/admin"))
                .andExpect(status().isOk());

        verify(sessionService).requireAdmin(any());
        assertEquals(1, protectedCalls.get());
    }

    @Test
    void loginAnnotationCannotWeakenAdminOnlyClass() throws Exception {
        when(sessionService.requireAdmin(any()))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.FORBIDDEN
                ));

        mockMvc.perform(get("/test-access/admin-class"))
                .andExpect(status().isForbidden());

        verify(sessionService).requireAdmin(any());
        verify(sessionService, never()).requireLogin(any());
        assertEquals(0, protectedCalls.get());
    }

    @Controller
    static class ExampleController {

        private final AtomicInteger calls;

        ExampleController(AtomicInteger calls) {
            this.calls = calls;
        }

        @GetMapping("/test-access/public")
        public String publicPage() {
            return "home/index";
        }

        @RequireLogin
        @GetMapping("/test-access/account")
        public String accountPage() {
            calls.incrementAndGet();
            return "home/index";
        }

        @RequireAdmin
        @GetMapping("/test-access/admin")
        public String adminPage() {
            calls.incrementAndGet();
            return "home/index";
        }
    }

    @Controller
    @RequireAdmin
    static class AdminController {

        private final AtomicInteger calls;

        AdminController(AtomicInteger calls) {
            this.calls = calls;
        }

        @RequireLogin
        @GetMapping("/test-access/admin-class")
        public String adminPage() {
            calls.incrementAndGet();
            return "home/index";
        }
    }
}