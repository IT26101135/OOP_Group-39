package com.drivingschool.instructor;

import com.drivingschool.auth.model.Role;
import com.drivingschool.auth.model.User;
import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.auth.service.AdminSeedService;
import com.drivingschool.auth.service.AuthService;
import com.drivingschool.instructor.model.Instructor;
import com.drivingschool.instructor.model.InstructorAvailability;
import com.drivingschool.instructor.repository.InstructorRepository;
import com.drivingschool.instructor.repository.InstructorAvailabilityRepository;
import com.drivingschool.instructor.service.InstructorService;
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

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class InstructorWebIntegrationTest {
    private static final String ROOT = "/admin/instructors";
    private static final String CREATE = ROOT + "/create";
    private static final String PASSWORD = "InstructorTestPass123!";

    @TempDir
    static Path dataDirectory;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("app.data-directory", () -> dataDirectory.toString());
        registry.add("app.admin-seed.enabled", () -> false);
    }

    @Autowired MockMvc mvc;
    @Autowired AuthService authService;
    @Autowired AdminSeedService adminSeedService;
    @Autowired UserRepository userRepository;
    @Autowired InstructorRepository instructorRepository;
    @Autowired InstructorAvailabilityRepository availabilityRepository;
    @Autowired InstructorService instructorService;

    @Test
    void anonymousUsersCannotReadOrChangeInstructorPages() throws Exception {
        MvcResult loginPage = mvc.perform(get("/login"))
                .andExpect(status().isOk()).andReturn();
        MockHttpSession session = sessionOf(loginPage);
        String csrf = tokenOf(loginPage);
        for (String path : List.of(ROOT, CREATE, ROOT + "/I999/edit",
                ROOT + "/I999/availability/create")) {
            mvc.perform(get(path)).andExpect(status().isUnauthorized());
        }
        for (String path : List.of(CREATE, ROOT + "/I999/edit",
                ROOT + "/I999/availability/create")) {
            mvc.perform(post(path).session(session).param("_csrf", csrf))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    void studentCannotReadOrChangeInstructorPages() throws Exception {
        var student = authService.registerStudent("Instructor Test Student",
                email(), "0771234567", PASSWORD);
        MockHttpSession session = login(student.getEmail());
        MvcResult home = mvc.perform(get("/").session(session))
                .andExpect(status().isOk()).andReturn();
        String csrf = tokenOf(home);
        int before = instructorRepository.readAllStrict().size();
        for (String path : List.of(ROOT, CREATE, ROOT + "/I999/edit",
                ROOT + "/I999/availability/create")) {
            mvc.perform(get(path).session(session))
                    .andExpect(status().isForbidden());
        }
        for (String path : List.of(CREATE, ROOT + "/I999/edit",
                ROOT + "/I999/availability/create")) {
            mvc.perform(post(path).session(session).param("_csrf", csrf))
                    .andExpect(status().isForbidden());
        }
        assertEquals(before, instructorRepository.readAllStrict().size());
    }

    @Test
    void administratorCanViewUncachedList() throws Exception {
        MockHttpSession session = loginAsAdmin();
        mvc.perform(get(ROOT).session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("instructor/list"))
                .andExpect(model().attributeExists("instructors"))
                .andExpect(header().string("Cache-Control", "no-store"));
    }

    @Test
    void createIgnoresTamperedIdentityAndActiveFlag() throws Exception {
        MockHttpSession session = loginAsAdmin();
        String address = email();
        int before = instructorRepository.readAllStrict().size();
        MvcResult page = mvc.perform(get(CREATE).session(session))
                .andExpect(status().isOk()).andReturn();
        mvc.perform(post(CREATE).session(session)
                        .param("_csrf", tokenOf(page))
                        .param("name", "New Instructor")
                        .param("email", address)
                        .param("phone", "0771234567")
                        .param("yearsOfExperience", "5")
                        .param("id", "I999")
                        .param("active", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ROOT))
                .andExpect(flash().attribute("instructorCreated", true));
        Instructor stored = findByEmail(address);
        assertEquals(before + 1, instructorRepository.readAllStrict().size());
        assertTrue(stored.getId().matches("I(?!000)[0-9]{3}"));
        assertNotEquals("I999", stored.getId());
        assertTrue(stored.isActive());
    }

    @Test
    void duplicateEmailReturnsConflictWithoutNewRecord() throws Exception {
        MockHttpSession session = loginAsAdmin();
        Instructor existing = createInstructor(4);
        int before = instructorRepository.readAllStrict().size();
        MvcResult page = mvc.perform(get(CREATE).session(session)).andReturn();
        mvc.perform(post(CREATE).session(session)
                        .param("_csrf", tokenOf(page))
                        .param("name", "Another Instructor")
                        .param("email", existing.getEmail())
                        .param("phone", "0771234567")
                        .param("yearsOfExperience", "6"))
                .andExpect(status().isConflict())
                .andExpect(view().name("instructor/create"))
                .andExpect(model().attributeExists("formError", "csrfToken"));
        assertEquals(before, instructorRepository.readAllStrict().size());
    }

    @Test
    void editChangesOnlyAllowedFields() throws Exception {
        MockHttpSession session = loginAsAdmin();
        Instructor original = createInstructor(4);
        String changedEmail = email();
        MvcResult page = mvc.perform(get(ROOT + "/" + original.getId() + "/edit")
                .session(session)).andExpect(status().isOk()).andReturn();
        mvc.perform(post(ROOT + "/" + original.getId() + "/edit")
                        .session(session).param("_csrf", tokenOf(page))
                        .param("name", "Edited Instructor")
                        .param("email", changedEmail)
                        .param("phone", "0777654321")
                        .param("yearsOfExperience", "8")
                        .param("id", "I999")
                        .param("active", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ROOT))
                .andExpect(flash().attribute("instructorUpdated", true));
        Instructor stored = instructorRepository.findByIdStrict(original.getId())
                .orElseThrow();
        assertEquals(original.getId(), stored.getId());
        assertTrue(stored.isActive());
        assertEquals("Edited Instructor", stored.getName());
        assertEquals(changedEmail, stored.getEmail());
        assertEquals("0777654321", stored.getPhone());
        assertEquals(8, stored.getYearsOfExperience());
    }

    @Test
    void validAvailabilityPersistsAndRedirects() throws Exception {
        MockHttpSession session = loginAsAdmin();
        Instructor instructor = createInstructor(5);
        String path = ROOT + "/" + instructor.getId() + "/availability/create";
        int before = availabilityRepository.readAllStrict().size();
        MvcResult page = mvc.perform(get(path).session(session))
                .andExpect(status().isOk()).andReturn();
        mvc.perform(post(path).session(session).param("_csrf", tokenOf(page))
                        .param("start", "2026-11-10T09:00")
                        .param("end", "2026-11-10T10:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ROOT))
                .andExpect(flash().attribute("availabilityCreated", true));
        List<InstructorAvailability> saved = availabilityRepository.readAllStrict();
        assertEquals(before + 1, saved.size());
        InstructorAvailability record = saved.stream()
                .filter(a -> a.getInstructorId().equals(instructor.getId()))
                .findFirst().orElseThrow();
        assertTrue(record.getId().matches("T(?!000)[0-9]{3}"));
        assertEquals(LocalDateTime.parse("2026-11-10T09:00"), record.getStartTime());
        assertEquals(LocalDateTime.parse("2026-11-10T10:00"), record.getEndTime());
    }

    @Test
    void reversedAvailabilityTimesAreRejectedWithoutSaving() throws Exception {
        MockHttpSession session = loginAsAdmin();
        Instructor instructor = createInstructor(5);
        String path = ROOT + "/" + instructor.getId() + "/availability/create";
        int before = availabilityRepository.readAllStrict().size();
        MvcResult page = mvc.perform(get(path).session(session)).andReturn();
        mvc.perform(post(path).session(session).param("_csrf", tokenOf(page))
                        .param("start", "2026-11-10T11:00")
                        .param("end", "2026-11-10T10:00"))
                .andExpect(status().isBadRequest())
                .andExpect(view().name("instructor/availability-create"))
                .andExpect(model().attributeExists("formError"));
        assertEquals(before, availabilityRepository.readAllStrict().size());
    }

    @Test
    void inactiveInstructorCannotReceiveAvailability() throws Exception {
        MockHttpSession session = loginAsAdmin();
        Instructor inactive = new Instructor(instructorRepository.nextId("I"),
                "Inactive Instructor", email(), "0771234567", 3, false);
        instructorRepository.save(inactive);
        int before = availabilityRepository.readAllStrict().size();
        String path = ROOT + "/" + inactive.getId() + "/availability/create";
        MvcResult page = mvc.perform(get(CREATE).session(session)).andReturn();
        mvc.perform(post(path).session(session).param("_csrf", tokenOf(page))
                        .param("start", "2026-11-10T09:00")
                        .param("end", "2026-11-10T10:00"))
                .andExpect(status().isBadRequest());
        assertEquals(before, availabilityRepository.readAllStrict().size());
    }

    @Test
    void missingAndInvalidInstructorIdsAreRejected() throws Exception {
        MockHttpSession session = loginAsAdmin();
        mvc.perform(get(ROOT + "/I999/edit").session(session))
                .andExpect(status().isNotFound());
        MvcResult invalid = mvc.perform(get(ROOT + "/abc/edit").session(session))
                .andReturn();
        assertTrue(invalid.getResponse().getStatus() == 400
                        || invalid.getResponse().getStatus() == 404,
                "Unexpected status for abc/edit: " + invalid.getResponse().getStatus());
        System.out.println("Instructor abc/edit status: "
                + invalid.getResponse().getStatus());
    }

    @Test
    void listIsSortedByDescendingExperience() throws Exception {
        MockHttpSession session = loginAsAdmin();
        Instructor junior = createInstructor(2);
        Instructor senior = createInstructor(9);
        MvcResult result = mvc.perform(get(ROOT).session(session))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("instructors"))
                .andReturn();
        @SuppressWarnings("unchecked")
        List<Instructor> instructors = (List<Instructor>) result
                .getModelAndView().getModel().get("instructors");
        int juniorIndex = -1;
        int seniorIndex = -1;
        for (int i = 0; i < instructors.size(); i++) {
            if (instructors.get(i).getId().equals(junior.getId())) juniorIndex = i;
            if (instructors.get(i).getId().equals(senior.getId())) seniorIndex = i;
        }
        assertTrue(juniorIndex >= 0 && seniorIndex >= 0);
        assertTrue(seniorIndex < juniorIndex,
                "Expected 9 years of experience before 2 years");
    }

    private Instructor createInstructor(int experience) {
        return instructorService.createInstructor("Instructor Test Person",
                email(), "0771234567", experience);
    }

    private Instructor findByEmail(String address) {
        return instructorRepository.readAllStrict().stream()
                .filter(i -> i.getEmail().equals(address))
                .findFirst().orElseThrow();
    }

    private String email() {
        return "instructor-" + UUID.randomUUID() + "@example.com";
    }

    private MockHttpSession loginAsAdmin() throws Exception {
        User existingAdmin = userRepository.readAllStrict().stream()
                .filter(user -> user.getRole() == Role.ADMIN)
                .findFirst().orElse(null);
        String address;
        if (existingAdmin == null) {
            address = email();
            assertTrue(adminSeedService.seedIfNeeded(
                    "Instructor Test Admin", address, PASSWORD));
        } else {
            address = existingAdmin.getEmail();
        }
        return login(address);
    }

    private MockHttpSession login(String address) throws Exception {
        MvcResult page = mvc.perform(get("/login"))
                .andExpect(status().isOk()).andReturn();
        MvcResult result = mvc.perform(post("/login")
                        .session(sessionOf(page))
                        .param("_csrf", tokenOf(page))
                        .param("email", address)
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
        return (String) result.getModelAndView().getModel().get("csrfToken");
    }
}
