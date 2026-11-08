/**
============================================================
MEMBER   : IT26101134 - [Name]
MODULE   : Bookings, FIFO queue and scheduling
FILE     : BookingService.java
WHAT IT DOES (simple words): Applies the booking rules.
REAL-LIFE EXAMPLE: A reception waiting line handles the oldest request first.
OOP USED : Encapsulation (private fields and public methods)
CRUD     : See the labelled operations; model and view files support these operations.
============================================================
*/
package com.drivingschool.booking.service;

import com.drivingschool.auth.model.Role;
import com.drivingschool.auth.model.User;
import com.drivingschool.auth.model.InstructorAccount;
import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.auth.util.AccountWriteCoordinator;
import com.drivingschool.booking.model.Booking;
import com.drivingschool.booking.model.BookingStatus;
import com.drivingschool.booking.repository.BookingRepository;
import com.drivingschool.booking.util.LessonIntervalUtils;
import com.drivingschool.booking.util.BookingRequestQueue;
import com.drivingschool.common.exception.RecordNotFoundException;
import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.instructor.model.Instructor;
import com.drivingschool.instructor.repository.InstructorRepository;
import com.drivingschool.student.model.Student;
import com.drivingschool.learningpackage.model.LearningPackage;
import com.drivingschool.learningpackage.repository.LearningPackageRepository;
import com.drivingschool.vehicle.model.Vehicle;
import com.drivingschool.vehicle.model.VehicleOperationalStatus;
import com.drivingschool.vehicle.repository.VehicleRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import com.drivingschool.booking.util.BookingOrder;
import com.drivingschool.booking.util.LessonTimeOrder;
import com.drivingschool.booking.util.PendingRequestOrder;

@Service
// SOLID: S - This class is responsible for application rules.
public class BookingService implements StudentBookingOperations, AdminBookingOperations {

    // AccountWriteCoordinator.execute is synchronized on this same class object.
    // One cashier desk therefore protects payment decisions and all booking writes together.
    private static final Object BOOKING_LOCK = AccountWriteCoordinator.class;
    private static final long LESSON_MINUTES = 60;

    private final BookingRepository bookings;
    private final LearningPackageRepository packages;
    private final InstructorRepository instructors;
    private final VehicleRepository vehicles;
    private final AuthSessionService auth;
    private final UserRepository users;
    private final Clock clock;
    private final PaidEnrollmentGuard paidEnrollments;
    private final BookingOrder byLessonTime = new LessonTimeOrder();
    private final BookingOrder byRequestTime = new PendingRequestOrder();
    @Value("${app.booking.change-cutoff-hours:24}")
    private long changeCutoffHours = 24;
    @Autowired(required = false)
    private SlotAvailabilityService slotAvailability;
    @Autowired(required = false)
    private BookingConflictChecker conflictChecker;

    // Receives the helpers or validates the details needed to create this object.
    public BookingService(BookingRepository bookings,
                          LearningPackageRepository packages,
                          InstructorRepository instructors,
                          VehicleRepository vehicles,
                          AuthSessionService auth,
                          UserRepository users,
                          Clock clock, PaidEnrollmentGuard paidEnrollments) {
        this.bookings = bookings;
        this.packages = packages;
        this.instructors = instructors;
        this.vehicles = vehicles;
        this.auth = auth;
        this.users = users;
        this.clock = clock;
        this.paidEnrollments = paidEnrollments;
    }

