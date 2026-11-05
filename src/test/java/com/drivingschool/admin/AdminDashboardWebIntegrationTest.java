package com.drivingschool.admin;

import com.drivingschool.auth.service.AdminSeedService;
import com.drivingschool.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AdminDashboardWebIntegrationTest {

    private static final String DASHBOARD = "/admin/dashboard";
    private static final String PASSWORD = "DashboardTestPass123!";

    @TempDir
    static Path dataDirectory;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("app.data-directory", () -> dataDirectory.toString());
        registry.add("app.admin-seed.enabled", () -> false);
    }

    @Autowired
    MockMvc mvc;

    @Autowired
    AuthService authService;

    @Autowired
    AdminSeedService adminSeedService;

    @Test
    void anonymousVisitorCannotAccessDashboard() throws Exception {
        mvc.perform(get(DASHBOARD))
                .andExpect(status().isUnauthorized())
                .andExpect(view().name("common/error"));
    }

    @Test
    void studentCannotAccessDashboard() throws Exception {
        String email = "dashboard-student-" + UUID.randomUUID() + "@example.com";

        authService.registerStudent(
                "Dashboard Student", email, "0771234567", PASSWORD);

        MockHttpSession session = login(email);

        mvc.perform(get(DASHBOARD).session(session))
                .andExpect(status().isForbidden())
                .andExpect(view().name("common/error"));

        // The student's own profile remains accessible.
        mvc.perform(get("/student/profile").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("navRole", "STUDENT"));
    }

    @Test
    void administratorCanOpenDashboardAndVehiclesThenLogOut() throws Exception {
        String email = "dashboard-admin-" + UUID.randomUUID() + "@example.com";

        assertTrue(adminSeedService.seedIfNeeded(
                "Dashboard Administrator", email, PASSWORD));

        MockHttpSession session = login(email);
        Path accountsFile = dataDirectory.resolve("users.txt");
        byte[] before = Files.readAllBytes(accountsFile);

        MvcResult dashboard = mvc.perform(get(DASHBOARD).session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(model().attribute("navLoggedIn", true))
                .andExpect(model().attribute("navRole", "ADMIN"))
                .andExpect(model().attribute(
                        "navDisplayName", "Dashboard Administrator"))
                .andExpect(model().attribute("navAdminDashboardActive", true))
                .andExpect(model().attributeExists(
                        "csrfParameterName", "csrfToken"))
                .andReturn();

        // Reading the dashboard must not update account records.
        assertArrayEquals(before, Files.readAllBytes(accountsFile));

        mvc.perform(get("/admin/vehicles").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("vehicle/list"));

        // Logout still requires CSRF protection.
        mvc.perform(post("/logout").session(session))
                .andExpect(status().isForbidden());

        mvc.perform(post("/logout")
                        .session(session)
                        .param("_csrf", tokenOf(dashboard)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?loggedOut"));

        assertTrue(session.isInvalid());

        mvc.perform(get(DASHBOARD))
                .andExpect(status().isUnauthorized());
    }

    private MockHttpSession login(String email) throws Exception {
        MvcResult page = mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult result = mvc.perform(post("/login")
                        .session(sessionOf(page))
                        .param("_csrf", tokenOf(page))
                        .param("email", email)
                        .param("password", PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andReturn();

        return sessionOf(result);
    }

    private MockHttpSession sessionOf(MvcResult result) {
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private String tokenOf(MvcResult result) {
        assertNotNull(result.getModelAndView());
        return (String) result.getModelAndView().getModel().get("csrfToken");
    }
}