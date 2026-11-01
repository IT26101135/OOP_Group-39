package com.drivingschool.auth.service;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

import static org.junit.jupiter.api.Assertions.*;

class CsrfTokenServiceTest {

    private final CsrfTokenService service = new CsrfTokenService();

    @Test
    void createsTokenAndReusesItWithinSameSession() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        String first = service.getOrCreateToken(request);
        String second = service.getOrCreateToken(request);

        assertNotNull(request.getSession(false));
        assertTrue(first.matches("[A-Za-z0-9_-]{43}"));
        assertEquals(first, second);
    }

    @Test
    void acceptsMatchingTokenFromAnotherRequestInSameSession() {
        MockHttpServletRequest formRequest = new MockHttpServletRequest();
        String token = service.getOrCreateToken(formRequest);

        MockHttpServletRequest submission = new MockHttpServletRequest();
        submission.setSession(
                (MockHttpSession) formRequest.getSession(false)
        );
        submission.setParameter(CsrfTokenService.PARAMETER_NAME, token);

        assertTrue(service.isValid(submission));
    }

    @Test
    void rejectsRequestWithoutSessionAndDoesNotCreateOne() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter(
                CsrfTokenService.PARAMETER_NAME,
                "A".repeat(43)
        );

        assertFalse(service.isValid(request));
        assertNull(request.getSession(false));
    }

    @Test
    void rejectsMissingBlankMalformedAndDuplicateParameters() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        String token = service.getOrCreateToken(request);

        assertFalse(service.isValid(request));

        request.setParameter(CsrfTokenService.PARAMETER_NAME, "");
        assertFalse(service.isValid(request));

        request.setParameter(CsrfTokenService.PARAMETER_NAME, "invalid");
        assertFalse(service.isValid(request));

        request.setParameter(
                CsrfTokenService.PARAMETER_NAME,
                token,
                token
        );
        assertFalse(service.isValid(request));
    }

    @Test
    void rejectsIncorrectTokenWithValidFormat() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        String token = service.getOrCreateToken(request);

        String changedToken =
                (token.charAt(0) == 'A' ? "B" : "A")
                        + token.substring(1);

        request.setParameter(
                CsrfTokenService.PARAMETER_NAME,
                changedToken
        );

        assertFalse(service.isValid(request));
    }

    @Test
    void rejectsTokenBelongingToAnotherSession() {
        MockHttpServletRequest first = new MockHttpServletRequest();
        MockHttpServletRequest second = new MockHttpServletRequest();

        String firstToken = service.getOrCreateToken(first);
        String secondToken = service.getOrCreateToken(second);

        assertNotEquals(firstToken, secondToken);

        second.setParameter(
                CsrfTokenService.PARAMETER_NAME,
                firstToken
        );

        assertFalse(service.isValid(second));
    }

    @Test
    void rejectsOldTokenAfterSessionReplacement() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        String oldToken = service.getOrCreateToken(request);

        request.getSession(false).invalidate();
        request.setSession(new MockHttpSession());

        request.setParameter(
                CsrfTokenService.PARAMETER_NAME,
                oldToken
        );

        // A new session must not accept a previous session's token.
        assertFalse(service.isValid(request));

        String newToken = service.getOrCreateToken(request);
        assertNotEquals(oldToken, newToken);

        request.setParameter(
                CsrfTokenService.PARAMETER_NAME,
                newToken
        );

        assertTrue(service.isValid(request));
    }
}