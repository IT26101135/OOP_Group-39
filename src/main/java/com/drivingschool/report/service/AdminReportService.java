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
    private final com.drivingschool.learningpackage.repository.LearningPackageRepository packages;

    // Receives the existing filing drawers; reports never modify them.
    public AdminReportService(EnrollmentRepository enrollments, BookingRepository bookings,
                              InstructorRepository instructors, VehicleRepository vehicles,
                              ReviewRepository reviews) {
        this(enrollments, bookings, instructors, vehicles, reviews, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public AdminReportService(EnrollmentRepository enrollments, BookingRepository bookings,
            InstructorRepository instructors, VehicleRepository vehicles, ReviewRepository reviews,
            com.drivingschool.learningpackage.repository.LearningPackageRepository packages) {
        this.packages = packages;
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

    // Counts confirmed Paid receipts only, using captured prices rather than today's course prices.
    // CRUD: READ   - finds or lists paid enrollment revenue
    public Map<String, String> revenue() {
        BigDecimal paid = BigDecimal.ZERO;
        for (Enrollment enrollment : enrollments.readAllStrict()) {
            if (enrollment.isPaid()) {
                paid = paid.add(enrollment.getAmount());
            }
        }
        Map<String, String> result = new LinkedHashMap<>();
        result.put("PAID", paid.toPlainString());
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
    // CRUD: READ - capture each store once; reports do not modify records.
    @Override
    public List<BusinessReportSection> business() {
        var receipts = enrollments.readAllStrict();
        var lessons = bookings.readAllStrict();
        var teachers = instructors.readAllStrict();
        var fleet = vehicles.readAllStrict();
        var feedback = reviews.readAllStrict();
        Map<String, com.drivingschool.learningpackage.model.LearningPackage> courses = new LinkedHashMap<>();
        if (packages != null) for (var course : packages.readAllStrict()) courses.put(course.getId(), course);
        var result = new ArrayList<BusinessReportSection>();
        var display = new com.drivingschool.common.controller.SchoolPresentationAdvice.PublicDisplay();
        Map<String, List<Enrollment>> byCourse = new TreeMap<>();
        Map<String, BigDecimal> byMonth = new TreeMap<>();
        for (var receipt : receipts) {
            byCourse.computeIfAbsent(receipt.getPackageId(), key -> new ArrayList<>()).add(receipt);
            if (receipt.isPaid()) byMonth.merge(receipt.getEnrolledAt().toLocalDate().toString().substring(0, 7), receipt.getAmount(), BigDecimal::add);
        }
        List<List<String>> rows = new ArrayList<>();
        long paidCount = 0, waitingCount = 0, cancelledCount = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (var group : byCourse.entrySet()) {
            var course = courses.get(group.getKey());
            long paid = group.getValue().stream().filter(Enrollment::isPaid).count();
            long waiting = group.getValue().stream().filter(e -> "AWAITING_PAYMENT".equals(e.getStatus())).count();
            long cancelled = group.getValue().stream().filter(e -> "CANCELLED".equals(e.getStatus())).count();
            BigDecimal revenue = BigDecimal.ZERO;
            for (var receipt : group.getValue()) if (receipt.isPaid()) revenue = revenue.add(receipt.getAmount());
            paidCount += paid; waitingCount += waiting; cancelledCount += cancelled; totalRevenue = totalRevenue.add(revenue);
            rows.add(List.of(course == null ? "Package no longer listed" : course.getName(), course == null ? "-" : display.label(course.getType()),
                    Integer.toString(group.getValue().size()), Long.toString(paid), Long.toString(waiting), Long.toString(cancelled), money(revenue)));
        }
        if (rows.isEmpty()) rows.add(List.of("No records for this period"));
        rows.add(List.of("Total", "", Integer.toString(receipts.size()), Long.toString(paidCount), Long.toString(waitingCount), Long.toString(cancelledCount), money(totalRevenue)));
        result.add(new BusinessReportSection("enrollments", "Enrollments by package",
                List.of("Package", "Type", "Enrollments", "Paid", "Awaiting payment", "Cancelled", "Revenue LKR"), rows));
        rows = new ArrayList<>();
        for (var month : byMonth.entrySet()) rows.add(List.of(java.time.YearMonth.parse(month.getKey()).format(
                java.time.format.DateTimeFormatter.ofPattern("MMM yyyy", Locale.UK)), money(month.getValue())));
        if (rows.isEmpty()) rows.add(List.of("No records for this period"));
        rows.add(List.of("Total", money(totalRevenue)));
        result.add(new BusinessReportSection("revenue", "Revenue by month", List.of("Enrollment month", "Revenue LKR"), rows));
        rows = new ArrayList<>();
        if (!lessons.isEmpty()) for (var status : BookingStatus.values()) rows.add(List.of(display.label(status),
                Long.toString(lessons.stream().filter(b -> b.getStatus() == status).count())));
        result.add(new BusinessReportSection("statuses", "Bookings by status", List.of("Status", "Bookings"), rows));
        Map<String, String> teacherNames = new LinkedHashMap<>();
        for (var teacher : teachers) teacherNames.put(teacher.getId(), teacher.getName());
        for (var lesson : lessons) if (lesson.getInstructorId() != null) teacherNames.putIfAbsent(lesson.getInstructorId(), "Instructor no longer listed");
        rows = new ArrayList<>();
        for (var teacher : teacherNames.entrySet()) {
            var assigned = lessons.stream().filter(b -> teacher.getKey().equals(b.getInstructorId())).toList();
            rows.add(List.of(teacher.getValue(), Integer.toString(assigned.size()), count(assigned, BookingStatus.COMPLETED),
                    count(assigned, BookingStatus.CANCELLED), count(assigned, BookingStatus.NO_SHOW)));
        }
        result.add(new BusinessReportSection("instructors", "Bookings by instructor", List.of("Instructor", "Total", "Completed", "Cancelled", "No-show"), rows));
        rows = new ArrayList<>();
        for (var teacher : teachers) {
            var approved = feedback.stream().filter(r -> teacher.getId().equals(r.getInstructorId()) && r.getStatus() == ReviewStatus.APPROVED).toList();
            double average = approved.stream().mapToInt(Review::getRating).average().orElse(0);
            rows.add(List.of(teacher.getName(), Integer.toString(teacher.getYearsOfExperience()), Integer.toString(approved.size()), String.format(Locale.ROOT, "%.1f", average)));
        }
        result.add(new BusinessReportSection("ratings", "Instructor ratings", List.of("Instructor", "Experience in years", "Approved reviews", "Average rating"), rows));
        rows = new ArrayList<>();
        for (var vehicle : fleet) rows.add(List.of(vehicle.getRegistrationNumber(), vehicle.getMake() + " " + vehicle.getModel(),
                vehicle.getVehicleCategory() == com.drivingschool.vehicle.model.VehicleCategory.MOTORCYCLE ? "Motorcycle" : display.label(vehicle.getTransmissionType()),
                Long.toString(lessons.stream().filter(b -> vehicle.getId().equals(b.getVehicleId())).count()), display.label(vehicle.getOperationalStatus()),
                vehicle.getInsuranceExpiry() == null ? "-" : display.date(vehicle.getInsuranceExpiry()),
                vehicle.getNextServiceDate() == null ? "-" : display.date(vehicle.getNextServiceDate())));
        result.add(new BusinessReportSection("vehicles", "Vehicle usage", List.of("Registration", "Make and model", "Type", "Lessons", "Status", "Insurance expiry", "Next service"), rows));
        return List.copyOf(result);
    }

    private String count(List<Booking> lessons, BookingStatus status) {
        return Long.toString(lessons.stream().filter(b -> b.getStatus() == status).count());
    }

    private String money(BigDecimal amount) {
        return amount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

}
