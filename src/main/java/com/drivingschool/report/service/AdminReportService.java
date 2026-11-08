/**
============================================================
MEMBER   : IT26101137 - [Name]
MODULE   : Reviews and Reports
FILE     : AdminReportService.java
WHAT IT DOES (simple words): Counts saved records for the school reports.
REAL-LIFE EXAMPLE: Like a feedback book and a school manager counting its records.
OOP USED : See labelled classes and methods below; presentation files display their results
CRUD     : CREATE, READ, UPDATE, DELETE for reviews; reports READ saved data
============================================================
*/
package com.drivingschool.report.service;

import com.drivingschool.booking.model.Booking;
import com.drivingschool.booking.model.BookingStatus;
import com.drivingschool.booking.repository.BookingRepository;
import com.drivingschool.instructor.model.Instructor;
import com.drivingschool.instructor.repository.InstructorRepository;
import com.drivingschool.learningpackage.model.Enrollment;
import com.drivingschool.learningpackage.repository.EnrollmentRepository;
import com.drivingschool.review.model.Review;
import com.drivingschool.review.model.ReviewStatus;
import com.drivingschool.review.repository.ReviewRepository;
import com.drivingschool.vehicle.model.Vehicle;
import com.drivingschool.vehicle.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.Map;
import java.util.List;
import java.util.Locale;

// SOLID: S - Count stored records here; JSP handles presentation and the controller handles HTTP.
@Service
public class AdminReportService implements ReportCatalog {
    private final EnrollmentRepository enrollments;
    private final BookingRepository bookings;
    private final InstructorRepository instructors;
    private final VehicleRepository vehicles;
    private final ReviewRepository reviews;

    // Receives the existing filing drawers; reports never modify them.
    public AdminReportService(EnrollmentRepository enrollments, BookingRepository bookings,
                              InstructorRepository instructors, VehicleRepository vehicles,
                              ReviewRepository reviews) {
        this.enrollments = enrollments;
        this.bookings = bookings;
        this.instructors = instructors;
        this.vehicles = vehicles;
        this.reviews = reviews;
    }

    // Counts enrollment records for each saved package ID, including historical packages.
    public Map<String, Long> enrollmentsByPackage() {
        Map<String, Long> result = new LinkedHashMap<>();
        for (Enrollment enrollment : enrollments.readAllStrict()) {
            addCount(result, enrollment.getPackageId());
        }
        return result;
    }

    // Calendar-shaped keys sort naturally in a TreeMap, so earlier months come first.
    public Map<String, Long> enrollmentsByMonth() {
        Map<String, Long> result = new TreeMap<>();
        for (Enrollment enrollment : enrollments.readAllStrict()) {
            String month = enrollment.getEnrolledAt().toLocalDate().toString().substring(0, 7);
            addCount(result, month);
        }
        return result;
    }

    // Includes every possible lesson status, even when its count is zero.
    public Map<String, Long> bookingsByStatus() {
        Map<String, Long> result = new LinkedHashMap<>();
        for (BookingStatus status : BookingStatus.values()) {
            result.put(status.name(), 0L);
        }
        for (Booking booking : bookings.readAllStrict()) {
            addCount(result, booking.getStatus().name());
        }
        return result;
    }

    // Counts assigned lessons only; unassigned requests do not belong to an instructor yet.
    public Map<String, Long> bookingsByInstructor() {
        Map<String, Long> result = new LinkedHashMap<>();
        for (Booking booking : bookings.readAllStrict()) {
            if (booking.getInstructorId() != null) {
                addCount(result, booking.getInstructorId());
            }
        }
        return result;
    }

    // Reads feedback once and calculates public ratings using approved reviews only.
    public Map<String, String> instructorRatings() {
        Map<String, String> result = new LinkedHashMap<>();
        List<Review> savedReviews = reviews.readAllStrict();
        for (Instructor instructor : instructors.readAllStrict()) {
            result.put(instructor.getId() + " - " + instructor.getName(), ratingSummary(instructor, savedReviews));
        }
        return result;
    }

