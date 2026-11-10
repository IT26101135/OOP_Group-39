// Module owner: IT26101135
package com.drivingschool.admin;

import com.drivingschool.auth.service.AdminSeedService;
import com.drivingschool.auth.service.AuthService;
import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.auth.model.AccountStatus;
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

    @Autowired UserRepository users;

    @Test
    void anonymousVisitorCannotAccessDashboard() throws Exception {
        mvc.perform(get(DASHBOARD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?required"));
    }

    @Test
    void studentCannotAccessDashboard() throws Exception {
        String email = "dashboard-student-" + UUID.randomUUID() + "@example.com";

        com.drivingschool.student.model.Student registered = authService.registerStudent(
                "Dashboard Student", email, "0771234567", PASSWORD);
        registered.setStatus(AccountStatus.ACTIVE); users.update(registered);

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
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login?required"));
    }

    @Test
    void visitorEnquiryReachesOnlyTheProtectedDashboardAndRetriesDoNotDuplicate() throws Exception {
        MvcResult form = mvc.perform(get("/contact")).andExpect(status().isOk()).andReturn();
        String text = "Please discuss parking lessons <script>alert(1)</script>";
        for (int attempt = 0; attempt < 2; attempt++) {
            mvc.perform(post("/contact").session(sessionOf(form)).param("_csrf", tokenOf(form))
                    .param("name", "Enquiry Visitor").param("email", "enquiry@example.test").param("message", text))
                    .andExpect(redirectedUrl("/contact")).andExpect(flash().attribute("messageSent", true));
        }
        String email = "enquiry-admin-" + UUID.randomUUID() + "@example.test";
        String temporaryAdminId = users.nextId("A");
        users.save(new com.drivingschool.auth.model.Admin(temporaryAdminId, "Enquiry Administrator", email,
                new com.drivingschool.auth.util.PasswordHasher().hash(PASSWORD)));
        try {
        MvcResult dashboard = mvc.perform(get(DASHBOARD).session(login(email)))
                .andExpect(status().isOk()).andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(model().attributeExists("contactMessages")).andReturn();
        java.util.List<?> messages = (java.util.List<?>) dashboard.getModelAndView().getModel().get("contactMessages");
        assertEquals(1, messages.stream().filter(item ->
                ((com.drivingschool.common.model.ContactMessage) item).getEmail().equals("enquiry@example.test")).count());
        MvcResult publicPage = mvc.perform(get("/contact")).andReturn();
        assertFalse(publicPage.getModelAndView().getModel().containsKey("contactMessages"));
        mvc.perform(post("/contact").param("name", "Visitor").param("email", "visitor@example.test")
                .param("message", "A message without a token" )).andExpect(status().isForbidden());
        } finally {
            users.delete(temporaryAdminId);
        }
    }

    @Test
    void invalidEnquiryKeepsTypedFieldsOnContactForm() throws Exception {
        MvcResult form = mvc.perform(get("/contact")).andReturn();
        mvc.perform(post("/contact").session(sessionOf(form)).param("_csrf", tokenOf(form))
                .param("name", "Visitor").param("email", "invalid-email").param("message", "Keep this typed question."))
                .andExpect(status().isBadRequest()).andExpect(view().name("public/contact"))
                .andExpect(model().attribute("contactMessage", "Keep this typed question."))
                .andExpect(model().attribute("contactEmail", "invalid-email"))
                .andExpect(model().attributeExists("formError", "csrfToken"));
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
                .andExpect(redirectedUrl(users.readAllStrict().stream().filter(u -> u.getEmail().equalsIgnoreCase(email)).findFirst().orElseThrow().getDashboardPath()))
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
