// Module owner: IT26101134
package com.drivingschool.booking.service;

import com.drivingschool.auth.model.Role;
import com.drivingschool.auth.model.User;
import com.drivingschool.auth.model.InstructorAccount;
import com.drivingschool.auth.repository.UserRepository;
import com.drivingschool.auth.service.AuthSessionService;
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

@Service
public class BookingService {

    private static final Object BOOKING_LOCK = new Object();
    private static final long LESSON_MINUTES = 60;

    private final BookingRepository bookings;
    private final LearningPackageRepository packages;
    private final InstructorRepository instructors;
    private final VehicleRepository vehicles;
    private final AuthSessionService auth;
    private final UserRepository users;
    private final Clock clock;
    @Value("${app.booking.change-cutoff-hours:24}")
    private long changeCutoffHours = 24;
    @Autowired(required = false)
    private SlotAvailabilityService slotAvailability;
    @Autowired(required = false)
    private BookingConflictChecker conflictChecker;

    public BookingService(BookingRepository bookings,
                          LearningPackageRepository packages,
                          InstructorRepository instructors,
                          VehicleRepository vehicles,
                          AuthSessionService auth,
                          UserRepository users,
                          Clock clock) {
        this.bookings = bookings;
        this.packages = packages;
        this.instructors = instructors;
        this.vehicles = vehicles;
        this.auth = auth;
        this.users = users;
        this.clock = clock;
    }

    public Booking requestLesson(HttpServletRequest request, String packageId,
                                 LocalDateTime startTime) {
        User student = requireStudent(request);
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

        synchronized (BOOKING_LOCK) {
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
            bookings.save(booking);
            return booking;
        }
    }

    public Booking requestLesson(HttpServletRequest request, String packageId,
                                 String instructorId, LocalDateTime startTime) {
        User student = requireStudent(request);
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
        synchronized (BOOKING_LOCK) {
            Booking booking = new Booking(bookings.nextId("B"), student.getId(),
                    ((Student) student).getActiveEnrollmentId(), startTime,
                    startTime.plusMinutes(LESSON_MINUTES), null, null,
                    BookingStatus.PENDING, LocalDateTime.now(clock));
            booking.setRequestedInstructorId(instructorId);
            String reason = conflictChecker == null ? null
                    : conflictChecker.conflictReason(booking, instructorId, null);
            if (reason != null) throw new ValidationException(reason);
            bookings.save(booking);
            return booking;
        }
    }

    public List<Booking> getOwnBookings(HttpServletRequest request) {
        String studentId = requireStudent(request).getId();
        return bookings.readAllStrict().stream()
                .filter(booking -> booking.getStudentId().equals(studentId))
                .sorted((left, right) -> left.getStartTime().compareTo(right.getStartTime()))
                .toList();
    }

