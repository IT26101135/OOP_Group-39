package com.drivingschool.auth.filter;

import com.drivingschool.auth.service.CsrfTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import static org.junit.jupiter.api.Assertions.*;

class CsrfFilterTest {

    private final CsrfTokenService tokenService =
            new CsrfTokenService();

    private final CsrfFilter filter =
            new CsrfFilter(tokenService);

    @Test
    void allowsSafeMethodsWithoutCreatingSession() throws Exception {
        for (String method : new String[]{
                "GET", "HEAD", "OPTIONS", "TRACE"
        }) {
            MockHttpServletRequest request =
                    new MockHttpServletRequest(method, "/");

            MockHttpServletResponse response =
                    new MockHttpServletResponse();

            MockFilterChain chain = new MockFilterChain();

            filter.doFilter(request, response, chain);

            assertSame(request, chain.getRequest(), method);
            assertNull(request.getSession(false), method);
        }
    }

    @Test
    void rejectsUnsafeMethodsWithoutSession() throws Exception {
        for (String method : new String[]{
                "POST", "PUT", "PATCH", "DELETE"
        }) {
            MockHttpServletRequest request =
                    new MockHttpServletRequest(method, "/example");

            MockHttpServletResponse response =
                    new MockHttpServletResponse();

            MockFilterChain chain = new MockFilterChain();

            filter.doFilter(request, response, chain);

            assertEquals(403, response.getStatus(), method);
            assertNull(chain.getRequest(), method);
            assertNull(request.getSession(false), method);
        }
    }

    @Test
    void rejectsAuthPostsWithoutTokenEvenWhenSessionExists()
            throws Exception {

        for (String path : new String[]{
                "/register", "/login", "/logout"
        }) {
            MockHttpServletRequest request =
                    new MockHttpServletRequest("POST", path);

            tokenService.getOrCreateToken(request);

            MockHttpServletResponse response =
                    new MockHttpServletResponse();

            MockFilterChain chain = new MockFilterChain();

            filter.doFilter(request, response, chain);

            assertEquals(403, response.getStatus(), path);
            assertNull(chain.getRequest(), path);
        }
    }

    @Test
    void allowsPostWithMatchingSessionToken() throws Exception {
        MockHttpServletRequest formRequest =
                new MockHttpServletRequest("GET", "/register");

        String token = tokenService.getOrCreateToken(formRequest);

        MockHttpServletRequest submission =
                new MockHttpServletRequest("POST", "/register");

        submission.setSession(
                (MockHttpSession) formRequest.getSession(false)
        );

        submission.setParameter(
                CsrfTokenService.PARAMETER_NAME,
                token
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(submission, response, chain);

        assertSame(submission, chain.getRequest());
        assertEquals(200, response.getStatus());
    }

    @Test
    void rejectsTokenFromDifferentSession() throws Exception {
        MockHttpServletRequest otherSession =
                new MockHttpServletRequest();

        String otherToken =
                tokenService.getOrCreateToken(otherSession);

        MockHttpServletRequest submission =
                new MockHttpServletRequest("POST", "/login");

        tokenService.getOrCreateToken(submission);

        submission.setParameter(
                CsrfTokenService.PARAMETER_NAME,
                otherToken
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(submission, response, chain);

        assertEquals(403, response.getStatus());
        assertNull(chain.getRequest());
    }

    @Test
    void rejectsDuplicateTokenParameters() throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest("POST", "/logout");

        String token = tokenService.getOrCreateToken(request);

        request.setParameter(
                CsrfTokenService.PARAMETER_NAME,
                token,
                token
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(403, response.getStatus());
        assertNull(chain.getRequest());
    }
}