    // Checks the student, time and enrollment before saving a pending lesson request.
    // CRUD: CREATE - adds a new booking and saves it
    public Booking requestLesson(HttpServletRequest request, String packageId,
                                 LocalDateTime startTime) {
        // Step 1: Check the signed-in student before reading or changing their requests.
        User student = requireStudent(request);
        paidEnrollments.requirePaid(student.getId(), ((Student) student).getActiveEnrollmentId());
        if (!(student instanceof Student profile) || !packageId.equals(profile.getActivePackageId())
                || profile.getLessonsRemaining() < 1 || profile.getActiveEnrollmentId() == null) {
            throw new ValidationException("Enroll in an active package with lessons remaining before booking.");
        }
        LearningPackage selected = packages.findByIdStrict(packageId)
                .orElseThrow(() -> new RecordNotFoundException("Package not found."));
        if (!selected.isActive()) {
            throw new ValidationException("This package is not available.");
        }
        if (startTime == null || !startTime.isAfter(LocalDateTime.now(clock))) {
            throw new ValidationException("Choose a lesson time in the future.");
        }

        // Step 2: Let one booking writer check and save at a time, like one reception desk.
        synchronized (BOOKING_LOCK) {
            paidEnrollments.requirePaid(student.getId(), profile.getActiveEnrollmentId());
            LocalDateTime endTime = startTime.plusMinutes(LESSON_MINUTES);
            for (Booking existing : bookings.readAllStrict()) {
                if (existing.getStudentId().equals(student.getId())
                        && existing.getStatus() != BookingStatus.CANCELLED
                        && LessonIntervalUtils.overlaps(startTime, endTime,
                        existing.getStartTime(), existing.getEndTime())) {
                    throw new ValidationException("You already have a lesson at that time.");
                }
            }
            LocalDateTime now = LocalDateTime.now(clock);
            Booking booking = new Booking(bookings.nextId("B"), student.getId(),
                    profile.getActiveEnrollmentId(), startTime, endTime, null, null,
                    BookingStatus.PENDING, now);
            // Step 3: Save only after the relevant time and resource checks pass.
            bookings.save(booking);
            return booking;
        }
    }

    // Checks the student, time and enrollment before saving a pending lesson request.
    // CRUD: CREATE - adds a new booking and saves it
    public Booking requestLesson(HttpServletRequest request, String packageId,
                                 String instructorId, LocalDateTime startTime) {
        // Step 1: Check the signed-in student before reading or changing their requests.
        User student = requireStudent(request);
        paidEnrollments.requirePaid(student.getId(), ((Student) student).getActiveEnrollmentId());
        if (slotAvailability == null || startTime == null
                || !slotAvailability.availableSlots(instructorId, startTime.toLocalDate(),
                null, student.getId()).contains(startTime)) {
            throw new ValidationException("That instructor slot is no longer available. Choose another time.");
        }
        LearningPackage selected = packages.findByIdStrict(packageId)
                .orElseThrow(() -> new RecordNotFoundException("Package not found."));
        if (!packageId.equals(((Student) student).getActivePackageId())) {
            throw new ValidationException("Select your currently active package for a lesson.");
        }
        // Step 2: Let one booking writer check and save at a time, like one reception desk.
        synchronized (BOOKING_LOCK) {
            paidEnrollments.requirePaid(student.getId(), ((Student) student).getActiveEnrollmentId());
            Booking booking = new Booking(bookings.nextId("B"), student.getId(),
                    ((Student) student).getActiveEnrollmentId(), startTime,
                    startTime.plusMinutes(LESSON_MINUTES), null, null,
                    BookingStatus.PENDING, LocalDateTime.now(clock));
            booking.setRequestedInstructorId(instructorId);
            String reason = conflictChecker == null ? null
                    : conflictChecker.conflictReason(booking, instructorId, null);
            if (reason != null) throw new ValidationException(reason);
            // Step 3: Save only after the relevant time and resource checks pass.
            bookings.save(booking);
            return booking;
        }
    }

    // Shares the same saved-payment check used by booking writes with the student's page.
    // CRUD: READ   - finds or lists enrollments
    public String bookingProblem(HttpServletRequest request) {
        Student student = (Student) requireStudent(request);
        return paidEnrollments.problemFor(student.getId(), student.getActiveEnrollmentId());
    }

    // Lists only this student's requests in lesson-time order.
    // CRUD: READ   - finds or lists bookings
    public List<Booking> getOwnBookings(HttpServletRequest request) {
        String studentId = requireStudent(request).getId();
        List<Booking> own = new ArrayList<>();
        for (Booking booking : bookings.readAllStrict()) {
            if (booking.getStudentId().equals(studentId)) own.add(booking);
        }
        return byLessonTime.order(own);
    }

