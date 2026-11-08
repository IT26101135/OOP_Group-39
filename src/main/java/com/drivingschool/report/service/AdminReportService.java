// Module owner: IT26101137
package com.drivingschool.report.service;

import com.drivingschool.booking.model.BookingStatus;
import com.drivingschool.booking.repository.BookingRepository;
import com.drivingschool.instructor.repository.InstructorRepository;
import com.drivingschool.learningpackage.model.Enrollment;
import com.drivingschool.learningpackage.repository.EnrollmentRepository;
import com.drivingschool.review.model.ReviewStatus;
import com.drivingschool.review.repository.ReviewRepository;
import com.drivingschool.vehicle.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/** Builds simple report totals directly from the stored source records. */
@Service
public class AdminReportService {
    private final EnrollmentRepository enrollments;
    private final BookingRepository bookings;
    private final InstructorRepository instructors;
    private final VehicleRepository vehicles;
    private final ReviewRepository reviews;

    public AdminReportService(EnrollmentRepository enrollments, BookingRepository bookings,
                              InstructorRepository instructors, VehicleRepository vehicles,
                              ReviewRepository reviews) {
        this.enrollments = enrollments; this.bookings = bookings; this.instructors = instructors;
        this.vehicles = vehicles; this.reviews = reviews;
    }

    public Map<String, Long> enrollmentsByPackage() {
        Map<String, Long> result = new LinkedHashMap<>();
        enrollments.readAllStrict().forEach(e -> result.merge(e.getPackageId(), 1L, Long::sum));
        return result;
    }
    public Map<String, Long> enrollmentsByMonth() {
        Map<String, Long> result = new LinkedHashMap<>();
        enrollments.readAllStrict().stream().sorted(java.util.Comparator.comparing(Enrollment::getEnrolledAt))
                .forEach(e -> result.merge(e.getEnrolledAt().toLocalDate().toString().substring(0, 7), 1L, Long::sum));
        return result;
    }
    public Map<String, Long> bookingsByStatus() {
        Map<String, Long> result = new LinkedHashMap<>();
        for (BookingStatus status : BookingStatus.values()) result.put(status.name(), 0L);
        bookings.readAllStrict().forEach(b -> result.merge(b.getStatus().name(), 1L, Long::sum));
        return result;
    }
    public Map<String, Long> bookingsByInstructor() {
        Map<String, Long> result = new LinkedHashMap<>();
        bookings.readAllStrict().stream().filter(b -> b.getInstructorId() != null)
                .forEach(b -> result.merge(b.getInstructorId(), 1L, Long::sum));
        return result;
    }
    public Map<String, String> instructorRatings() {
        Map<String, String> result = new LinkedHashMap<>();
        instructors.readAllStrict().forEach(i -> {
            var approved = reviews.readAllStrict().stream().filter(r -> i.getId().equals(r.getInstructorId())
                    && r.getStatus() == ReviewStatus.APPROVED).toList();
            double average = approved.stream().mapToInt(com.drivingschool.review.model.Review::getRating)
                    .average().orElse(0.0);
            result.put(i.getId() + " — " + i.getName(), String.format("%.2f / 5 (%d approved)", average, approved.size()));
        });
        return result;
    }
    public Map<String, Long> vehicleUsage() {
        Map<String, Long> result = new LinkedHashMap<>();
        bookings.readAllStrict().stream().filter(b -> b.getVehicleId() != null)
                .forEach(b -> result.merge(b.getVehicleId(), 1L, Long::sum));
        vehicles.readAllStrict().forEach(v -> result.putIfAbsent(v.getId(), 0L));
        return result;
    }
    public Map<String, String> revenue() {
        Map<String, BigDecimal> sums = new LinkedHashMap<>();
        sums.put("PAID", BigDecimal.ZERO); sums.put("UNPAID", BigDecimal.ZERO);
        enrollments.readAllStrict().forEach(e -> {
            String key = "PAID".equalsIgnoreCase(e.getPaymentStatus()) ? "PAID" : "UNPAID";
            sums.merge(key, e.getAmount(), BigDecimal::add);
        });
        Map<String, String> result = new LinkedHashMap<>();
        sums.forEach((k, v) -> result.put(k, v.toPlainString()));
        return result;
    }
    public Map<String, Map<String, ?>> all() {
        Map<String, Map<String, ?>> result = new LinkedHashMap<>();
        result.put("Enrollments per package", enrollmentsByPackage());
        result.put("Enrollments per month", enrollmentsByMonth());
        result.put("Bookings by status", bookingsByStatus());
        result.put("Bookings by instructor", bookingsByInstructor());
        result.put("Instructor ratings", instructorRatings());
        result.put("Vehicle usage", vehicleUsage());
        result.put("Revenue", revenue());
        return result;
    }
}
