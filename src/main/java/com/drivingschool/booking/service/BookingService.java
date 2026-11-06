package com.drivingschool.booking.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.drivingschool.auth.model.Role;
import com.drivingschool.auth.model.User;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.booking.model.Booking;
import com.drivingschool.booking.model.BookingStatus;
import com.drivingschool.booking.repository.BookingRepository;
import com.drivingschool.common.exception.RecordNotFoundException;
import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.learningpackage.api.EnrollmentLookup;
import com.drivingschool.learningpackage.api.EnrollmentSummary;
import com.drivingschool.student.api.StudentLookup;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final AuthSessionService authSessionService;
    private final StudentLookup studentLookup;
    private final EnrollmentLookup enrollmentLookup;
    private final Clock clock;

    public BookingService(
            BookingRepository bookingRepository,
            AuthSessionService authSessionService,
            StudentLookup studentLookup,
            EnrollmentLookup enrollmentLookup,
            Clock clock) {

        this.bookingRepository = bookingRepository;
        this.authSessionService = authSessionService;
        this.studentLookup = studentLookup;
        this.enrollmentLookup = enrollmentLookup;
        this.clock = clock;
    }

    public Booking createBooking(
            HttpServletRequest request,
            String enrollmentId,
            LocalDateTime startTime) {

        return BookingWriteCoordinator.execute(() -> {

            // 1. Check authentication
            User user = authSessionService.requireLogin(request);

            // 2. Only students can create bookings
            if (user.getRole() != Role.STUDENT) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Only students can create bookings."
                );
            }

            // 3. Get the authenticated student's ID
            String studentId = user.getId();

            // 4. Verify the student exists
            if (!studentLookup.exists(studentId)) {
                throw new RecordNotFoundException(
                        "Student not found.");
            }

            // 5. Validate the requested information
            if (enrollmentId == null || enrollmentId.isBlank()) {
                throw new ValidationException(
                        "Enrollment ID cannot be blank.");
            }

            if (startTime == null) {
                throw new ValidationException(
                        "Lesson start time cannot be null.");
            }

            // 6. Find the student's enrollment
            EnrollmentSummary enrollment =
                    enrollmentLookup.findById(enrollmentId)
                            .orElseThrow(() ->
                                    new RecordNotFoundException(
                                            "Enrollment not found."));

            // 7. Verify enrollment ownership
            if (!studentId.equals(enrollment.studentId())) {
                throw new ValidationException(
                        "This enrollment does not belong to you.");
            }

            // 8. Get current Colombo time
            LocalDateTime now = LocalDateTime.now(
                    clock.withZone(ZoneId.of("Asia/Colombo")));

            // 9. Calculate lesson end time (60 minutes)
            LocalDateTime endTime = startTime.plusMinutes(60);

            // 10. Check that the lesson is in the future
            if (!startTime.isAfter(now)) {
                throw new ValidationException(
                        "Lesson must be scheduled in the future.");
            }

            // 11. Check enrollment validity
            if (enrollment.validFrom() == null
                    || enrollment.validUntil() == null
                    || startTime.isBefore(enrollment.validFrom())
                    || endTime.isAfter(enrollment.validUntil())) {

                throw new ValidationException(
                        "Lesson is outside the enrollment validity period.");
            }

            // 12. Read existing bookings strictly
            List<Booking> bookings =
                    bookingRepository.readAllStrict();

            // 13. Count used lessons for this enrollment
            long usedLessons = bookings.stream()
                    .filter(booking ->
                            booking.getEnrollmentId().equals(enrollmentId))
                    .filter(booking ->
                            booking.getStatus() == BookingStatus.PENDING
                                    || booking.getStatus() == BookingStatus.CONFIRMED
                                    || booking.getStatus() == BookingStatus.COMPLETED)
                    .count();

            // 14. Check purchased lesson allowance
            if (usedLessons >= enrollment.purchasedLessonAllowance()) {
                throw new ValidationException(
                        "No remaining lessons in this enrollment.");
            }

            // 15. Check student scheduling conflicts
            if (BookingConflictChecker.hasStudentConflict(
                    bookings,
                    studentId,
                    startTime,
                    endTime,
                    null)) {

                throw new ValidationException(
                        "You already have a booking during this time.");
            }

            // 16. Generate a booking ID
            String bookingId = bookingRepository.nextId("B");

            // 17. Create a pending booking
            Booking booking = new Booking(
                    bookingId,
                    studentId,
                    enrollmentId,
                    startTime,
                    endTime,
                    null,
                    null,
                    BookingStatus.PENDING,
                    now
            );

            // 18. Save the booking
            bookingRepository.save(booking);

            return booking;
        });
    }

}
