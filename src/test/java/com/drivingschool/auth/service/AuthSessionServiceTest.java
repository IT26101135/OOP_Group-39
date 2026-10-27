package com.drivingschool.auth.service;

import com.drivingschool.auth.model.Admin;
import com.drivingschool.auth.model.User;
import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.common.exception.StorageException;
import com.drivingschool.student.model.Student;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthSessionServiceTest {

    private static final String USER_ID_ATTRIBUTE =
            "drivespark.auth.userId";

    // Format-only fixture; credential verification is mocked here.
    private static final String HASH =
            "$2a$10$" + "A".repeat(53);

    @Mock
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    private AuthSessionService service;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        service = new AuthSessionService(authService, userRepository);
        request = new MockHttpServletRequest();
    }

    private Student student() {
        return new Student(
                "S001",
                "Nimal Perera",
                "nimal@example.com",
                HASH,
                "0771234567"
        );
    }

    private Admin admin() {
        return new Admin(
                "A001",
                "System Admin",
                "admin@example.com",
                HASH
        );
    }

    private MockHttpSession sessionWithId(Object id) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(USER_ID_ATTRIBUTE, id);
        request.setSession(session);
        return session;
    }

    @Test
    void successfulLoginCreatesSessionContainingOnlyUserId() {
        when(authService.authenticate("nimal@example.com", "password"))
                .thenReturn(Optional.of(student()));

        assertTrue(service.login(
                request, "nimal@example.com", "password"
        ));

        HttpSession session = request.getSession(false);

        assertNotNull(session);
        assertEquals("S001", session.getAttribute(USER_ID_ATTRIBUTE));
        assertEquals(1800, session.getMaxInactiveInterval());

        assertEquals(
                java.util.List.of(USER_ID_ATTRIBUTE),
                Collections.list(session.getAttributeNames())
        );
    }

    @Test
    void successfulLoginReplacesOldSessionAndItsAttributes() {
        MockHttpSession oldSession = sessionWithId("A001");
        oldSession.setAttribute("oldAttribute", "old value");
        String oldId = oldSession.getId();

        when(authService.authenticate("nimal@example.com", "password"))
                .thenReturn(Optional.of(student()));

        assertTrue(service.login(
                request, "nimal@example.com", "password"
        ));

        HttpSession freshSession = request.getSession(false);

        assertTrue(oldSession.isInvalid());
        assertNotNull(freshSession);
        assertNotEquals(oldId, freshSession.getId());
        assertNull(freshSession.getAttribute("oldAttribute"));
        assertEquals(
                "S001", freshSession.getAttribute(USER_ID_ATTRIBUTE)
        );
    }

    @Test
    void failedLoginClearsPreviousSession() {
        MockHttpSession oldSession = sessionWithId("S001");

        when(authService.authenticate("nimal@example.com", "wrong"))
                .thenReturn(Optional.empty());

        assertFalse(service.login(
                request, "nimal@example.com", "wrong"
        ));

        assertTrue(oldSession.isInvalid());
        assertNull(request.getSession(false));
    }

    @Test
    void anonymousChecksDoNotCreateSession() {
        assertTrue(service.getCurrentUser(request).isEmpty());
        assertFalse(service.isLoggedIn(request));
        assertFalse(service.isAdmin(request));

        assertNull(request.getSession(false));
        verifyNoInteractions(userRepository);
    }

    @Test
    void anonymousExistingSessionRemainsAnonymous() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("otherFeature", "value");
        request.setSession(session);

        assertTrue(service.getCurrentUser(request).isEmpty());
        assertFalse(session.isInvalid());
        verifyNoInteractions(userRepository);
    }

    @Test
    void currentUserIsReloadedFromRepository() {
        sessionWithId("S001");

        Student updatedStudent = new Student(
                "S001",
                "Updated Name",
                "nimal@example.com",
                HASH,
                "0771234567"
        );

        when(userRepository.findByIdStrict("S001"))
                .thenReturn(Optional.of(student()))
                .thenReturn(Optional.of(updatedStudent));

        assertEquals(
                "Nimal Perera",
                service.getCurrentUser(request).orElseThrow().getName()
        );

        assertEquals(
                "Updated Name",
                service.getCurrentUser(request).orElseThrow().getName()
        );

        verify(userRepository, times(2)).findByIdStrict("S001");
    }

    @Test
    void studentIsLoggedInButCannotPassAdminCheck() {
        sessionWithId("S001");

        when(userRepository.findByIdStrict("S001"))
                .thenReturn(Optional.of(student()));

        assertTrue(service.isLoggedIn(request));
        assertFalse(service.isAdmin(request));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.requireAdmin(request)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void administratorPassesAdminCheck() {
        sessionWithId("A001");

        when(userRepository.findByIdStrict("A001"))
                .thenReturn(Optional.of(admin()));

        assertTrue(service.isAdmin(request));

        User authenticated = service.requireAdmin(request);

        assertInstanceOf(Admin.class, authenticated);
        assertEquals("A001", authenticated.getId());
    }

    @Test
    void anonymousProtectedAccessRequiresLogin() {
        ResponseStatusException loginException = assertThrows(
                ResponseStatusException.class,
                () -> service.requireLogin(request)
        );

        ResponseStatusException adminException = assertThrows(
                ResponseStatusException.class,
                () -> service.requireAdmin(request)
        );

        assertEquals(
                HttpStatus.UNAUTHORIZED, loginException.getStatusCode()
        );
        assertEquals(
                HttpStatus.UNAUTHORIZED, adminException.getStatusCode()
        );

        assertNull(request.getSession(false));
    }

    @Test
    void deletedAccountInvalidatesItsSession() {
        MockHttpSession session = sessionWithId("S001");

        when(userRepository.findByIdStrict("S001"))
                .thenReturn(Optional.empty());

        assertTrue(service.getCurrentUser(request).isEmpty());
        assertTrue(session.isInvalid());
        assertNull(request.getSession(false));
    }

    @Test
    void malformedSessionIdentityIsRejectedWithoutRepositoryLookup() {
        for (Object invalid : new Object[]{
                123, "S000", "S1000", "I001", "", " S001"
        }) {
            request = new MockHttpServletRequest();
            MockHttpSession session = sessionWithId(invalid);

            assertTrue(service.getCurrentUser(request).isEmpty());
            assertTrue(session.isInvalid());
        }

        verifyNoInteractions(userRepository);
    }

    @Test
    void storageFailureIsNotConvertedIntoAnonymousAccess() {
        sessionWithId("S001");

        StorageException failure = new StorageException(
                "Cannot read accounts.",
                new IOException("Test storage failure")
        );

        when(userRepository.findByIdStrict("S001"))
                .thenThrow(failure);

        assertSame(failure, assertThrows(
                StorageException.class,
                () -> service.getCurrentUser(request)
        ));

        assertSame(failure, assertThrows(
                StorageException.class,
                () -> service.isLoggedIn(request)
        ));

        assertSame(failure, assertThrows(
                StorageException.class,
                () -> service.requireAdmin(request)
        ));
    }

    @Test
    void authenticationStorageFailureDoesNotCreateSession() {
        StorageException failure = new StorageException(
                "Cannot read accounts.",
                new IOException("Test storage failure")
        );

        when(authService.authenticate("nimal@example.com", "password"))
                .thenThrow(failure);

        assertSame(failure, assertThrows(
                StorageException.class,
                () -> service.login(
                        request, "nimal@example.com", "password"
                )
        ));

        assertNull(request.getSession(false));
    }

    @Test
    void logoutInvalidatesSessionAndCanBeRepeated() {
        MockHttpSession session = sessionWithId("S001");

        service.logout(request);

        assertTrue(session.isInvalid());
        assertNull(request.getSession(false));

        assertDoesNotThrow(() -> service.logout(request));
    }
}