    public void cancelOwnBooking(HttpServletRequest request, String bookingId) {
        String studentId = requireStudent(request).getId();
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

    public Booking rescheduleOwnBooking(HttpServletRequest request, String bookingId,
                                         LocalDateTime newStartTime) {
        String studentId = requireStudent(request).getId();
        synchronized (BOOKING_LOCK) {
            Booking old = findBooking(bookingId);
            if (!old.getStudentId().equals(studentId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
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
            bookings.save(replacement);
            old.cancel();
            bookings.update(old);
            return replacement;
        }
    }

    /** Processes only the current FIFO head, either assigning it or rejecting it with a reason. */
    public Booking processNext(HttpServletRequest request, String instructorId, String vehicleId) {
        auth.requireAdmin(request);
        BookingRequestQueue queue = new BookingRequestQueue(bookings.readAllStrict());
        Booking next = queue.dequeue();
        if (next == null) throw new ValidationException("There are no pending requests to process.");
        return assign(request, next.getId(), instructorId, vehicleId);
    }

    public void deleteCancelledBooking(HttpServletRequest request, String bookingId) {
        String studentId = requireStudent(request).getId();
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

    public List<Booking> getAllBookings(HttpServletRequest request) {
        auth.requireAdmin(request);
        List<Booking> allBookings = bookings.readAllStrict();
        BookingRequestQueue queue = new BookingRequestQueue(allBookings);
        List<Booking> ordered = new java.util.ArrayList<>();
        ordered.addAll(queue.toList());
        allBookings.stream()
                .filter(booking -> booking.getStatus() == BookingStatus.PENDING
                        && booking.getInstructorId() != null)
                .sorted((left, right) -> left.getStartTime().compareTo(right.getStartTime()))
                .forEach(ordered::add);
        allBookings.stream()
                .filter(booking -> booking.getStatus() != BookingStatus.PENDING)
                .sorted((left, right) -> left.getStartTime().compareTo(right.getStartTime()))
                .forEach(ordered::add);
        return List.copyOf(ordered);
    }

    public List<Booking> filterBookings(HttpServletRequest request, String from, String to,
                                        String instructorId, String vehicleId, String status) {
        List<Booking> result = new java.util.ArrayList<>(getAllBookings(request));
        if (from != null && !from.isBlank()) result.removeIf(b -> b.getStartTime().toLocalDate().isBefore(java.time.LocalDate.parse(from)));
        if (to != null && !to.isBlank()) result.removeIf(b -> b.getStartTime().toLocalDate().isAfter(java.time.LocalDate.parse(to)));
        if (instructorId != null && !instructorId.isBlank()) result.removeIf(b -> !instructorId.equals(b.getInstructorId()) && !instructorId.equals(b.getRequestedInstructorId()));
        if (vehicleId != null && !vehicleId.isBlank()) result.removeIf(b -> !vehicleId.equals(b.getVehicleId()) && !vehicleId.equals(b.getRequestedVehicleId()));
        if (status != null && !status.isBlank()) result.removeIf(b -> !b.getStatus().name().equalsIgnoreCase(status));
        return List.copyOf(result);
    }

    public Booking reassign(HttpServletRequest request, String bookingId,
                            String instructorId, String vehicleId) {
        auth.requireAdmin(request);
        synchronized (BOOKING_LOCK) {
            Booking target = findBooking(bookingId);
            String reason = conflictChecker.conflictReason(target, instructorId, vehicleId);
            if (reason != null) throw new ValidationException(reason);
            target.reassign(instructorId, vehicleId);
            bookings.update(target);
            return target;
        }
    }

    public Booking forceCancel(HttpServletRequest request, String bookingId, String reason) {
        auth.requireAdmin(request);
        synchronized (BOOKING_LOCK) {
            Booking target = findBooking(bookingId);
            target.forceCancel(reason);
            bookings.update(target);
            return target;
        }
    }

    public int getQueuePosition(HttpServletRequest request, String bookingId) {
        auth.requireAdmin(request);
        BookingRequestQueue queue = new BookingRequestQueue(bookings.readAllStrict());
        return queue.positionOf(bookingId);
    }

    public List<Booking> getPendingQueue(HttpServletRequest request) {
        auth.requireAdmin(request);
        return new BookingRequestQueue(bookings.readAllStrict()).toList();
    }

    public int getOwnQueuePosition(HttpServletRequest request, String bookingId) {
        User student = requireStudent(request);
        Booking booking = findBooking(bookingId);
        if (!booking.getStudentId().equals(student.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You may only view your own request position.");
        }
        return new BookingRequestQueue(bookings.readAllStrict()).positionOf(bookingId);
    }

    public Booking assign(HttpServletRequest request, String bookingId,
                          String instructorId, String vehicleId) {
        auth.requireAdmin(request);
        synchronized (BOOKING_LOCK) {
            Booking target = findBooking(bookingId);
            BookingRequestQueue queue = new BookingRequestQueue(bookings.readAllStrict());
            Booking next = queue.peek();
            if (next == null || !next.getId().equals(target.getId())) {
                throw new ValidationException("Pending requests must be processed in FIFO order.");
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

    public Booking complete(HttpServletRequest request, String bookingId) {
        auth.requireAdmin(request);
        synchronized (BOOKING_LOCK) {
            Booking target = findBooking(bookingId);
            target.complete();
            bookings.update(target);
            decrementPackageLessons(target);
            return target;
        }
    }

    public Booking acceptAssignedLesson(HttpServletRequest request, String bookingId) {
        User user = auth.requireLogin(request);
        if (user.getRole() != Role.INSTRUCTOR) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        synchronized (BOOKING_LOCK) {
            Booking target = findBooking(bookingId);
            if (!user.getId().equals(target.getInstructorId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            String conflict = conflictChecker == null ? null
                    : conflictChecker.conflictReason(target, target.getInstructorId(), target.getVehicleId());
            if (conflict != null) throw new ValidationException(conflict);
            target.confirm(target.getInstructorId(), target.getVehicleId());
            bookings.update(target);
            return target;
        }
    }

    public Booking declineAssignedLesson(HttpServletRequest request, String bookingId, String reason) {
        User user = auth.requireLogin(request);
        if (user.getRole() != Role.INSTRUCTOR) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        synchronized (BOOKING_LOCK) {
            Booking target = findBooking(bookingId);
            if (!user.getId().equals(target.getInstructorId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            target.reject(reason);
            bookings.update(target);
            return target;
        }
    }

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
        return bookings.readAllStrict().stream()
                .filter(booking -> user.getId().equals(booking.getInstructorId()))
                .sorted((left, right) -> left.getStartTime().compareTo(right.getStartTime()))
                .toList();
    }

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
        synchronized (BOOKING_LOCK) {
            Booking target = findBooking(bookingId);
            if (!user.getId().equals(target.getInstructorId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You can only complete lessons assigned to you.");
            }
            target.complete();
            bookings.update(target);
            decrementPackageLessons(target);
            return target;
        }
    }

    public Booking markNoShowAssignedLesson(HttpServletRequest request, String bookingId) {
        User user = auth.requireLogin(request);
        if (user.getRole() != Role.INSTRUCTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Instructor access is required.");
        }
        synchronized (BOOKING_LOCK) {
            Booking target = findBooking(bookingId);
            if (!user.getId().equals(target.getInstructorId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You may only update lessons assigned to you.");
            }
            target.markNoShow();
            bookings.update(target);
            return target;
        }
    }

    private void ensureResourceAvailable(Booking target, String instructorId,
                                         String vehicleId) {
        for (Booking existing : bookings.readAllStrict()) {
            if (existing.getId().equals(target.getId())
                    || existing.getStatus() == BookingStatus.CANCELLED
                    || existing.getStatus() == BookingStatus.COMPLETED) {
                continue;
            }
            boolean sameInstructor = instructorId.equals(existing.getInstructorId());
            boolean sameVehicle = vehicleId.equals(existing.getVehicleId());
            if ((sameInstructor || sameVehicle)
                    && LessonIntervalUtils.overlaps(target.getStartTime(),
                    target.getEndTime(), existing.getStartTime(), existing.getEndTime())) {
                throw new ValidationException("That instructor or vehicle is already booked.");
            }
        }
    }

    private Booking findBooking(String id) {
        return bookings.findByIdStrict(id)
                .orElseThrow(() -> new RecordNotFoundException("Booking not found."));
    }

    private User requireStudent(HttpServletRequest request) {
        User user = auth.requireLogin(request);
        if (user.getRole() != Role.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Student access is required.");
        }
        return user;
    }

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
