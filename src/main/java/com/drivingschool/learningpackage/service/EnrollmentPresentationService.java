/**
 * MEMBER   : IT26101136 - [Name]
 * MODULE   : Learning packages and enrollment
 * FILE     : EnrollmentPresentationService.java
 * WHAT IT DOES: Builds read-only enrollment and booking summaries once per page request.
 * OOP USED : Encapsulation; presentation data stays separate from stored records.
 * CRUD     : READ only; no records are changed.
 */
package com.drivingschool.learningpackage.service;

import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.booking.model.Booking;
import com.drivingschool.booking.model.BookingStatus;
import com.drivingschool.booking.service.StudentBookingOperations;
import com.drivingschool.learningpackage.dto.EnrollmentUsageView;
import com.drivingschool.learningpackage.model.Enrollment;
import com.drivingschool.learningpackage.model.LearningPackage;
import com.drivingschool.learningpackage.repository.EnrollmentRepository;
import com.drivingschool.student.model.Student;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
// SOLID: S - Presentation labels do not change enrollment or booking rules.
public class EnrollmentPresentationService {
    private final EnrollmentRepository enrollments;
    private final LearningPackageCatalog packages;
    private final StudentBookingOperations bookings;
    private final AuthSessionService auth;
    private final Clock clock;

    public EnrollmentPresentationService(EnrollmentRepository enrollments, LearningPackageCatalog packages,
            StudentBookingOperations bookings, AuthSessionService auth, Clock clock) {
        this.enrollments = enrollments;
        this.packages = packages;
        this.bookings = bookings;
        this.auth = auth;
        this.clock = clock;
    }

    // CRUD: READ - prepare only this student's receipts and lessons after checking their role.
    public void addStudent(HttpServletRequest request, Model model, List<Booking> ownBookings) {
        if (!(auth.requireLogin(request) instanceof Student student)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student access is required.");
        }
        // Step 1: Read each catalog once, then resolve references from maps in memory.
        Map<String, Enrollment> receipts = enrollmentMap();
        Map<String, LearningPackage> courses = packageMap();
        if (ownBookings == null) ownBookings = bookings.getOwnBookings(request);
        List<Enrollment> ownReceipts = new ArrayList<>();
        Map<String, EnrollmentUsageView> usage = new LinkedHashMap<>();
        Enrollment selected = null;
        Enrollment latestPaid = null;
        for (Enrollment receipt : receipts.values()) {
            if (student.getId().equals(receipt.getStudentId()) && receipt.isPaid()
                    && newerThan(receipt, latestPaid)) latestPaid = receipt;
        }
        for (Enrollment receipt : receipts.values()) {
            if (!student.getId().equals(receipt.getStudentId())) continue;
            ownReceipts.add(receipt);
            boolean balanceOwner = receipt.getId().equals(student.getActiveEnrollmentId())
                    && receipt.getPackageId().equals(student.getActivePackageId());
            // Exhausting the account clears its active IDs. Only the latest Paid receipt can represent that zero.
            if (student.getActiveEnrollmentId() == null && student.getActivePackageId() == null
                    && student.getLessonsRemaining() == 0 && receipt == latestPaid) balanceOwner = true;
            usage.put(receipt.getId(), describe(receipt, courses.get(receipt.getPackageId()), student, ownBookings, balanceOwner));
            if (student.getActiveEnrollmentId() == null) {
                if (selected == null
                        || ("AWAITING_PAYMENT".equals(receipt.getStatus()) && !"AWAITING_PAYMENT".equals(selected.getStatus()))
                        || (("AWAITING_PAYMENT".equals(receipt.getStatus()) == "AWAITING_PAYMENT".equals(selected.getStatus()))
                        && newerThan(receipt, selected))) selected = receipt;
            }
        }
        if (student.getActiveEnrollmentId() != null) selected = receipts.get(student.getActiveEnrollmentId());
        if (selected != null && !student.getId().equals(selected.getStudentId())) selected = null;
        // Step 2: Keep old receipt balances separate from the current account's balance.
        List<LearningPackage> ownPackages = new ArrayList<>();
        LearningPackage activeCourse = courses.get(student.getActivePackageId());
        if (activeCourse != null && activeCourse.isActive()) ownPackages.add(activeCourse);
        model.addAttribute("packages", ownPackages);
        model.addAttribute("enrollments", ownReceipts);
        model.addAttribute("enrollmentUsage", usage);
        model.addAttribute("currentUsage", selected == null ? unavailable() : usage.get(selected.getId()));
        model.addAttribute("bookingPackages", bookingViews(ownBookings, receipts, courses));
    }

    // Callers supply only bookings already authorized by their role's existing service.
    public Map<String, EnrollmentUsageView> forBookings(List<Booking> authorizedBookings) {
        return bookingViews(authorizedBookings, enrollmentMap(), packageMap());
    }

