/**
============================================================
MEMBER   : IT26101137 - [Name]
MODULE   : Reviews and Reports
FILE     : AdminReportServiceTest.java
WHAT IT DOES (simple words): Checks feedback permissions and report numbers with temporary data.
REAL-LIFE EXAMPLE: Rehearsing the school manager's checks before a demonstration.
OOP USED : Service interfaces, encapsulation and exception handling
CRUD     : READ report totals; CREATE, READ, UPDATE, DELETE review records
============================================================
*/
package com.drivingschool.review.service;

import com.drivingschool.report.service.*;
import com.drivingschool.report.controller.AdminReportController;
import com.drivingschool.booking.model.*;
import com.drivingschool.booking.repository.BookingRepository;
import com.drivingschool.instructor.model.Instructor;
import com.drivingschool.instructor.repository.InstructorRepository;
import com.drivingschool.learningpackage.model.Enrollment;
import com.drivingschool.learningpackage.repository.EnrollmentRepository;
import com.drivingschool.review.model.*;
import com.drivingschool.review.repository.ReviewRepository;
import com.drivingschool.vehicle.model.*;
import com.drivingschool.vehicle.repository.VehicleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockHttpServletResponse;
import java.nio.file.Path;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminReportServiceTest {
    @TempDir Path folder;
    private EnrollmentRepository enrollments;
    private BookingRepository bookings;
    private InstructorRepository instructors;
    private VehicleRepository vehicles;
    private ReviewRepository reviews;
    private AdminReportService reports;
    private final LocalDateTime time = LocalDateTime.of(2026, 1, 10, 9, 0);

    // Each test opens independent filing drawers; no project data is read or changed.
    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        enrollments = new EnrollmentRepository(folder.toString(), mapper);
        bookings = new BookingRepository(folder.toString(), mapper);
        instructors = new InstructorRepository(folder, mapper);
        vehicles = new VehicleRepository(folder, mapper);
        reviews = new ReviewRepository(folder.toString(), mapper);
        reports = new AdminReportService(enrollments, bookings, instructors, vehicles, reviews);
    }

    // Empty groups still show every lesson status and zero-length bars without division by zero.
    @Test
    void emptyReportsKeepZeroCounts() {
        assertEquals(BookingStatus.values().length, reports.bookingsByStatus().size());
        for (long count : reports.bookingsByStatus().values()) {
            assertEquals(0L, count);
        }
        assertEquals(7, reports.all().size());
        for (ReportSection report : reports.all()) {
            for (ReportRow row : report.getRows()) {
                assertEquals(0, row.getPercentage());
            }
        }
    }

    // Cancelled requests and waiting receipts are not income, including in the shared CSV rows.
    @Test
    void revenueExcludesWaitingAndCancelledRequests() {
        enrollments.save(new Enrollment("E001", "S001", "P001", time, new BigDecimal("100"), "PAID"));
        enrollments.save(new Enrollment("E002", "S002", "P001", time, new BigDecimal("200"), "UNPAID"));
        Enrollment cancelled = new Enrollment("E003", "S003", "P001", time, new BigDecimal("300"), "UNPAID");
        cancelled.setStatus("CANCELLED");
        enrollments.save(cancelled);
        assertEquals("100", reports.revenue().get("PAID"));
        assertEquals(1, reports.revenue().size());
        assertEquals(1, reports.all().get(reports.all().size() - 1).getRows().size());
    }

    // Months appear in calendar order even when enrollment records were saved in reverse order.
    @Test
    void monthsAreChronologicalAndAmountsUseEnrollmentSnapshots() {
        enrollments.save(new Enrollment("E001", "S001", "P001", time.plusMonths(2), new BigDecimal("100.25"), "PAID"));
        enrollments.save(new Enrollment("E002", "S002", "P001", time, new BigDecimal("50.75"), "paid"));
        enrollments.save(new Enrollment("E003", "S003", "P002", time.plusMonths(1), new BigDecimal("35.10"), "UNPAID"));
        assertEquals(List.of("2026-01", "2026-02", "2026-03"), new ArrayList<>(reports.enrollmentsByMonth().keySet()));
        assertEquals("151.00", reports.revenue().get("PAID"));
        assertFalse(reports.revenue().containsKey("UNPAID"));
        assertEquals(2L, reports.enrollmentsByPackage().get("P001"));
    }

    // Vehicle counts include unused cars, and unassigned requests do not count as instructor work.
    @Test
    void usageAndStatusCountsMatchStoredLessons() {
        vehicles.save(new Vehicle("V001", "CAB1234", "Toyota", "Aqua", TransmissionType.MANUAL,
                VehicleCategory.CAR, VehicleOperationalStatus.IN_SERVICE));
        vehicles.save(new Vehicle("V002", "CAB5678", "Honda", "Fit", TransmissionType.AUTOMATIC,
                VehicleCategory.CAR, VehicleOperationalStatus.IN_SERVICE));
        bookings.save(new Booking("B001", "S001", "E001", time, time.plusHours(1), "I001", "V001", BookingStatus.COMPLETED, time.minusDays(1)));
        bookings.save(new Booking("B002", "S002", "E002", time, time.plusHours(1), null, null, BookingStatus.PENDING, time.minusDays(1)));
        assertEquals(1L, reports.vehicleUsage().get("V001"));
        assertEquals(0L, reports.vehicleUsage().get("V002"));
        assertEquals(1L, reports.bookingsByInstructor().get("I001"));
        assertEquals(1L, reports.bookingsByStatus().get("PENDING"));
        assertEquals(1L, reports.bookingsByStatus().get("COMPLETED"));
    }

    // Only approved feedback contributes to the displayed star average and response count.
    @Test
    void ratingsExcludePendingAndHiddenFeedback() {
        instructors.save(new Instructor("I001", "Teacher", "teacher@example.test", "0771234567", 5, true));
        reviews.save(new Review("R001", "S001", "B001", "I001", 5, "Clear driving instructions", ReviewStatus.APPROVED, time, time));
        reviews.save(new Review("R002", "S002", "B002", "I001", 4, "Helpful parking instruction", ReviewStatus.APPROVED, time, time));
        reviews.save(new Review("R003", "S003", "B003", "I001", 1, "Waiting for moderation now", ReviewStatus.PENDING, time, time));
        reviews.save(new Review("R004", "S004", "B004", "I001", 1, "Hidden example feedback", ReviewStatus.HIDDEN, time, time));
        assertEquals("4.50 / 5 (2 approved)", reports.instructorRatings().get("I001 - Teacher"));
    }

    // Real count bars are scaled against the largest group, keeping zero at zero.
    @Test
    void chartWidthsMatchCountsAndAreBounded() {
        enrollments.save(new Enrollment("E001", "S001", "P001", time, new BigDecimal("100"), "PAID"));
        enrollments.save(new Enrollment("E002", "S002", "P001", time, new BigDecimal("100"), "PAID"));
        enrollments.save(new Enrollment("E003", "S003", "P002", time, new BigDecimal("100"), "PAID"));
        ReportSection section = reports.all().get(0);
        assertEquals(100, section.getRows().get(0).getPercentage());
        assertEquals(50, section.getRows().get(1).getPercentage());
        assertEquals(0, new ReportRow("zero", "0", -5).getPercentage());
        assertEquals(100, new ReportRow("limit", "1", 200).getPercentage());
    }

    // Downloaded cells escape commas, quotes and line breaks, and cannot start spreadsheet formulas.
    @Test
    void csvEscapesTextAndKeepsExactNumbers() throws Exception {
        ReportCatalog catalog = mock(ReportCatalog.class);
        ReportRow row = new ReportRow(" =2+2,\"quoted\"\nnext", "125000.00", 0);
        when(catalog.business()).thenReturn(List.of(new BusinessReportSection("revenue", "Revenue by month", List.of("Month", "Revenue LKR"), List.of(List.of(row.getLabel(), row.getValue())))));
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller(catalog).export(new org.springframework.mock.web.MockHttpServletRequest(), response, null);
        assertEquals("text/csv;charset=UTF-8", response.getContentType());
        assertTrue(response.getHeader("Content-Disposition").contains("drivespark-report-2026-11-09.csv"));
        assertTrue(response.getContentAsString().contains("\"' =2+2,\"\"quoted\"\"\nnext\",\"125000.00\""));
        assertTrue(response.getContentAsString().startsWith("\uFEFF\"Driving school report\"\r\n"));
    }
    private AdminReportController controller(ReportCatalog catalog) {
        var auth = mock(com.drivingschool.auth.service.AuthSessionService.class);
        var admin = mock(com.drivingschool.auth.model.User.class);
        when(admin.getName()).thenReturn("Anjali Perera");
        when(auth.requireAdmin(any())).thenReturn(admin);
        var clock = java.time.Clock.fixed(java.time.Instant.parse("2026-11-09T12:10:00Z"), java.time.ZoneId.of("Asia/Colombo"));
        return new AdminReportController(catalog, auth, clock, "DriveSpark Academy");
    }

    @Test
    void businessCsvUsesNamesHeadersTotalsAndBom() throws Exception {
        var mapper = new ObjectMapper().findAndRegisterModules();
        var courses = new com.drivingschool.learningpackage.repository.LearningPackageRepository(folder, mapper);
        courses.save(new com.drivingschool.learningpackage.model.LearningPackage("P001", "Manual Car Course", "Manual driving practice and parking", new BigDecimal("12500"), 5, 90, true));
        instructors.save(new Instructor("I001", "Shahithiyan Sivarajah", "teacher@example.test", "0770000000", 8, true));
        vehicles.save(new Vehicle("V001", "CAB4826", "Suzuki", "Alto", TransmissionType.MANUAL, VehicleCategory.CAR, VehicleOperationalStatus.IN_SERVICE));
        enrollments.save(new Enrollment("E001", "S001", "P001", time, new BigDecimal("12500"), "PAID"));
        enrollments.save(new Enrollment("E002", "S002", "P001", time, new BigDecimal("900"), "UNPAID"));
        reviews.save(new Review("R001", "S001", "B001", "I001", 5, "Clear parking explanation", ReviewStatus.APPROVED, time, time));
        var service = new AdminReportService(enrollments, bookings, instructors, vehicles, reviews, courses);
        var sections = service.business();
        assertEquals(6, sections.size());
        assertEquals("12500.00", sections.get(0).rows().get(0).get(6));
        assertEquals("12500.00", sections.get(1).rows().get(1).get(1));
        assertEquals("5.0", sections.get(4).rows().get(0).get(3));
        var response = new MockHttpServletResponse();
        controller(service).export(new org.springframework.mock.web.MockHttpServletRequest(), response, null);
        String csv = response.getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertArrayEquals(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF}, java.util.Arrays.copyOf(response.getContentAsByteArray(), 3));
        assertTrue(csv.contains("Generated on 9 Nov 2026, 5:40 pm"));
        assertTrue(csv.contains("Generated by Anjali Perera"));
        assertTrue(csv.contains("Reporting period: All records\"\r\n\r\n"));
        for (var section : sections) assertTrue(csv.contains(section.title()));
        assertTrue(csv.contains("Revenue LKR"));
        assertTrue(csv.contains("Manual Car Course"));
        assertTrue(csv.contains("Shahithiyan Sivarajah"));
        assertTrue(csv.contains("CAB4826"));
        assertTrue(csv.contains("\"Total\",\"12500.00\""));
        assertFalse(csv.matches("(?s).*\\b[PISVEBR][0-9]{3}\\b.*"));
        assertFalse(csv.contains("NaN"));
        assertFalse(csv.contains("null"));
    }

    @Test
    void emptySectionsKeepHeadingsAndReadableEmptyRow() throws Exception {
        var response = new MockHttpServletResponse();
        controller(reports).export(new org.springframework.mock.web.MockHttpServletRequest(), response, null);
        String csv = response.getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(csv.contains("\"Instructor ratings\"\r\n\"Instructor\",\"Experience in years\""));
        assertEquals(6, csv.split("No records for this period", -1).length - 1);
        assertTrue(csv.contains("\"Total\",\"0.00\""));
    }

    @Test
    void sectionDownloadAndFormulaPrefixesStaySafe() throws Exception {
        ReportCatalog catalog = mock(ReportCatalog.class);
        var cells = List.of("=2+2", "+2", "-2", "@SUM(A1)", "  =3", "comma, quote\" and\nline");
        when(catalog.business()).thenReturn(List.of(new BusinessReportSection("ratings", "Instructor ratings", List.of("Instructor"), cells.stream().map(List::of).toList())));
        var response = new MockHttpServletResponse();
        controller(catalog).export(new org.springframework.mock.web.MockHttpServletRequest(), response, "ratings");
        String csv = response.getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        for (String value : cells.subList(0, 5)) assertTrue(csv.contains("\"'" + value + "\""));
        assertTrue(csv.contains("comma, quote\"\" and\nline"));
        assertTrue(response.getHeader("Content-Disposition").contains("2026-11-09-ratings.csv"));
        assertThrows(com.drivingschool.common.exception.RecordNotFoundException.class, () ->
                controller(catalog).export(new org.springframework.mock.web.MockHttpServletRequest(), new MockHttpServletResponse(), "missing"));
    }

}