    // Cancels an owned request only when the configured notice period permits it.
    // CRUD: UPDATE - changes an existing booking
    public void cancelOwnBooking(HttpServletRequest request, String bookingId) {
        String studentId = requireStudent(request).getId();
        // Step 2: Let one booking writer check and save at a time, like one reception desk.
        synchronized (BOOKING_LOCK) {
            Booking booking = findBooking(bookingId);
            if (!booking.getStudentId().equals(studentId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You can only cancel your own lesson requests.");
            }
            if (booking.getStartTime().minusHours(changeCutoffHours)
                    .isBefore(LocalDateTime.now(clock))) {
                throw new ValidationException("Bookings can only be changed at least "
                        + changeCutoffHours + " hours before the lesson.");
            }
            booking.cancel();
            bookings.update(booking);
        }
    }

    // Validates a replacement request before cancelling the old one; it joins the queue tail.
    // CRUD: UPDATE - changes an existing booking
    public Booking rescheduleOwnBooking(HttpServletRequest request, String bookingId,
                                         LocalDateTime newStartTime) {
        String studentId = requireStudent(request).getId();
        // Step 2: Let one booking writer check and save at a time, like one reception desk.
        synchronized (BOOKING_LOCK) {
            Booking old = findBooking(bookingId);
            if (!old.getStudentId().equals(studentId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            paidEnrollments.requirePaid(studentId, old.getEnrollmentId());
            if (old.getStartTime().minusHours(changeCutoffHours).isBefore(LocalDateTime.now(clock))) {
                throw new ValidationException("Bookings can only be changed at least " + changeCutoffHours + " hours before the lesson.");
            }
            if (old.getStatus() != BookingStatus.PENDING && old.getStatus() != BookingStatus.CONFIRMED) {
                throw new ValidationException("Only pending or confirmed bookings can be rescheduled.");
            }
            if (newStartTime == null || !newStartTime.isAfter(LocalDateTime.now(clock)))
                throw new ValidationException("Choose a new lesson time in the future.");
            User current = users.findByIdStrict(studentId).orElseThrow();
            if (!(current instanceof Student student) || student.getActivePackageId() == null
                    || student.getActiveEnrollmentId() == null || student.getLessonsRemaining() < 1)
                throw new ValidationException("An active package with remaining lessons is required.");
            String requestedInstructor = old.getRequestedInstructorId() != null
                    ? old.getRequestedInstructorId() : old.getInstructorId();
            if (requestedInstructor == null || slotAvailability == null
                    || !slotAvailability.availableSlots(requestedInstructor,
                    newStartTime.toLocalDate(), old.getId(), studentId).contains(newStartTime))
                throw new ValidationException("That instructor slot is unavailable. Choose another time.");
            Booking replacement = new Booking(bookings.nextId("B"), studentId,
                    student.getActiveEnrollmentId(), newStartTime,
                    newStartTime.plusMinutes(LESSON_MINUTES), null, null,
                    BookingStatus.PENDING, LocalDateTime.now(clock));
            replacement.setRequestedInstructorId(requestedInstructor);
            String reason = conflictChecker == null ? null
                    : conflictChecker.conflictReason(replacement, requestedInstructor, null, old.getId());
            if (reason != null) throw new ValidationException(reason);
            // Step 4: Save the new pending request before cancelling the old request.
            bookings.save(replacement);
            old.cancel();
            bookings.update(old);
            return replacement;
        }
    }

    /** Processes only the current FIFO head, either assigning it or rejecting it with a reason. */
    // Takes only the FIFO head for assignment and checks it again before saving.
    // CRUD: UPDATE - changes an existing booking
    public Booking processNext(HttpServletRequest request, String instructorId, String vehicleId) {
        auth.requireAdmin(request);
        synchronized (BOOKING_LOCK) {
            BookingRequestQueue queue = new BookingRequestQueue(bookings.readAllStrict());
            Booking next = queue.dequeue();
            if (next == null) throw new ValidationException("There are no pending requests to process.");
            return assign(request, next.getId(), instructorId, vehicleId);
        }
    }

    // Removes only this student's already-cancelled request.
    // CRUD: DELETE - removes a booking
    public void deleteCancelledBooking(HttpServletRequest request, String bookingId) {
        String studentId = requireStudent(request).getId();
        // Step 2: Let one booking writer check and save at a time, like one reception desk.
        synchronized (BOOKING_LOCK) {
            Booking booking = findBooking(bookingId);
            if (!booking.getStudentId().equals(studentId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You can only delete your own lesson requests.");
            }
            if (booking.getStatus() != BookingStatus.CANCELLED) {
                throw new ValidationException(
                        "Cancel a lesson request before deleting it.");
            }
            bookings.delete(bookingId);
        }
    }

    // Lists the unassigned FIFO queue first, followed by other bookings in time order.
    // CRUD: READ   - finds or lists bookings
    public List<Booking> getAllBookings(HttpServletRequest request) {
        auth.requireAdmin(request);
        List<Booking> all = bookings.readAllStrict();
        List<Booking> assignedPending = new ArrayList<>();
        List<Booking> history = new ArrayList<>();
        for (Booking booking : all) {
            if (booking.getStatus() != BookingStatus.PENDING) history.add(booking);
            else if (booking.getInstructorId() != null) assignedPending.add(booking);
        }
        // OOP: POLYMORPHISM - Submission order protects fairness; lesson order helps read a diary.
        List<Booking> ordered = new ArrayList<>(byRequestTime.order(all));
        ordered.addAll(byLessonTime.order(assignedPending));
        ordered.addAll(byLessonTime.order(history));
        return List.copyOf(ordered);
    }

    // Selects the bookings matching the administrator's schedule filters.
    // CRUD: READ   - finds or lists bookings
    public List<Booking> filterBookings(HttpServletRequest request, String from, String to,
                                        String instructorId, String vehicleId, String status) {
        List<Booking> all = getAllBookings(request);
        LocalDate firstDate = parseFilterDate(from);
        LocalDate lastDate = parseFilterDate(to);
        List<Booking> result = new ArrayList<>();
        for (Booking booking : all) {
            if (matchesFilters(booking, firstDate, lastDate, instructorId, vehicleId, status)) result.add(booking);
        }
        return List.copyOf(result);
    }

    // Turns a filter date into a date value or a friendly validation error.
    // OOP: EXCEPTION HANDLING - A malformed filter becomes a friendly ValidationException page.
    private LocalDate parseFilterDate(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw new ValidationException("Choose a valid schedule filter date.");
        }
    }

    // Checks one booking against the administrator's chosen filters.
    private boolean matchesFilters(Booking booking, LocalDate from, LocalDate to,
                                    String instructorId, String vehicleId, String status) {
        LocalDate date = booking.getStartTime().toLocalDate();
        if (from != null && date.isBefore(from)) return false;
        if (to != null && date.isAfter(to)) return false;
        if (instructorId != null && !instructorId.isBlank()
                && !instructorId.equals(booking.getInstructorId())
                && !instructorId.equals(booking.getRequestedInstructorId())) return false;
        if (vehicleId != null && !vehicleId.isBlank() && !vehicleId.equals(booking.getVehicleId())
                && !vehicleId.equals(booking.getRequestedVehicleId())) return false;
        return status == null || status.isBlank() || booking.getStatus().name().equalsIgnoreCase(status);
    }

    // Checks conflicts before changing an existing confirmed lesson's resources.
    // CRUD: UPDATE - changes an existing booking
    public Booking reassign(HttpServletRequest request, String bookingId,
                            String instructorId, String vehicleId) {
        auth.requireAdmin(request);
        // Step 2: Let one booking writer check and save at a time, like one reception desk.
        synchronized (BOOKING_LOCK) {
            Booking target = findBooking(bookingId);
            paidEnrollments.requirePaid(target.getStudentId(), target.getEnrollmentId());
            String reason = conflictChecker.conflictReason(target, instructorId, vehicleId);
            if (reason != null) throw new ValidationException(reason);
            target.reassign(instructorId, vehicleId);
            bookings.update(target);
            return target;
        }
    }

    // Stores the administrator's reason when a lesson must be cancelled.
    // CRUD: UPDATE - changes an existing booking
    public Booking forceCancel(HttpServletRequest request, String bookingId, String reason) {
        auth.requireAdmin(request);
        // Step 2: Let one booking writer check and save at a time, like one reception desk.
        synchronized (BOOKING_LOCK) {
            Booking target = findBooking(bookingId);
            target.forceCancel(reason);
            bookings.update(target);
            return target;
        }
    }

    // Tells the administrator where an unassigned request sits in the FIFO line.
    public int getQueuePosition(HttpServletRequest request, String bookingId) {
        auth.requireAdmin(request);
        BookingRequestQueue queue = new BookingRequestQueue(bookings.readAllStrict());
        return queue.positionOf(bookingId);
    }

    // Returns the current unassigned FIFO waiting line for the administrator.
    public List<Booking> getPendingQueue(HttpServletRequest request) {
        auth.requireAdmin(request);
        return new BookingRequestQueue(bookings.readAllStrict()).toList();
    }

    // Shows a queue position only after checking the request belongs to this student.
    public int getOwnQueuePosition(HttpServletRequest request, String bookingId) {
        // Step 1: Check the signed-in student before reading or changing their requests.
        User student = requireStudent(request);
        Booking booking = findBooking(bookingId);
        if (!booking.getStudentId().equals(student.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You may only view your own request position.");
        }
        return new BookingRequestQueue(bookings.readAllStrict()).positionOf(bookingId);
    }

    // Allows assignment only for the FIFO head and records any conflict reason.
    // CRUD: UPDATE - changes an existing booking
    public Booking assign(HttpServletRequest request, String bookingId,
                          String instructorId, String vehicleId) {
        auth.requireAdmin(request);
        // Step 2: Let one booking writer check and save at a time, like one reception desk.
        synchronized (BOOKING_LOCK) {
            Booking target = findBooking(bookingId);
            BookingRequestQueue queue = new BookingRequestQueue(bookings.readAllStrict());
            Booking next = queue.peek();
            if (next == null || !next.getId().equals(target.getId())) {
                throw new ValidationException("Pending requests must be processed in FIFO order.");
            }
            // Step 3: Retire an unpaid legacy request before even looking up resources.
            String paymentProblem = paidEnrollments.problemFor(target.getStudentId(), target.getEnrollmentId());
            if (paymentProblem != null) {
                target.reject(paymentProblem);
                bookings.update(target);
                return target;
            }
            Instructor instructor = instructors.findByIdStrict(instructorId)
                    .orElseThrow(() -> new RecordNotFoundException("Instructor not found."));
            Vehicle vehicle = vehicles.findByIdStrict(vehicleId)
                    .orElseThrow(() -> new RecordNotFoundException("Vehicle not found."));
            if (!instructor.isActive()) {
                throw new ValidationException("Choose an active instructor.");
            }
            if (vehicle.getOperationalStatus() != VehicleOperationalStatus.IN_SERVICE) {
                throw new ValidationException("Choose a vehicle that is in service.");
            }
            String conflict = conflictChecker == null ? null
                    : conflictChecker.conflictReason(target, instructorId, vehicleId);
            if (conflict != null) {
                target.reject(conflict);
                bookings.update(target);
                return target;
            }
            target.assignForInstructor(instructorId, vehicleId);
            bookings.update(target);
            return target;
        }
    }

    // Marks a confirmed lesson complete through the booking rules.
    // CRUD: UPDATE - changes an existing booking
    public Booking complete(HttpServletRequest request, String bookingId) {
        auth.requireAdmin(request);
        // Step 2: Let one booking writer check and save at a time, like one reception desk.
        synchronized (BOOKING_LOCK) {
            Booking target = findBooking(bookingId);
            paidEnrollments.requirePaid(target.getStudentId(), target.getEnrollmentId());
            target.complete();
            bookings.update(target);
            decrementPackageLessons(target);
            return target;
        }
    }

    // Rechecks ownership and conflicts before the instructor confirms a lesson.
    // CRUD: UPDATE - changes an existing booking
    public Booking acceptAssignedLesson(HttpServletRequest request, String bookingId) {
        User user = auth.requireLogin(request);
        if (user.getRole() != Role.INSTRUCTOR) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        // Step 2: Let one booking writer check and save at a time, like one reception desk.
        synchronized (BOOKING_LOCK) {
            Booking target = findBooking(bookingId);
            if (!user.getId().equals(target.getInstructorId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            paidEnrollments.requirePaid(target.getStudentId(), target.getEnrollmentId());
            String conflict = conflictChecker == null ? null
                    : conflictChecker.conflictReason(target, target.getInstructorId(), target.getVehicleId());
            if (conflict != null) throw new ValidationException(conflict);
            target.confirm(target.getInstructorId(), target.getVehicleId());
            bookings.update(target);
            return target;
        }
    }

    // Rejects only a lesson assigned to this instructor and saves the reason.
    // CRUD: UPDATE - changes an existing booking
    public Booking declineAssignedLesson(HttpServletRequest request, String bookingId, String reason) {
        User user = auth.requireLogin(request);
        if (user.getRole() != Role.INSTRUCTOR) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        // Step 2: Let one booking writer check and save at a time, like one reception desk.
        synchronized (BOOKING_LOCK) {
            Booking target = findBooking(bookingId);
            if (!user.getId().equals(target.getInstructorId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            target.reject(reason);
            bookings.update(target);
            return target;
        }
    }

    // Lists lessons assigned to an active instructor in lesson-time order.
    // CRUD: READ   - finds or lists bookings
    public List<Booking> getInstructorBookings(HttpServletRequest request) {
        User user = auth.requireLogin(request);
        if (user.getRole() != Role.INSTRUCTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Instructor access is required.");
        }
        Instructor profile = instructors.findByIdStrict(user.getId())
                .orElseThrow(() -> new RecordNotFoundException("Instructor profile not found."));
        if (!profile.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "This instructor account is inactive.");
        }
        List<Booking> assigned = new ArrayList<>();
        for (Booking booking : bookings.readAllStrict()) {
            if (user.getId().equals(booking.getInstructorId())) assigned.add(booking);
        }
        return byLessonTime.order(assigned);
    }

    // Checks the instructor owns this lesson before recording completion.
    // CRUD: UPDATE - changes an existing booking
    public Booking completeAssignedLesson(HttpServletRequest request, String bookingId) {
        User user = auth.requireLogin(request);
        if (!(user instanceof InstructorAccount)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Instructor access is required.");
        }
        Instructor profile = instructors.findByIdStrict(user.getId())
                .orElseThrow(() -> new RecordNotFoundException("Instructor profile not found."));
        if (!profile.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "This instructor account is inactive.");
        }
        // Step 2: Let one booking writer check and save at a time, like one reception desk.
        synchronized (BOOKING_LOCK) {
            Booking target = findBooking(bookingId);
            if (!user.getId().equals(target.getInstructorId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You can only complete lessons assigned to you.");
            }
            paidEnrollments.requirePaid(target.getStudentId(), target.getEnrollmentId());
            target.complete();
            bookings.update(target);
            decrementPackageLessons(target);
            return target;
        }
    }

    // Checks the instructor owns this lesson before recording a missed attendance.
    // CRUD: UPDATE - changes an existing booking
    public Booking markNoShowAssignedLesson(HttpServletRequest request, String bookingId) {
        User user = auth.requireLogin(request);
        if (user.getRole() != Role.INSTRUCTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Instructor access is required.");
        }
        // Step 2: Let one booking writer check and save at a time, like one reception desk.
        synchronized (BOOKING_LOCK) {
            Booking target = findBooking(bookingId);
            if (!user.getId().equals(target.getInstructorId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You may only update lessons assigned to you.");
            }
            paidEnrollments.requirePaid(target.getStudentId(), target.getEnrollmentId());
            target.markNoShow();
            bookings.update(target);
            return target;
        }
    }

    // Looks up one booking and reports a friendly error if it is missing.
    private Booking findBooking(String id) {
        return bookings.findByIdStrict(id)
                .orElseThrow(() -> new RecordNotFoundException("Booking not found."));
    }

    // Refuses student-only work unless the signed-in account has that role.
    private User requireStudent(HttpServletRequest request) {
        User user = auth.requireLogin(request);
        if (user.getRole() != Role.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Student access is required.");
        }
        return user;
    }

    // Uses one lesson only when the booking belongs to the student's active enrollment.
    private void decrementPackageLessons(Booking booking) {
        User stored = users.findByIdStrict(booking.getStudentId())
                .orElseThrow(() -> new RecordNotFoundException("Student account not found."));
        if (stored instanceof Student student
                && booking.getEnrollmentId().equals(student.getActiveEnrollmentId())) {
            if (student.getLessonsRemaining() <= 0) {
                throw new ValidationException("The student's package has no lessons remaining.");
            }
            student.setLessonsRemaining(student.getLessonsRemaining() - 1);
            users.update(student);
        }
    }
}
