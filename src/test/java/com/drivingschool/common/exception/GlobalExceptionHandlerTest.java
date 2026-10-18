package com.drivingschool.common.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.io.IOException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        InternalResourceViewResolver resolver =
                new InternalResourceViewResolver();

        resolver.setPrefix("/WEB-INF/views/");
        resolver.setSuffix(".jsp");

        mockMvc = MockMvcBuilders
                .standaloneSetup(new ErrorTestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setViewResolvers(resolver)
                .build();
    }

    @Test
    void validationReturns400() throws Exception {
        mockMvc.perform(get("/test-errors/validation"))
                .andExpect(status().isBadRequest())
                .andExpect(view().name("common/error"))
                .andExpect(model().attribute("statusCode", 400))
                .andExpect(model().attribute(
                        "message", "Name is required."
                ));
    }

    @Test
    void missingRecordReturns404() throws Exception {
        mockMvc.perform(get("/test-errors/missing"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("common/error"))
                .andExpect(model().attribute("statusCode", 404));
    }

    @Test
    void duplicateReturns409() throws Exception {
        mockMvc.perform(get("/test-errors/duplicate"))
                .andExpect(status().isConflict())
                .andExpect(view().name("common/error"))
                .andExpect(model().attribute("statusCode", 409));
    }

    @Test
    void storageReturns500WithoutInternalDetails() throws Exception {
        mockMvc.perform(get("/test-errors/storage"))
                .andExpect(status().isInternalServerError())
                .andExpect(view().name("common/error"))
                .andExpect(model().attribute("statusCode", 500))
                .andExpect(model().attribute(
                        "message",
                        "We could not complete the operation. "
                                + "Please contact the administrator."
                ));
    }

    // Only used by tests; these routes are not part of the running app.
    @Controller
    static class ErrorTestController {

        @GetMapping("/test-errors/validation")
        public String validation() {
            throw new ValidationException("Name is required.");
        }

        @GetMapping("/test-errors/missing")
        public String missing() {
            throw new RecordNotFoundException("Record not found.");
        }

        @GetMapping("/test-errors/duplicate")
        public String duplicate() {
            throw new DuplicateRecordException("ID already exists.");
        }

        @GetMapping("/test-errors/storage")
        public String storage() {
            throw new StorageException(
                    "Internal storage details",
                    new IOException("Internal filesystem details")
            );
        }
    }
}