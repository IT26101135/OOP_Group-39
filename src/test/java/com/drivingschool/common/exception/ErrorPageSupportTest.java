// MEMBER: IT26101135 - Shared recovery links. CRUD: no writes.
package com.drivingschool.common.exception;

import com.drivingschool.auth.model.Role;
import com.drivingschool.auth.model.User;
import com.drivingschool.auth.service.AuthSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ErrorPageSupportTest {
    private final AuthSessionService sessions = mock(AuthSessionService.class);
    private final ErrorPageSupport pages = new ErrorPageSupport(sessions);

    @Test
    void recoveryDoesNotOfferAnotherRolesForm() {
        MockHttpServletRequest request = request("/admin/packages/update", Role.STUDENT);
        var page = pages.prepare(request, HttpStatus.BAD_REQUEST, "Check input", "Try again");
        assertNull(page.getModel().get("retryUrl"));
        assertEquals("/student/profile", page.getModel().get("recoveryUrl"));
    }

    @Test
    void missingVehicleLinksToExistingListRatherThanMissingEditForm() {
        MockHttpServletRequest request = request("/admin/vehicles/V999/edit", Role.ADMIN);
        var page = pages.prepare(request, HttpStatus.NOT_FOUND, "Missing", "Choose another record");
        assertEquals("/admin/vehicles", page.getModel().get("retryUrl"));
        assertEquals("Back to the list", page.getModel().get("retryLabel"));
    }

    @Test
    void conflictReturnsInstructorToTheirWorkingHoursForm() {
        MockHttpServletRequest request = request("/instructor/availability/update", Role.INSTRUCTOR);
        var page = pages.prepare(request, HttpStatus.CONFLICT, "Conflict", "Choose another time");
        assertEquals("/instructor/availability", page.getModel().get("retryUrl"));
    }

    private MockHttpServletRequest request(String path, Role role) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
        User account = mock(User.class);
        when(account.getRole()).thenReturn(role);
        when(sessions.getCurrentUser(request)).thenReturn(Optional.of(account));
        return request;
    }
}
