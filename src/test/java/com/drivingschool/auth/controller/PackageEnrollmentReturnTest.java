/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication and integration
FILE     : PackageEnrollmentReturnTest.java
WHAT IT DOES (simple words): Checks the safe package destination survives login rotation.
REAL-LIFE EXAMPLE: Reception returns a learner to the course they asked about.
OOP USED : Abstraction through mocked helpers
CRUD     : READ login result
============================================================
*/
package com.drivingschool.auth.controller;
import com.drivingschool.auth.service.*;
import com.drivingschool.auth.form.LoginForm;
import com.drivingschool.student.model.Student;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
import org.springframework.ui.ExtendedModelMap;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class PackageEnrollmentReturnTest {
    // Only a fixed internal package path may replace the normal dashboard destination.
    @Test void returnsToSelectedPackageAfterSessionRotation() {
        assertEquals("redirect:/packages/P001", login("/packages/P001"));
    }
    // A stored external address must never turn login into an open redirect.
    @Test void rejectsExternalReturnDestination() {
        assertEquals("redirect:/student/profile", login("https://example.test"));
    }
    // Uses real servlet session rotation while keeping password verification outside this unit test.
    private String login(String destination) {
        AuthSessionService sessions = mock(AuthSessionService.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute("packageLoginReturn", destination);
        when(sessions.loginWithResult(request, "student@example.test", "password")).thenAnswer(call -> {
            request.getSession().invalidate(); request.getSession(true); return LoginResult.SUCCESS;
        });
        Student student = new Student("S001", "Test Student", "student@example.test", "$2a$10$" + "A".repeat(53), "0771234567");
        when(sessions.getCurrentUser(request)).thenReturn(Optional.of(student));
        LoginForm form = new LoginForm(); form.setEmail("student@example.test"); form.setPassword("password");
        AuthController controller = new AuthController(mock(AuthService.class), sessions, mock(CsrfTokenService.class));
        return controller.login(form, request, new MockHttpServletResponse(), new ExtendedModelMap());
    }
}
