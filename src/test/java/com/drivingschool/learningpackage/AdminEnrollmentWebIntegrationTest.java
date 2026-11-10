/**
 * ============================================================
 * MEMBER   : IT26101136 - [Name]
 * MODULE   : Learning packages and enrollment
 * FILE     : AdminEnrollmentWebIntegrationTest.java
 * WHAT IT DOES (simple words): Checks payment forms, permissions and repeat submissions.
 * REAL-LIFE EXAMPLE: Rehearses the cashier hand-off using spare student receipts.
 * OOP USED : Abstraction and exception handling
 * CRUD     : READ and UPDATE enrollment requests
 * ============================================================
 */
package com.drivingschool.learningpackage;

import com.drivingschool.auth.model.Admin;
import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.auth.service.AuthService;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.auth.util.PasswordHasher;
import com.drivingschool.learningpackage.model.Enrollment;
import com.drivingschool.learningpackage.model.LearningPackage;
import com.drivingschool.learningpackage.repository.EnrollmentRepository;
import com.drivingschool.learningpackage.repository.LearningPackageRepository;
import com.drivingschool.learningpackage.service.EnrollmentService;
import com.drivingschool.student.model.Student;
import org.junit.jupiter.api.*;
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
import java.math.BigDecimal;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AdminEnrollmentWebIntegrationTest {
    @TempDir static Path folder;
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired AuthService auth;
    @Autowired AuthSessionService sessions;
    @Autowired EnrollmentService enrollmentService;
    @Autowired EnrollmentRepository receipts;
    @Autowired LearningPackageRepository packages;
    private MockHttpSession adminSession;
    private MockHttpSession studentSession;
    private Enrollment enrollment;
    private static final String PASSWORD = "TestingPass123!";

    // Keeps all account creation and receipt updates in temporary files.
    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("app.data-directory", () -> folder.toString());
        registry.add("app.demo-seed.enabled", () -> false);
        registry.add("app.admin-seed.enabled", () -> false);
    }

    // Creates a separate cashier and unpaid learner for each form check.
    @BeforeEach
    void setUp() {
        String number = String.valueOf(users.readAllStrict().size());
        String adminEmail = "cashier" + number + "@example.test";
        users.save(new Admin(users.nextId("A"), "Cashier " + number, adminEmail, new PasswordHasher().hash(PASSWORD)));
        Student learner = auth.registerStudent("Payment Learner", "learner" + number + "@example.test", "0771234567", PASSWORD);
        adminSession = login(adminEmail);
        studentSession = login(learner.getEmail());
        String packageId = packages.nextId("P");
        packages.save(new LearningPackage(packageId, "Course " + number, "Practical driving lessons", new BigDecimal("1500"), 5, 30, true));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(studentSession);
        enrollment = enrollmentService.enroll(request, packageId);
    }

    // The receipt list shows real saved amounts and both readable name maps.
    @Test
    void listsWaitingPaymentsForAdmin() throws Exception {
        mvc.perform(get("/admin/enrollments").session(adminSession)).andExpect(status().isOk())
                .andExpect(view().name("admin/enrollments"))
                .andExpect(model().attributeExists("payments", "studentNames", "packageNames", "csrfToken"));
    }

    // A second click returns a friendly message and cannot grant another set of lessons.
    @Test
    void confirmsOnceAndExplainsRepeatedPost() throws Exception {
        String token = token(adminSession, "/admin/enrollments");
        mvc.perform(post("/admin/enrollments/mark-paid").session(adminSession).param("_csrf", token)
                .param("enrollmentId", enrollment.getId())).andExpect(redirectedUrl("/admin/enrollments"))
                .andExpect(flash().attributeExists("success"));
        mvc.perform(post("/admin/enrollments/mark-paid").session(adminSession).param("_csrf", token)
                .param("enrollmentId", enrollment.getId())).andExpect(redirectedUrl("/admin/enrollments"))
                .andExpect(flash().attribute("error", org.hamcrest.Matchers.containsString("already handled")));
        assertEquals("PAID", receipts.findByIdStrict(enrollment.getId()).orElseThrow().getStatus());
        assertEquals(5, ((Student) users.findByIdStrict(enrollment.getStudentId()).orElseThrow()).getLessonsRemaining());
    }

    // Cancelling keeps the reference and optional reason instead of deleting history.
    @Test
    void cancelsWithOptionalReason() throws Exception {
        mvc.perform(post("/admin/enrollments/cancel-request").session(adminSession)
                .param("_csrf", token(adminSession, "/admin/enrollments"))
                .param("enrollmentId", enrollment.getId()).param("reason", "Student withdrew request"))
                .andExpect(redirectedUrl("/admin/enrollments"));
        Enrollment saved = receipts.findByIdStrict(enrollment.getId()).orElseThrow();
        assertEquals("CANCELLED", saved.getStatus());
        assertEquals("Student withdrew request", saved.getCancellationReason());
        mvc.perform(get("/enrollments").session(studentSession)).andExpect(status().isOk());
    }

    // Existing role and CSRF rules stop unauthorized changes before the service runs.
    @Test
    void blocksAnonymousStudentsAndMissingTokens() throws Exception {
        mvc.perform(get("/admin/enrollments")).andExpect(redirectedUrl("/login?required"));
        mvc.perform(get("/admin/enrollments").session(studentSession)).andExpect(status().isForbidden());
        mvc.perform(post("/admin/enrollments/mark-paid").session(adminSession)
                .param("enrollmentId", enrollment.getId())).andExpect(status().isForbidden());
        mvc.perform(post("/admin/enrollments/cancel-request").session(studentSession)
                .param("_csrf", token(studentSession, "/enrollments"))
                .param("enrollmentId", enrollment.getId())).andExpect(status().isForbidden());
        assertEquals("AWAITING_PAYMENT", receipts.findByIdStrict(enrollment.getId()).orElseThrow().getStatus());
    }

    // Opens a normal authenticated session after real password verification.
    private MockHttpSession login(String email) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        assertTrue(sessions.login(request, email, PASSWORD));
        return (MockHttpSession) request.getSession(false);
    }

    // Reads the same form token that a browser would submit on its payment page.
    private String token(MockHttpSession session, String route) throws Exception {
        return (String) mvc.perform(get(route).session(session)).andReturn()
                .getModelAndView().getModel().get("csrfToken");
    }
}
