package com.drivingschool.instructor.controller;

import com.drivingschool.auth.filter.CsrfFilter;
import com.drivingschool.auth.interceptor.AccessInterceptor;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.auth.service.CsrfTokenService;
import com.drivingschool.common.exception.GlobalExceptionHandler;
import com.drivingschool.common.exception.RecordNotFoundException;
import com.drivingschool.common.exception.StorageException;
import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.instructor.model.Instructor;
import com.drivingschool.instructor.service.InstructorAvailabilityService;
import com.drivingschool.instructor.service.InstructorService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class InstructorControllerTest {

    private InstructorService instructorService;
    private InstructorAvailabilityService availabilityService;
    private AuthSessionService sessionService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        instructorService = mock(InstructorService.class);
        availabilityService = mock(InstructorAvailabilityService.class);
        sessionService = mock(AuthSessionService.class);

        CsrfTokenService csrf = new CsrfTokenService();

        mockMvc = MockMvcBuilders.standaloneSetup(
                        new InstructorController(
                                instructorService, availabilityService, csrf))
                .addInterceptors(new AccessInterceptor(sessionService))
                .addFilters(new CsrfFilter(csrf))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void anonymousCannotAccessInstructorList() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED))
                .when(sessionService).requireAdmin(any());

        mockMvc.perform(get("/admin/instructors"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(instructorService, availabilityService);
    }

    @Test
    void studentCannotAccessInstructorList() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN))
                .when(sessionService).requireAdmin(any());

        mockMvc.perform(get("/admin/instructors"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(instructorService, availabilityService);
    }

    @Test
    void adminCanAccessInstructorList() throws Exception {
        when(instructorService.getInstructorsSortedByExperience())
                .thenReturn(List.of());

        mockMvc.perform(get("/admin/instructors"))
                .andExpect(status().isOk())
                .andExpect(view().name("instructor/list"))
                .andExpect(model().attributeExists("instructors"));

        verify(sessionService).requireAdmin(any());
        verify(instructorService).getInstructorsSortedByExperience();
    }

    @Test
    void createPageProvidesFormAndCsrfToken() throws Exception {
        mockMvc.perform(get("/admin/instructors/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("instructor/create"))
                .andExpect(model().attributeExists(
                        "instructorCreateForm",
                        "csrfToken",
                        "csrfParameterName"));
    }

    @Test
    void createWithoutCsrfIsForbidden() throws Exception {
        mockMvc.perform(post("/admin/instructors/create")
                        .param("name", "Test Instructor"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(instructorService, availabilityService);
    }

    @Test
    void createUsesOnlyAllowedFields() throws Exception {
        MvcResult page = mockMvc.perform(
                get("/admin/instructors/create")).andReturn();

        mockMvc.perform(post("/admin/instructors/create")
                        .session(sessionOf(page))
                        .param("_csrf", tokenOf(page))
                        .param("name", "Test Instructor")
                        .param("email", "test@example.com")
                        .param("phone", "0771234567")
                        .param("yearsOfExperience", "5")
                        .param("id", "I999")
                        .param("active", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/instructors"));

        verify(instructorService).createInstructor(
                "Test Instructor", "test@example.com", "0771234567", 5);
    }

    @Test
    void invalidExperienceDoesNotCreateInstructor() throws Exception {
        MvcResult page = mockMvc.perform(
                get("/admin/instructors/create")).andReturn();

        mockMvc.perform(post("/admin/instructors/create")
                        .session(sessionOf(page))
                        .param("_csrf", tokenOf(page))
                        .param("name", "Test Instructor")
                        .param("email", "test@example.com")
                        .param("phone", "0771234567")
                        .param("yearsOfExperience", "not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(view().name("instructor/create"))
                .andExpect(model().attributeExists(
                        "formError", "csrfToken"));

        verifyNoInteractions(instructorService, availabilityService);
    }

    @Test
    void editPageLoadsExistingInstructor() throws Exception {
        Instructor instructor = sampleInstructor();

        when(instructorService.getInstructorById("I001"))
                .thenReturn(instructor);

        mockMvc.perform(get("/admin/instructors/I001/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("instructor/edit"))
                .andExpect(model().attributeExists(
                        "instructor", "instructorEditForm", "csrfToken"));
    }

    @Test
    void editPreservesUrlInstructorIdentity() throws Exception {
        Instructor instructor = sampleInstructor();

        when(instructorService.getInstructorById("I001"))
                .thenReturn(instructor);

        MvcResult page = mockMvc.perform(
                get("/admin/instructors/I001/edit")).andReturn();

        mockMvc.perform(post("/admin/instructors/I001/edit")
                        .session(sessionOf(page))
                        .param("_csrf", tokenOf(page))
                        .param("id", "I999")
                        .param("active", "false")
                        .param("name", "Updated Instructor")
                        .param("email", "updated@example.com")
                        .param("phone", "0777654321")
                        .param("yearsOfExperience", "8"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/instructors"));

        verify(instructorService).updateInstructor(
                "I001", "Updated Instructor",
                "updated@example.com", "0777654321", 8);
    }

    @Test
    void missingInstructorReturnsNotFound() throws Exception {
        when(instructorService.getInstructorById("I999"))
                .thenThrow(new RecordNotFoundException(
                        "Instructor not found: I999"));

        mockMvc.perform(get("/admin/instructors/I999/edit"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("common/error"));
    }

    @Test
    void availabilityPageRejectsInactiveInstructor() throws Exception {
        Instructor inactive = new Instructor(
                "I002", "Inactive Instructor",
                "inactive@example.com", "0771234567", 3, false);

        when(instructorService.getInstructorById("I002"))
                .thenReturn(inactive);

        mockMvc.perform(
                        get("/admin/instructors/I002/availability/create"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(availabilityService);
    }

    @Test
    void invalidAvailabilityIntervalDoesNotSave() throws Exception {
        when(instructorService.getInstructorById("I001"))
                .thenReturn(sampleInstructor());

        MvcResult page = mockMvc.perform(
                get("/admin/instructors/I001/availability/create"))
                .andReturn();

        mockMvc.perform(
                        post("/admin/instructors/I001/availability/create")
                                .session(sessionOf(page))
                                .param("_csrf", tokenOf(page))
                                .param("start", "2026-11-10T15:00")
                                .param("end", "2026-11-10T10:00"))
                .andExpect(status().isBadRequest())
                .andExpect(view().name("instructor/availability-create"))
                .andExpect(model().attributeExists(
                        "formError", "csrfToken"));

        verifyNoInteractions(availabilityService);
    }

    @Test
    void validAvailabilityDelegatesToService() throws Exception {
        when(instructorService.getInstructorById("I001"))
                .thenReturn(sampleInstructor());

        MvcResult page = mockMvc.perform(
                get("/admin/instructors/I001/availability/create"))
                .andReturn();

        mockMvc.perform(
                        post("/admin/instructors/I001/availability/create")
                                .session(sessionOf(page))
                                .param("_csrf", tokenOf(page))
                                .param("start", "2026-11-10T09:00")
                                .param("end", "2026-11-10T12:00")
                                .param("instructorId", "I999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/instructors"));

        verify(availabilityService).addAvailability(
                any(HttpServletRequest.class),
                eq("I001"),
                eq(LocalDateTime.of(2026, 11, 10, 9, 0)),
                eq(LocalDateTime.of(2026, 11, 10, 12, 0)));
    }

    @Test
    void storageFailureUsesGlobalErrorHandler() throws Exception {
        when(instructorService.getInstructorsSortedByExperience())
                .thenThrow(new StorageException("Storage unavailable", new IOException("Test storage failure")));

        mockMvc.perform(get("/admin/instructors"))
                .andExpect(status().isInternalServerError())
                .andExpect(view().name("common/error"));
    }

    private Instructor sampleInstructor() {
        return new Instructor(
                "I001", "Test Instructor",
                "test@example.com", "0771234567", 5, true);
    }

    private MockHttpSession sessionOf(MvcResult result) {
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private String tokenOf(MvcResult result) {
        return (String) result.getModelAndView()
                .getModel().get("csrfToken");
    }
}