    private Map<String, Enrollment> enrollmentMap() {
        Map<String, Enrollment> result = new LinkedHashMap<>();
        for (Enrollment receipt : enrollments.readAllStrict()) result.put(receipt.getId(), receipt);
        return result;
    }

    private Map<String, LearningPackage> packageMap() {
        Map<String, LearningPackage> result = new LinkedHashMap<>();
        for (LearningPackage course : packages.listPackages()) result.put(course.getId(), course);
        return result;
    }

    private Map<String, EnrollmentUsageView> bookingViews(List<Booking> authorizedBookings,
            Map<String, Enrollment> receipts, Map<String, LearningPackage> courses) {
        Map<String, EnrollmentUsageView> result = new LinkedHashMap<>();
        // Step 3: Missing references become labels, never a per-row file lookup or error page.
        for (Booking booking : authorizedBookings) {
            Enrollment receipt = receipts.get(booking.getEnrollmentId());
            if (receipt == null || !booking.getStudentId().equals(receipt.getStudentId())) {
                result.put(booking.getId(), unavailable());
            } else {
                result.put(booking.getId(), describe(receipt, courses.get(receipt.getPackageId()), null, List.of(), false));
            }
        }
        return result;
    }

    private EnrollmentUsageView describe(Enrollment receipt, LearningPackage course,
            Student student, List<Booking> lessons, boolean balanceOwner) {
        int completed = 0;
        int requested = 0;
        int confirmed = 0;
        for (Booking booking : lessons) {
            if (!receipt.getId().equals(booking.getEnrollmentId())
                    || !receipt.getStudentId().equals(booking.getStudentId())) continue;
            if (booking.getStatus() == BookingStatus.COMPLETED) completed++;
            if (booking.getStatus() == BookingStatus.PENDING) requested++;
            if (booking.getStatus() == BookingStatus.CONFIRMED) confirmed++;
        }
        boolean current = student != null && balanceOwner;
        boolean expired = receipt.getValidUntil() != null
                && receipt.getValidUntil().isBefore(LocalDate.now(clock));
        String status;
        if ("AWAITING_PAYMENT".equals(receipt.getStatus())) status = "Awaiting payment";
        else if ("CANCELLED".equals(receipt.getStatus())) status = "Cancelled";
        else if (course == null || !course.isActive()) status = "Unavailable";
        else if (expired) status = "Expired";
        else if (!current) status = "Historical";
        else if (student.getLessonsRemaining() == 0) status = "Used up";
        else status = "Active";
        String left = current && receipt.isPaid() ? String.valueOf(student.getLessonsRemaining()) : "Not recorded";
        String message = "";
        if ("Historical".equals(status) || (receipt.isPaid() && !current)) {
            message = "Historical enrollment — remaining balance not recorded";
        }
        if ("Awaiting payment".equals(status)) message = "Lessons available after payment";
        if ("Used up".equals(status) || "Expired".equals(status)) {
            message += (message.isEmpty() ? "" : ". ") + "Contact the school to renew. Buying another package is not available yet.";
        }
        boolean bookable = "Active".equals(status);
        boolean full = bookable && requested + confirmed >= student.getLessonsRemaining();
        if (full) message = "Your remaining lessons are already requested. View your bookings to change a request.";
        String url = "/packages";
        String label = "View packages";
        if ("Awaiting payment".equals(status)) {
            url = "/enrollments/" + receipt.getId() + "/payment";
            label = "Payment instructions";
        } else if (bookable) {
            url = "/bookings";
            label = full ? "View my bookings" : "Book lesson";
        } else if ("Unavailable".equals(status)) {
            url = "/enrollments";
            label = "My enrollments";
            if (message.isEmpty()) message = "Package details are unavailable. Contact the school for help.";
        }
        return new EnrollmentUsageView(course == null ? "Package unavailable (" + receipt.getPackageId() + ")" : course.getName(),
                course == null ? "Not recorded" : course.getType().toString(),
                receipt.getValidUntil() == null ? "Validity not recorded" : receipt.getValidUntil().toString(),
                status, left, course == null ? "Not recorded" : String.valueOf(course.getLessonCount()),
                completed, requested, confirmed, bookable, url, label, message);
    }

    private boolean newerThan(Enrollment receipt, Enrollment previous) {
        if (previous == null) return true;
        int timeOrder = receipt.getEnrolledAt().compareTo(previous.getEnrolledAt());
        return timeOrder > 0 || (timeOrder == 0 && receipt.getId().compareTo(previous.getId()) > 0);
    }

    private EnrollmentUsageView unavailable() {
        return new EnrollmentUsageView("Package unavailable", "Not recorded", "Validity not recorded",
                "Unavailable", "Not recorded", "Not recorded", 0, 0, 0, false,
                "/enrollments", "My enrollments", "No current package details are available. View packages or contact the school.");
    }
}
