// Module owner: IT26101133
package com.drivingschool.instructor.service;

import com.drivingschool.auth.model.Role;
import com.drivingschool.auth.model.User;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.booking.model.Booking;
import com.drivingschool.booking.model.BookingStatus;
import com.drivingschool.booking.repository.BookingRepository;
import com.drivingschool.common.exception.RecordNotFoundException;
import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.instructor.model.LessonRecord;
import com.drivingschool.instructor.repository.LessonRecordRepository;
import com.drivingschool.student.dto.StudentProgress;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class InstructorLessonService {
    private final BookingRepository bookings;
    private final LessonRecordRepository records;
    private final AuthSessionService auth;
    private final Clock clock;

    public InstructorLessonService(BookingRepository bookings,
                                   LessonRecordRepository records,
                                   AuthSessionService auth, Clock clock) {
        this.bookings = bookings;
        this.records = records;
        this.auth = auth;
        this.clock = clock;
    }

    public LessonRecord saveNotes(HttpServletRequest request, String bookingId,
                                  String notes, Map<String, Integer> skills,
                                  boolean readyForTest) {
        User instructor = auth.requireLogin(request);
        if (instructor.getRole() != Role.INSTRUCTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Instructor access is required.");
        }
        Booking booking = bookings.findByIdStrict(bookingId)
                .orElseThrow(() -> new RecordNotFoundException("Booking not found."));
        if (!instructor.getId().equals(booking.getInstructorId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You may only record notes for your assigned lessons.");
        }
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new ValidationException("Mark the lesson complete before adding its record.");
        }
        LocalDateTime updatedAt = LocalDateTime.now(clock);
        LessonRecord existing = records.readAllStrict().stream()
                .filter(record -> record.getBookingId().equals(bookingId))
                .findFirst().orElse(null);
        if (existing == null) {
            LessonRecord created = new LessonRecord(records.nextId("N"), bookingId,
                    notes, skills, readyForTest, updatedAt);
            records.save(created);
            return created;
        }
        LessonRecord replacement = new LessonRecord(existing.getId(), bookingId,
                notes, skills, readyForTest, updatedAt);
        records.update(replacement);
        return replacement;
    }

    public StudentProgress studentProgress(HttpServletRequest request) {
        User student = auth.requireLogin(request);
        if (student.getRole() != Role.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Student access is required.");
        }
        List<Booking> completed = bookings.readAllStrict().stream()
                .filter(booking -> booking.getStudentId().equals(student.getId())
                        && booking.getStatus() == BookingStatus.COMPLETED)
                .toList();
        List<String> bookingIds = completed.stream().map(Booking::getId).toList();
        List<LessonRecord> lessonRecords = records.readAllStrict().stream()
                .filter(record -> bookingIds.contains(record.getBookingId())).toList();
        double hours = completed.stream().mapToLong(booking ->
                Duration.between(booking.getStartTime(), booking.getEndTime()).toMinutes())
                .sum() / 60.0;
        return new StudentProgress(completed.size(), hours, lessonRecords);
    }
}
