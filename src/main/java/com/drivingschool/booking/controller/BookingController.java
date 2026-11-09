/**
============================================================
MEMBER   : IT26101134 - [Name]
MODULE   : Bookings, FIFO queue and scheduling
FILE     : BookingController.java
WHAT IT DOES (simple words): Prepares pages and accepts forms for booking.
REAL-LIFE EXAMPLE: A reception waiting line handles the oldest request first.
OOP USED : Encapsulation (private fields and public methods)
CRUD     : See the labelled operations; model and view files support these operations.
============================================================
*/
package com.drivingschool.booking.controller;

import com.drivingschool.auth.annotation.RequireLogin;
import com.drivingschool.auth.service.CsrfTokenService;
import com.drivingschool.booking.service.StudentBookingOperations;
import com.drivingschool.booking.service.SlotAvailabilityService;
import com.drivingschool.booking.model.Booking;
import com.drivingschool.booking.model.BookingStatus;
import com.drivingschool.instructor.model.Instructor;
import com.drivingschool.instructor.repository.InstructorRepository;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.student.model.Student;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.time.Clock;
import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.learningpackage.service.EnrollmentPresentationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Controller
@RequireLogin
@RequestMapping("/bookings")
// SOLID: S - This class is responsible for web requests and page data.
public class BookingController {

    // SOLID: D - Student pages receive only the booking operations they need.
    private final StudentBookingOperations bookingService;
    private final CsrfTokenService csrf;
    private final SlotAvailabilityService slotService;
    private final InstructorRepository instructors;
    private final AuthSessionService auth;
    private final Clock clock;

    private final EnrollmentPresentationService presentation;

    // Receives the helpers or validates the details needed to create this object.
    public BookingController(StudentBookingOperations bookingService,
                             CsrfTokenService csrf,
                             SlotAvailabilityService slotService,
                             InstructorRepository instructors,
                             AuthSessionService auth, Clock clock, EnrollmentPresentationService presentation) {
        this.presentation = presentation;
        this.bookingService = bookingService;
        this.csrf = csrf;
        this.slotService = slotService;
        this.instructors = instructors;
        this.auth = auth;
        this.clock = clock;
    }

    @GetMapping
    // Prepares the saved records and filter choices needed by this page.
    // CRUD: READ   - finds or lists bookings
    public String list(HttpServletRequest request, HttpServletResponse response,
                       Model model) {
        response.setHeader("Cache-Control", "no-store");
        List<Booking> ownBookings = bookingService.getOwnBookings(request);
        Student student = (Student) auth.requireLogin(request);
        model.addAttribute("bookings", ownBookings);
        presentation.addStudent(request, model, ownBookings);
        addRequestGroups(ownBookings, model);
        String bookingProblem = bookingService.bookingProblem(request);
        model.addAttribute("bookingProblem", bookingProblem);
        model.addAttribute("canBook", bookingProblem == null);
        model.addAttribute("instructors", activeInstructors());
        model.addAttribute("today", LocalDate.now(clock));
        addQueuePositions(ownBookings, request, model);
        addSlotChoices(request, model);
        addCsrf(request, model);
        return "booking/list";
    }

    @PostMapping
    // Accepts a lesson request and lets the service recheck the chosen slot.
    // CRUD: CREATE - adds a new booking and saves it
    public String request(HttpServletRequest request,
                          @RequestParam String packageId,
                          @RequestParam String instructorId,
                          @RequestParam String startTime,
                          RedirectAttributes flash) {
        bookingService.requestLesson(request, packageId, instructorId, parseStartTime(startTime));
        flash.addFlashAttribute("success", "Lesson request sent. An administrator will confirm the instructor and vehicle.");
        return "redirect:/bookings";
    }

    @PostMapping("/cancel")
    // Sends the owned request through the cancellation and notice-period checks.
    // CRUD: UPDATE - changes an existing booking
    public String cancel(HttpServletRequest request,
                         @RequestParam String bookingId,
                         RedirectAttributes flash) {
        bookingService.cancelOwnBooking(request, bookingId);
        flash.addFlashAttribute("success", "Lesson request cancelled.");
        return "redirect:/bookings";
    }

    @PostMapping("/reschedule")
    // Sends a replacement time through the scheduling checks before changing the old request.
    // CRUD: UPDATE - changes an existing booking
    public String reschedule(HttpServletRequest request, @RequestParam String bookingId,
                             @RequestParam String startTime, RedirectAttributes flash) {
        bookingService.rescheduleOwnBooking(request, bookingId, parseStartTime(startTime));
        flash.addFlashAttribute("success", "A new pending request was added to the end of the queue.");
        return "redirect:/bookings";
    }

    @PostMapping("/delete")
    // Removes a record only after the service has checked its rules.
    // CRUD: DELETE - removes a booking
    public String delete(HttpServletRequest request,
                         @RequestParam String bookingId,
                         RedirectAttributes flash) {
        bookingService.deleteCancelledBooking(request, bookingId);
        flash.addFlashAttribute("success", "Cancelled lesson request deleted.");
        return "redirect:/bookings";
    }

    // Keeps the add request groups step in one place for this module.
    private void addRequestGroups(List<Booking> bookings, Model model) {
        List<Booking> open = new ArrayList<>();
        List<Booking> history = new ArrayList<>();
        for (Booking booking : bookings) {
            if (booking.getStatus() == BookingStatus.PENDING || booking.getStatus() == BookingStatus.CONFIRMED) open.add(booking);
            else history.add(booking);
        }
        model.addAttribute("openBookings", open);
        model.addAttribute("historyBookings", history);
    }

    // Keeps the active instructors step in one place for this module.
    private List<Instructor> activeInstructors() {
        List<Instructor> result = new ArrayList<>();
        for (Instructor instructor : instructors.readAllStrict()) {
            if (instructor.isActive()) result.add(instructor);
        }
        return result;
    }

    // Keeps the add queue positions step in one place for this module.
    private void addQueuePositions(List<Booking> own, HttpServletRequest request, Model model) {
        Map<String, Integer> positions = new LinkedHashMap<>();
        for (Booking booking : own) {
            if (booking.getStatus() == BookingStatus.PENDING && booking.getInstructorId() == null) {
                positions.put(booking.getId(), bookingService.getOwnQueuePosition(request, booking.getId()));
            }
        }
        model.addAttribute("queuePositions", positions);
    }

    // Keeps the add slot choices step in one place for this module.
    private void addSlotChoices(HttpServletRequest request, Model model) {
        String instructorId = request.getParameter("instructorId");
        String dateValue = request.getParameter("date");
        if (instructorId == null || dateValue == null) return;
        model.addAttribute("selectedInstructorId", instructorId);
        model.addAttribute("selectedDate", dateValue);
        try {
            LocalDate date = LocalDate.parse(dateValue);
            model.addAttribute("availableSlots", slotService.availableSlots(instructorId,
                    date, null, auth.requireLogin(request).getId()));
        } catch (DateTimeParseException exception) {
            model.addAttribute("availableSlots", List.of());
            model.addAttribute("slotError", "Choose a valid date.");
        }
    }

    // Adds the form token that proves a write request came from this browser session.
    private void addCsrf(HttpServletRequest request, Model model) {
        model.addAttribute("csrfParameterName", CsrfTokenService.PARAMETER_NAME);
        model.addAttribute("csrfToken", csrf.getOrCreateToken(request));
    }

    // Converts the form time and gives a friendly message for malformed input.
    private LocalDateTime parseStartTime(String value) {
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException exception) {
            throw new ValidationException("Enter a valid lesson start time.");
        }
    }
}
