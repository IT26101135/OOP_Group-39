package com.drivingschool.common.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthorizationErrorHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void unauthorizedUsesSharedErrorPageAndPreserves401()
            throws Exception {

        mockMvc.perform(get("/test-errors/unauthorized"))
                .andExpect(status().isUnauthorized())
                .andExpect(view().name("common/error"))
                .andExpect(model().attribute("statusCode", 401))
                .andExpect(model().attribute(
                        "title", "Login required"))
                .andExpect(model().attribute(
                        "message", "Please log in to continue."));
    }

    @Test
    void forbiddenUsesSharedErrorPageWithoutExposingInternalReason()
            throws Exception {

        mockMvc.perform(get("/test-errors/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(view().name("common/error"))
                .andExpect(model().attribute("statusCode", 403))
                .andExpect(model().attribute(
                        "title", "Access denied"))
                .andExpect(model().attribute(
                        "message",
                        "You do not have permission to access this resource."
                ));
    }

    @Test
    void otherResponseStatusErrorsKeepTheirOriginalStatus()
            throws Exception {

        mockMvc.perform(get("/test-errors/not-found"))
                .andExpect(status().isNotFound());
    }

    @Controller
    static class TestController {

        @GetMapping("/test-errors/unauthorized")
        public String unauthorized() {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Internal authentication detail"
            );
        }

        @GetMapping("/test-errors/forbidden")
        public String forbidden() {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Internal authorization detail"
            );
        }

        @GetMapping("/test-errors/not-found")
        public String notFound() {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Test resource was not found"
            );
        }
    }
}