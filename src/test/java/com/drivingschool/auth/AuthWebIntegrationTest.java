package com.drivingschool.auth;

import com.drivingschool.auth.annotation.RequireAdmin;
import com.drivingschool.auth.annotation.RequireLogin;
import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.auth.service.AdminSeedService;
import com.drivingschool.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(AuthWebIntegrationTest.ProbeConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AuthWebIntegrationTest {

    @TempDir
    static Path dataDirectory;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "app.data-directory",
                () -> dataDirectory.toString()
        );
        registry.add("app.admin-seed.enabled", () -> false);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Autowired
    private AdminSeedService adminSeedService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void registrationLoginNavigationAndLogoutWorkTogether()
            throws Exception {

        String email = uniqueEmail();
        String password = "IntegrationPass123!";

        MvcResult registrationPage = mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andReturn();

        mockMvc.perform(post("/register")
                        .session(sessionOf(registrationPage))
                        .param("_csrf", tokenOf(registrationPage))
                        .param("name", "Integration Student")
                        .param("email", email)
                        .param("phone", "0771234567")
                        .param("password", password)
                        .param("confirmPassword", password))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        assertEquals(
                1,
                userRepository.readAllStrict().stream()
                        .filter(user -> user.getEmail().equals(email))
                        .count()
        );

        MockHttpSession authenticatedSession = login(email, password);

        MvcResult home = mockMvc.perform(
                        get("/").session(authenticatedSession))
                .andExpect(status().isOk())
                .andExpect(model().attribute("navLoggedIn", true))
                .andExpect(model().attribute("navRole", "STUDENT"))
                .andExpect(model().attribute(
                        "navDisplayName", "Integration Student"))
                .andExpect(model().attributeExists("csrfToken"))
                .andReturn();

        mockMvc.perform(get("/integration-probe/account")
                        .session(authenticatedSession))
                .andExpect(status().isOk())
                .andExpect(content().string("account-access"));

        // Even a logged-in user needs a valid CSRF token to log out.
        mockMvc.perform(post("/logout")
                        .session(authenticatedSession))
                .andExpect(status().isForbidden());

        assertFalse(authenticatedSession.isInvalid());

        mockMvc.perform(post("/logout")
                        .session(authenticatedSession)
                        .param("_csrf", tokenOf(home)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?loggedOut"));

        assertTrue(authenticatedSession.isInvalid());

        mockMvc.perform(get("/integration-probe/account"))
                .andExpect(status().isUnauthorized())
                .andExpect(view().name("common/error"));

        mockMvc.perform(get("/"))
                .andExpect(model().attribute("navLoggedIn", false))
                .andExpect(model().attribute("navRole", "PUBLIC"));

        // Logout removes the session, not the persisted account.
        assertTrue(authService.authenticate(email, password).isPresent());
    }

    @Test
    void registrationWithoutCsrfCannotCreateAnAccount()
            throws Exception {

        String email = uniqueEmail();
        int countBefore = userRepository.readAllStrict().size();

        mockMvc.perform(post("/register")
                        .param("name", "Blocked Student")
                        .param("email", email)
                        .param("phone", "0771234567")
                        .param("password", "IntegrationPass123!")
                        .param("confirmPassword", "IntegrationPass123!"))
                .andExpect(status().isForbidden());

        assertEquals(
                countBefore,
                userRepository.readAllStrict().size()
        );
    }

    @Test
    void adminEndpointRejectsAnonymousAndStudentButAllowsAdmin()
            throws Exception {

        mockMvc.perform(get("/integration-probe/admin"))
                .andExpect(status().isUnauthorized())
                .andExpect(view().name("common/error"));

        String studentEmail = uniqueEmail();
        String password = "IntegrationPass123!";

        authService.registerStudent(
                "Access Student",
                studentEmail,
                "0771234567",
                password
        );

        MockHttpSession studentSession =
                login(studentEmail, password);

        mockMvc.perform(get("/integration-probe/admin")
                        .session(studentSession))
                .andExpect(status().isForbidden())
                .andExpect(view().name("common/error"));

        // Explicit test setup in temporary storage.
        // The application's startup seed runner remains disabled.
        String adminEmail = uniqueEmail();

        assertTrue(adminSeedService.seedIfNeeded(
                "Integration Admin",
                adminEmail,
                password
        ));

        MockHttpSession adminSession = login(adminEmail, password);

        mockMvc.perform(get("/integration-probe/admin")
                        .session(adminSession))
                .andExpect(status().isOk())
                .andExpect(content().string("admin-access"));

        mockMvc.perform(get("/").session(adminSession))
                .andExpect(model().attribute("navRole", "ADMIN"));
    }

    @Test
    void failedLoginRefreshesSessionAndAllowsCorrectRetry()
            throws Exception {

        String email = uniqueEmail();
        String password = "IntegrationPass123!";

        authService.registerStudent(
                "Retry Student",
                email,
                "0771234567",
                password
        );

        MvcResult page = mockMvc.perform(get("/login")).andReturn();
        MockHttpSession oldSession = sessionOf(page);
        String oldToken = tokenOf(page);

        MvcResult failedLogin = mockMvc.perform(post("/login")
                        .session(oldSession)
                        .param("_csrf", oldToken)
                        .param("email", email)
                        .param("password", "IncorrectPass123!"))
                .andExpect(status().isUnauthorized())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attribute("navLoggedIn", false))
                .andReturn();

        assertTrue(oldSession.isInvalid());

        MockHttpSession retrySession = sessionOf(failedLogin);
        String retryToken = tokenOf(failedLogin);

        assertNotSame(oldSession, retrySession);
        assertNotEquals(oldToken, retryToken);

        MvcResult success = mockMvc.perform(post("/login")
                        .session(retrySession)
                        .param("_csrf", retryToken)
                        .param("email", email)
                        .param("password", password))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andReturn();

        assertTrue(retrySession.isInvalid());

        mockMvc.perform(get("/integration-probe/account")
                        .session(sessionOf(success)))
                .andExpect(status().isOk());
    }

    private MockHttpSession login(String email, String password)
            throws Exception {

        MvcResult page = mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession anonymousSession = sessionOf(page);

        MvcResult result = mockMvc.perform(post("/login")
                        .session(anonymousSession)
                        .param("_csrf", tokenOf(page))
                        .param("email", email)
                        .param("password", password))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andReturn();

        assertTrue(anonymousSession.isInvalid());

        MockHttpSession authenticatedSession = sessionOf(result);
        assertNotNull(authenticatedSession);
        assertNotSame(anonymousSession, authenticatedSession);

        return authenticatedSession;
    }

    private String uniqueEmail() {
        return "test-" + UUID.randomUUID() + "@example.com";
    }

    private MockHttpSession sessionOf(MvcResult result) {
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private String tokenOf(MvcResult result) {
        return (String) result.getModelAndView()
                .getModel().get("csrfToken");
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class ProbeConfiguration {

        @Bean
        AccessProbeController accessProbeController() {
            return new AccessProbeController();
        }
    }

    @Controller
    static class AccessProbeController {

        @RequireLogin
        @ResponseBody
        @GetMapping("/integration-probe/account")
        public String account() {
            return "account-access";
        }

        @RequireAdmin
        @ResponseBody
        @GetMapping("/integration-probe/admin")
        public String admin() {
            return "admin-access";
        }
    }
}