    // Keeps the star average and the number of approved responses together for the report.
    private String ratingSummary(Instructor instructor, List<Review> savedReviews) {
        int count = 0;
        int total = 0;
        for (Review review : savedReviews) {
            if (instructor.getId().equals(review.getInstructorId()) && review.getStatus() == ReviewStatus.APPROVED) {
                total += review.getRating();
                count++;
            }
        }
        double average = 0.0;
        if (count > 0) {
            average = (double) total / count;
        }
        return String.format(Locale.ROOT, "%.2f / 5 (%d approved)", average, count);
    }

    // Shows all vehicles, including vehicles with no assigned lesson history.
    public Map<String, Long> vehicleUsage() {
        Map<String, Long> result = new LinkedHashMap<>();
        for (Vehicle vehicle : vehicles.readAllStrict()) {
            result.put(vehicle.getId(), 0L);
        }
        for (Booking booking : bookings.readAllStrict()) {
            if (booking.getVehicleId() != null) {
                addCount(result, booking.getVehicleId());
            }
        }
        return result;
    }

    // Uses enrollment amounts captured at purchase time rather than today's package prices.
    public Map<String, String> revenue() {
        BigDecimal paid = BigDecimal.ZERO;
        BigDecimal unpaid = BigDecimal.ZERO;
        for (Enrollment enrollment : enrollments.readAllStrict()) {
            if ("PAID".equalsIgnoreCase(enrollment.getPaymentStatus())) {
                paid = paid.add(enrollment.getAmount());
            } else {
                unpaid = unpaid.add(enrollment.getAmount());
            }
        }
        Map<String, String> result = new LinkedHashMap<>();
        result.put("PAID", paid.toPlainString());
        result.put("UNPAID", unpaid.toPlainString());
        return result;
    }

    // CRUD: READ   - finds or lists report totals
    // Step 1: Count each group. Step 2: Build rows shared by the screen and CSV.
    @Override
    public List<ReportSection> all() {
        List<ReportSection> result = new ArrayList<>();
        result.add(countSection("Enrollments per package", enrollmentsByPackage()));
        result.add(countSection("Enrollments per month", enrollmentsByMonth()));
        result.add(countSection("Bookings by status", bookingsByStatus()));
        result.add(countSection("Bookings by instructor", bookingsByInstructor()));
        result.add(textSection("Instructor ratings", instructorRatings()));
        result.add(countSection("Vehicle usage", vehicleUsage()));
        result.add(textSection("Revenue", revenue()));
        return result;
    }

    // Adds one record to its group without stream collectors or method references.
    private void addCount(Map<String, Long> counts, String key) {
        long count = counts.getOrDefault(key, 0L);
        counts.put(key, count + 1);
    }

    // Compares bar lengths within this section, handling a completely empty chart safely.
    private ReportSection countSection(String title, Map<String, Long> counts) {
        long maximum = 0;
        for (long count : counts.values()) {
            maximum = Math.max(maximum, count);
        }
        List<ReportRow> rows = new ArrayList<>();
        for (Map.Entry<String, Long> entry : counts.entrySet()) {
            int width = 0;
            if (maximum > 0) {
                width = (int) Math.round(100.0 * entry.getValue() / maximum);
            }
            rows.add(new ReportRow(entry.getKey(), entry.getValue().toString(), width));
        }
        return new ReportSection(title, rows, true);
    }

    // Keeps exact rating and money strings in tables rather than drawing misleading numeric bars.
    private ReportSection textSection(String title, Map<String, String> values) {
        List<ReportRow> rows = new ArrayList<>();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            rows.add(new ReportRow(entry.getKey(), entry.getValue(), 0));
        }
        return new ReportSection(title, rows, false);
    }
}
