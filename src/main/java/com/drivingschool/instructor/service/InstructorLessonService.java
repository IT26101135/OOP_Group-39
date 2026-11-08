/**
============================================================
MEMBER   : IT26101133 - [Name]
MODULE   : Instructors and bubble sort
FILE     : InstructorLessonService.java
WHAT IT DOES (simple words): Applies the instructor lesson rules.
REAL-LIFE EXAMPLE: A teacher diary keeps teaching hours, lesson notes and profile details organised.
OOP USED : Encapsulation (private fields and public methods)
CRUD     : See the labelled operations; model and view files support these operations.
============================================================
*/
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
// SOLID: S - This class is responsible for application rules.
public class InstructorLessonService {
    private final BookingRepository bookings;
    private final LessonRecordRepository records;
    private final AuthSessionService auth;
    private final Clock clock;

    // Receives the helpers or validates the details needed to create this object.
    public InstructorLessonService(BookingRepository bookings,
                                   LessonRecordRepository records,
                                   AuthSessionService auth, Clock clock) {
        this.bookings = bookings;
        this.records = records;
        this.auth = auth;
        this.clock = clock;
    }

    // Creates or updates feedback only for a completed lesson assigned to this instructor.
    // CRUD: UPDATE - changes an existing lesson feedback
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
        LessonRecord existing = findLessonRecord(bookingId);
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

    // Totals completed driving time and returns only this student's feedback.
    // CRUD: READ   - finds or lists student progress
    public StudentProgress studentProgress(HttpServletRequest request) {
        User student = auth.requireLogin(request);
        if (student.getRole() != Role.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Student access is required.");
        }
        List<String> completedIds = new ArrayList<>();
        long minutesDriven = 0;
        for (Booking booking : bookings.readAllStrict()) {
            if (booking.getStudentId().equals(student.getId()) && booking.getStatus() == BookingStatus.COMPLETED) {
                completedIds.add(booking.getId());
                minutesDriven += Duration.between(booking.getStartTime(), booking.getEndTime()).toMinutes();
            }
        }
        List<LessonRecord> lessonRecords = new ArrayList<>();
        for (LessonRecord record : records.readAllStrict()) {
            if (completedIds.contains(record.getBookingId())) lessonRecords.add(record);
        }
        return new StudentProgress(completedIds.size(), minutesDriven / 60.0, lessonRecords);
    }
    public Map<String, LessonRecord> ownRecords(HttpServletRequest request) {
        User instructor = auth.requireLogin(request);
        if (instructor.getRole() != Role.INSTRUCTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Instructor access is required.");
        }
        List<String> ownBookingIds = new ArrayList<>();
        for (Booking booking : bookings.readAllStrict()) {
            if (instructor.getId().equals(booking.getInstructorId())) ownBookingIds.add(booking.getId());
        }
        Map<String, LessonRecord> result = new LinkedHashMap<>();
        for (LessonRecord record : records.readAllStrict()) {
            if (ownBookingIds.contains(record.getBookingId())) result.putIfAbsent(record.getBookingId(), record);
        }
        return result;
    }

    // Looks for the saved feedback attached to this lesson.
    private LessonRecord findLessonRecord(String bookingId) {
        for (LessonRecord record : records.readAllStrict()) {
            if (record.getBookingId().equals(bookingId)) return record;
        }
        return null;
    }
}
