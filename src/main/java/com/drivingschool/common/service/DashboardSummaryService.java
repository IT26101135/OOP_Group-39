/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : DashboardSummaryService.java
WHAT IT DOES (simple words): Applies the rules for dashboard summary.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.common.service;

import com.drivingschool.auth.model.Role;
import com.drivingschool.auth.model.User;
import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.booking.model.Booking;
import com.drivingschool.booking.model.BookingStatus;
import com.drivingschool.booking.repository.BookingRepository;
import com.drivingschool.common.model.DashboardSummary;
import com.drivingschool.instructor.repository.InstructorRepository;
import com.drivingschool.learningpackage.model.Enrollment;
import com.drivingschool.learningpackage.repository.EnrollmentRepository;
import com.drivingschool.learningpackage.repository.LearningPackageRepository;
import com.drivingschool.vehicle.repository.VehicleRepository;
import org.springframework.stereotype.Service;

// SOLID: S - This class calculates dashboard totals without building web pages.
@Service
public class DashboardSummaryService implements DashboardSummaryProvider {
    private final UserRepository users;
    private final InstructorRepository instructors;
    private final VehicleRepository vehicles;
    private final LearningPackageRepository packages;
    private final BookingRepository bookings;
    private final EnrollmentRepository enrollments;

    // Receives the helpers this class needs so each job stays separate.
    public DashboardSummaryService(UserRepository users, InstructorRepository instructors,
            VehicleRepository vehicles, LearningPackageRepository packages,
            BookingRepository bookings, EnrollmentRepository enrollments) {
        this.users = users;
        this.instructors = instructors;
        this.vehicles = vehicles;
        this.packages = packages;
        this.bookings = bookings;
        this.enrollments = enrollments;
    }

    // Counts original records for the dashboard without changing them.
    public DashboardSummary readSummary() {
        // Step 1: Count stored records, including inactive profiles, without changing them.
        DashboardSummary summary = new DashboardSummary();
        summary.setInstructorCount(instructors.readAllStrict().size());
        summary.setVehicleCount(vehicles.readAllStrict().size());
        summary.setPackageCount(packages.readAllStrict().size());
        for (User user : users.readAllStrict()) {
            if (user.getRole() == Role.STUDENT) {
                summary.setStudentCount(summary.getStudentCount() + 1);
            }
        }
        // Step 2: Add lesson and simulated-payment totals from their original records.
        countBookings(summary);
        countPayments(summary);
        return summary;
    }

    // Adds each pending, confirmed or completed lesson to its dashboard total.
    private void countBookings(DashboardSummary summary) {
        for (Booking booking : bookings.readAllStrict()) {
            if (booking.getStatus() == BookingStatus.PENDING) {
                summary.setPendingBookings(summary.getPendingBookings() + 1);
            } else if (booking.getStatus() == BookingStatus.CONFIRMED) {
                summary.setConfirmedBookings(summary.getConfirmedBookings() + 1);
            } else if (booking.getStatus() == BookingStatus.COMPLETED) {
                summary.setCompletedBookings(summary.getCompletedBookings() + 1);
            }
        }
    }

    // Adds simulated paid amounts using decimal arithmetic suitable for money.
    private void countPayments(DashboardSummary summary) {
        for (Enrollment enrollment : enrollments.readAllStrict()) {
            if ("PAID".equalsIgnoreCase(enrollment.getPaymentStatus())) {
                summary.setPaidRevenue(summary.getPaidRevenue().add(enrollment.getAmount()));
            }
        }
    }
}
