package com.drivingschool.student;

import com.drivingschool.auth.model.Role;
import com.drivingschool.auth.model.User;
import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.auth.service.AdminSeedService;
import com.drivingschool.auth.service.AuthService;
import com.drivingschool.student.dto.StudentProfile;
import com.drivingschool.student.form.StudentProfileForm;
import com.drivingschool.student.model.Student;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class StudentProfileWebIntegrationTest {
    private static final String PROFILE = "/student/profile";
    private static final String EDIT = "/student/profile/edit";
    private static final String PASSWORD = "ProfileTestPass123!";

    @TempDir static Path dataDirectory;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("app.data-directory", () -> dataDirectory.toString());
        registry.add("app.admin-seed.enabled", () -> false);
    }

    @Autowired MockMvc mvc;
    @Autowired AuthService authService;
    @Autowired AdminSeedService adminSeedService;
    @Autowired UserRepository repository;
    @Autowired ObjectMapper mapper;

    @Test
    void anonymousGetAndValidCsrfPostAreUnauthorized() throws Exception {
        mvc.perform(get(PROFILE)).andExpect(status().isUnauthorized())
                .andExpect(view().name("common/error"));
        mvc.perform(get(EDIT)).andExpect(status().isUnauthorized());
        MvcResult anonymous = mvc.perform(get("/login")).andReturn();
        mvc.perform(edit(sessionOf(anonymous), tokenOf(anonymous), "Anonymous User", email(), "0771234567"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post(EDIT)).andExpect(status().isForbidden());
    }

    @Test
    void administratorCannotReadOrUpdateStudentProfile() throws Exception {
        String email = email();
        assertTrue(adminSeedService.seedIfNeeded("Profile Test Admin", email, PASSWORD));
        MockHttpSession session = login(email);
        MvcResult home = mvc.perform(get("/").session(session))
                .andExpect(model().attribute("navRole", "ADMIN")).andReturn();
        mvc.perform(get(PROFILE).session(session)).andExpect(status().isForbidden());
        mvc.perform(get(EDIT).session(session)).andExpect(status().isForbidden());
        mvc.perform(edit(session, tokenOf(home), "Blocked Change", email(), "0771234567"))
                .andExpect(status().isForbidden());
        assertEquals("Profile Test Admin", authService.authenticate(email, PASSWORD).orElseThrow().getName());
    }

    @Test
    void studentSeesSafeProfileAndPrefilledForm() throws Exception {
        Student student = createStudent();
        MockHttpSession session = login(student.getEmail());
        MvcResult page = mvc.perform(get(PROFILE).session(session))
                .andExpect(status().isOk()).andExpect(view().name("student/profile"))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(model().attribute("navRole", "STUDENT"))
                .andExpect(model().attribute("navProfileActive", true))
                .andReturn();
        StudentProfile profile = (StudentProfile) page.getModelAndView().getModel().get("profile");
        assertEquals(student.getId(), profile.getId());
        assertEquals(student.getEmail(), profile.getEmail());
        assertEquals(4, mapper.valueToTree(profile).size());
        assertFalse(mapper.valueToTree(profile).has("passwordHash"));
        MvcResult formPage = editPage(session);
        StudentProfileForm form = (StudentProfileForm) formPage.getModelAndView().getModel().get("profileForm");
        assertEquals(student.getName(), form.getName());
        assertEquals(student.getEmail(), form.getEmail());
        assertEquals(student.getPhone(), form.getPhone());
        assertNotNull(tokenOf(formPage));
    }

    @Test
    void successfulUpdateRedirectsWithoutPrivateQueryValuesAndRefreshesNavigation() throws Exception {
        Student student = createStudent();
        MockHttpSession session = login(student.getEmail());
        String newEmail = email();
        MvcResult page = editPage(session);
        MvcResult update = mvc.perform(edit(session, tokenOf(page), "  Updated Student  ",
                        " " + newEmail.toUpperCase(java.util.Locale.ROOT) + " ", " +94770000000 "))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(PROFILE))
                .andExpect(flash().attribute("profileUpdated", true))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andReturn();
        assertFalse(session.isInvalid());
        Student stored = (Student) repository.findByIdStrict(student.getId()).orElseThrow();
        assertEquals(newEmail, stored.getEmail());
        assertEquals("+94770000000", stored.getPhone());
        assertEquals(student.getPasswordHash(), stored.getPasswordHash());
        mvc.perform(get(PROFILE).session(session).flashAttrs(update.getFlashMap()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("navDisplayName", "Updated Student"))
                .andExpect(model().attribute("profileUpdated", true));
        assertTrue(authService.authenticate(newEmail, PASSWORD).isPresent());
        assertTrue(authService.authenticate(student.getEmail(), PASSWORD).isEmpty());
    }

    @Test
    void browserIdentityRoleAndPasswordFieldsCannotChangeTargetAccount() throws Exception {
        Student owner = createStudent();
        Student other = createStudent();
        MockHttpSession session = login(owner.getEmail());
        MvcResult page = editPage(session);
        mvc.perform(edit(session, tokenOf(page), "Owner Updated", owner.getEmail(), "0771234567")
                        .param("id", other.getId()).param("studentId", other.getId())
                        .param("role", "ADMIN").param("passwordHash", "attacker-value")
                        .param("password", "attacker-password"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl(PROFILE));
        User storedOwner = repository.findByIdStrict(owner.getId()).orElseThrow();
        User storedOther = repository.findByIdStrict(other.getId()).orElseThrow();
        assertEquals("Owner Updated", storedOwner.getName());
        assertEquals(Role.STUDENT, storedOwner.getRole());
        assertEquals(owner.getPasswordHash(), storedOwner.getPasswordHash());
        assertEquals(other.getName(), storedOther.getName());
        assertEquals(other.getEmail(), storedOther.getEmail());
        assertEquals(other.getPasswordHash(), storedOther.getPasswordHash());
    }

    @Test
    void invalidFieldsRedisplayFormWithTokenAndDoNotPersist() throws Exception {
        Student student = createStudent();
        MockHttpSession session = login(student.getEmail());
        MvcResult page = editPage(session);
        String[][] invalid = {{"X", student.getEmail(), "0771234567"},
                {"Good Name", "not-an-email", "0771234567"},
                {"Good Name", student.getEmail(), "12"}};
        for (String[] values : invalid) {
            MvcResult failed = mvc.perform(edit(session, tokenOf(page), values[0], values[1], values[2]))
                    .andExpect(status().isBadRequest()).andExpect(view().name("student/profile-edit"))
                    .andExpect(model().attributeExists("formError", "csrfToken", "profileForm"))
                    .andExpect(header().string("Cache-Control", "no-store")).andReturn();
            StudentProfileForm form = (StudentProfileForm) failed.getModelAndView().getModel().get("profileForm");
            assertEquals(values[0], form.getName());
            assertEquals(values[1], form.getEmail());
            assertEquals(values[2], form.getPhone());
            Student stored = (Student) repository.findByIdStrict(student.getId()).orElseThrow();
            assertEquals(student.getName(), stored.getName());
            assertEquals(student.getEmail(), stored.getEmail());
            assertEquals(student.getPhone(), stored.getPhone());
        }
    }

    @Test
    void duplicateEmailReturnsConflictAndAllowsRetry() throws Exception {
        Student student = createStudent();
        Student other = createStudent();
        MockHttpSession session = login(student.getEmail());
        MvcResult page = editPage(session);
        MvcResult failed = mvc.perform(edit(session, tokenOf(page), "New Name", other.getEmail(), "0771234567"))
                .andExpect(status().isConflict()).andExpect(view().name("student/profile-edit"))
                .andExpect(model().attributeExists("formError", "csrfToken")).andReturn();
        assertEquals(student.getEmail(), repository.findByIdStrict(student.getId()).orElseThrow().getEmail());
        mvc.perform(edit(session, tokenOf(failed), "New Name", student.getEmail(), "0771234567"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void missingInvalidDuplicateAndOtherSessionTokensCannotWrite() throws Exception {
        Student student = createStudent();
        MockHttpSession session = login(student.getEmail());
        String token = tokenOf(editPage(session));
        MvcResult foreignPage = mvc.perform(get("/login")).andReturn();
        mvc.perform(post(EDIT).session(session).param("name", "Blocked Name"))
                .andExpect(status().isForbidden());
        mvc.perform(edit(session, "invalid", "Blocked Name", student.getEmail(), "0771234567"))
                .andExpect(status().isForbidden());
        mvc.perform(edit(session, tokenOf(foreignPage), "Blocked Name", student.getEmail(), "0771234567"))
                .andExpect(status().isForbidden());
        mvc.perform(post(EDIT).session(session).param("_csrf", token, token)
                        .param("name", "Blocked Name").param("email", student.getEmail()).param("phone", "0771234567"))
                .andExpect(status().isForbidden());
        assertEquals(student.getName(), repository.findByIdStrict(student.getId()).orElseThrow().getName());
    }

    @Test
    void getEditDoesNotMutateAndMissingAccountLosesAccess() throws Exception {
        Student student = createStudent();
        MockHttpSession session = login(student.getEmail());
        byte[] before = Files.readAllBytes(dataDirectory.resolve("users.txt"));
        mvc.perform(get(EDIT).session(session).param("name", "GET must not save"))
                .andExpect(status().isOk());
        assertArrayEquals(before, Files.readAllBytes(dataDirectory.resolve("users.txt")));
        repository.delete(student.getId()); // Test fixture only; not a deletion feature.
        mvc.perform(get(PROFILE).session(session)).andExpect(status().isUnauthorized());
        assertTrue(session.isInvalid());
    }

    @Test
    void corruptAccountStorageProducesGenericServerError() throws Exception {
        Student student = createStudent();
        MockHttpSession session = login(student.getEmail());
        String token = tokenOf(editPage(session));
        Path file = dataDirectory.resolve("users.txt");
        byte[] before = Files.readAllBytes(file);
        try {
            Files.writeString(file, "invalid-json-test-fixture\n");
            MvcResult failed = mvc.perform(edit(session, token, "New Name", student.getEmail(), "0771234567"))
                    .andExpect(status().isInternalServerError()).andExpect(view().name("common/error"))
                    .andReturn();
            assertEquals("We could not complete the operation. Please contact the administrator.",
                    failed.getModelAndView().getModel().get("message"));
        } finally {
            Files.write(file, before);
        }
    }

    private Student createStudent() {
        return authService.registerStudent("Profile Learner", email(), "0771234567", PASSWORD);
    }
    private String email() { return "profile-" + UUID.randomUUID() + "@example.com"; }
    private MvcResult editPage(MockHttpSession session) throws Exception {
        return mvc.perform(get(EDIT).session(session)).andExpect(status().isOk())
                .andExpect(view().name("student/profile-edit")).andReturn();
    }
    private MockHttpServletRequestBuilder edit(MockHttpSession session, String token,
                                               String name, String email, String phone) {
        return post(EDIT).session(session).param("_csrf", token)
                .param("name", name).param("email", email).param("phone", phone);
    }
    private MockHttpSession login(String email) throws Exception {
        MvcResult page = mvc.perform(get("/login")).andExpect(status().isOk()).andReturn();
        MvcResult result = mvc.perform(post("/login").session(sessionOf(page))
                        .param("_csrf", tokenOf(page)).param("email", email).param("password", PASSWORD))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/")).andReturn();
        return sessionOf(result);
    }
    private MockHttpSession sessionOf(MvcResult result) {
        return (MockHttpSession) result.getRequest().getSession(false);
    }
    private String tokenOf(MvcResult result) {
        return (String) result.getModelAndView().getModel().get("csrfToken");
    }
}
