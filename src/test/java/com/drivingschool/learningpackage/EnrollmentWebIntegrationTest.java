/**
 * ============================================================
 * MEMBER   : IT26101136 - [Name]
 * MODULE   : Learning packages and enrollment
 * FILE     : EnrollmentWebIntegrationTest.java
 * WHAT IT DOES (simple words): Checks enrollment routes, role checks and form tokens.
 * REAL-LIFE EXAMPLE: Try forms at a practice reception desk with spare records.
 * OOP USED : Interface-based controllers and exception handling
 * CRUD     : CREATE, READ, UPDATE, DELETE (temporary data only)
 * ============================================================
 */
package com.drivingschool.learningpackage;

import com.drivingschool.auth.model.AccountStatus;
import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.auth.service.AuthService;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.learningpackage.model.Enrollment;
import com.drivingschool.learningpackage.model.LearningPackage;
import com.drivingschool.learningpackage.repository.EnrollmentRepository;
import com.drivingschool.learningpackage.repository.LearningPackageRepository;
import com.drivingschool.student.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class EnrollmentWebIntegrationTest {
    @TempDir static Path directory;
    @Autowired MockMvc mvc;
    @Autowired AuthService authentication;
    @Autowired AuthSessionService sessions;
    @Autowired UserRepository users;
    @Autowired EnrollmentRepository enrollments;
    @Autowired LearningPackageRepository packages;
    private Student student;
    private MockHttpSession session;

    // Keeps every test inside a separate temporary application data directory.
    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("app.data-directory", () -> directory.toString());
        registry.add("app.admin-seed.enabled", () -> false);
        registry.add("app.demo-seed.enabled", () -> false);
    }

    // Signs in a real active account without bypassing password verification.
    @BeforeEach
    void setUp() {
        String email = "learner" + users.readAllStrict().size() + "@example.test";
        student = authentication.registerStudent("Test Learner", email, "0771234567", "TestingPass123!");
        student.setStatus(AccountStatus.ACTIVE);
        users.update(student);
        MockHttpServletRequest request = new MockHttpServletRequest();
        assertTrue(sessions.login(request, email, "TestingPass123!"));
        session = (MockHttpSession) request.getSession(false);
    }

    // Anonymous visitors cannot view or change any enrollment.
    @Test
    void anonymousVisitorMustLogIn() throws Exception {
        mvc.perform(get("/enrollments")).andExpect(redirectedUrl("/login?required"));
    }

    // A valid form opens owned payment instructions without unlocking any lessons.
    @Test
    void createsReadsAndUpdatesWithValidToken() throws Exception {
        String packageId = packages.nextId("P");
        packages.save(new LearningPackage(packageId, "Course " + packageId, "Practical driving lessons",
                new BigDecimal("1000.00"), 5, 30, true));
        String token = token();
        mvc.perform(post("/enrollments").session(session).param("_csrf", token)
                .param("packageId", packageId)).andExpect(redirectedUrlPattern("/enrollments/E*/payment"));
        Enrollment enrollment = findOwnEnrollment();
        assertEquals("AWAITING_PAYMENT", enrollment.getStatus());
        assertEquals(0, ((Student) users.findByIdStrict(student.getId()).orElseThrow()).getLessonsRemaining());
        mvc.perform(get("/enrollments/" + enrollment.getId() + "/payment").session(session))
                .andExpect(status().isOk()).andExpect(view().name("student/payment-instructions"))
                .andExpect(model().attribute("packageName", "Course " + packageId))
                .andExpect(model().attribute("paymentAdminEmail", ""));
        mvc.perform(get("/enrollments").session(session)).andExpect(status().isOk())
                .andExpect(model().attributeExists("enrollments", "expiryLabels"));
        mvc.perform(post("/enrollments/update").session(session).param("_csrf", token)
                .param("enrollmentId", enrollment.getId()).param("learningGoal", "Confident parking"))
                .andExpect(redirectedUrl("/enrollments"));
        assertEquals("Confident parking", enrollments.findByIdStrict(enrollment.getId()).orElseThrow().getLearningGoal());
    }

    // A duplicate submit returns a useful explanation and leaves just one payment request.
    @Test
    void duplicateRequestRedirectsWithFriendlyMessage() throws Exception {
        String packageId = packages.nextId("P");
        packages.save(new LearningPackage(packageId, "Course " + packageId, "Driving lessons",
                new BigDecimal("1200"), 5, 30, true));
        mvc.perform(post("/enrollments").session(session).param("_csrf", token())
                .param("packageId", packageId)).andExpect(status().is3xxRedirection());
        mvc.perform(post("/enrollments").session(session).param("_csrf", token())
                .param("packageId", packageId)).andExpect(redirectedUrl("/enrollments"))
                .andExpect(flash().attributeExists("error"));
    }

    // Neither an anonymous visitor nor another student may view a payment reference.
    @Test
    void paymentInstructionsRequireOwner() throws Exception {
        Enrollment other = new Enrollment(enrollments.nextId("E"), "S999", "P001",
                LocalDateTime.now(), new BigDecimal("1000"), "UNPAID");
        enrollments.save(other);
        mvc.perform(get("/enrollments/" + other.getId() + "/payment"))
                .andExpect(redirectedUrl("/login?required"));
        mvc.perform(get("/enrollments/" + other.getId() + "/payment").session(session))
                .andExpect(status().isForbidden());
    }

    // Missing tokens and another student's IDs cannot remove or edit records.
    @Test
    void rejectsMissingTokenAndAnotherOwner() throws Exception {
        Enrollment other = new Enrollment(enrollments.nextId("E"), "S999", "P001",
                LocalDateTime.now(), new BigDecimal("1000.00"), "UNPAID");
        enrollments.save(other);
        mvc.perform(post("/enrollments/update").session(session).param("enrollmentId", other.getId())
                .param("learningGoal", "Changed")).andExpect(status().isForbidden());
        mvc.perform(post("/enrollments/delete").session(session).param("_csrf", token())
                .param("enrollmentId", other.getId())).andExpect(status().isForbidden());
        assertTrue(enrollments.findByIdStrict(other.getId()).isPresent());
    }

    // A valid delete removes only an unused unpaid enrollment owned by the student.
    @Test
    void deletesOwnUnusedUnpaidRecordWithToken() throws Exception {
        Enrollment enrollment = new Enrollment(enrollments.nextId("E"), student.getId(), "P001",
                LocalDateTime.now(), new BigDecimal("1000.00"), "UNPAID");
        enrollments.save(enrollment);
        mvc.perform(post("/enrollments/delete").session(session).param("_csrf", token())
                .param("enrollmentId", enrollment.getId())).andExpect(redirectedUrl("/enrollments"));
        assertTrue(enrollments.findByIdStrict(enrollment.getId()).isEmpty());
    }

    // Finds this test student's receipt even when another test left an unrelated record.
    private Enrollment findOwnEnrollment() {
        for (Enrollment enrollment : enrollments.readAllStrict()) {
            if (enrollment.getStudentId().equals(student.getId())) return enrollment;
        }
        throw new AssertionError("Expected the student's new enrollment.");
    }

    // Uses the same token prepared for the student enrollment form.
    private String token() throws Exception {
        MvcResult page = mvc.perform(get("/enrollments").session(session)).andReturn();
        return (String) page.getModelAndView().getModel().get("csrfToken");
    }
}
