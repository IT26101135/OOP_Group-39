// MEMBER: IT26101135 - Shared contact flow. CRUD: CREATE and admin-only READ.
package com.drivingschool.common.service;

import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.common.model.ContactMessage;
import com.drivingschool.common.repository.ContactMessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.nio.file.Path;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ContactServiceTest {
    @TempDir Path directory;
    private final Clock clock = Clock.fixed(Instant.parse("2030-01-10T08:00:00Z"), ZoneOffset.UTC);
    private ContactMessageRepository messages;
    private AuthSessionService auth;
    private ContactService service;

    @BeforeEach
    void prepare() {
        messages = new ContactMessageRepository(directory.toString(), new ObjectMapper().findAndRegisterModules());
        auth = mock(AuthSessionService.class);
        service = new ContactService(messages, clock, auth);
    }

    @Test
    void immediateRetriesSaveOnlyOneNormalizedMessage() {
        service.submit("  Audit Visitor ", " AUDIT@EXAMPLE.TEST ", " Please send lesson times. ");
        service.submit("Audit Visitor", "audit@example.test", "Please send lesson times.");
        assertEquals(1, messages.readAllStrict().size());
        assertEquals("audit@example.test", messages.readAllStrict().get(0).getEmail());
    }

    @Test
    void sameTextAfterRetryWindowCanBeSentAgain() {
        messages.save(new ContactMessage("M001", "Audit Visitor", "audit@example.test",
                "Please send lesson times.", LocalDateTime.now(clock).minusMinutes(2)));
        service.submit("Audit Visitor", "audit@example.test", "Please send lesson times.");
        assertEquals(2, messages.readAllStrict().size());
    }

    @Test
    void invalidSubmissionDoesNotSaveARecord() {
        assertThrows(ValidationException.class, () -> service.submit("X", "audit@example.test", "Please send lesson times."));
        assertTrue(messages.readAllStrict().isEmpty());
    }

    @Test
    void administratorReadsNewestFirstWithoutChangingRecords() {
        messages.save(new ContactMessage("M001", "Earlier Visitor", "earlier@example.test",
                "Please send lesson times.", LocalDateTime.now(clock).minusDays(1)));
        service.submit("Later Visitor", "later@example.test", "Can we discuss parking lessons?");
        MockHttpServletRequest request = new MockHttpServletRequest();
        assertEquals("Later Visitor", service.messagesForAdmin(request).get(0).getName());
        verify(auth).requireAdmin(request);
        assertEquals(2, messages.readAllStrict().size());
    }

    @Test
    void nonAdministratorCannotReadEnquiries() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN)).when(auth).requireAdmin(request);
        assertThrows(ResponseStatusException.class, () -> service.messagesForAdmin(request));
    }
}
