// Module owner: IT26101134
package com.drivingschool.booking.controller;

import com.drivingschool.auth.annotation.RequireLogin;
import com.drivingschool.auth.service.CsrfTokenService;
import com.drivingschool.booking.service.BookingService;
import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.learningpackage.model.LearningPackage;
import com.drivingschool.learningpackage.service.LearningPackageService;
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
public class BookingController {

    private final BookingService bookingService;
    private final LearningPackageService packageService;
    private final CsrfTokenService csrf;
    private final com.drivingschool.booking.service.SlotAvailabilityService slotService;
    private final com.drivingschool.instructor.repository.InstructorRepository instructors;
    private final com.drivingschool.auth.service.AuthSessionService auth;

    public BookingController(BookingService bookingService,
                             LearningPackageService packageService,
                             CsrfTokenService csrf,
                             com.drivingschool.booking.service.SlotAvailabilityService slotService,
                             com.drivingschool.instructor.repository.InstructorRepository instructors,
                             com.drivingschool.auth.service.AuthSessionService auth) {
        this.bookingService = bookingService;
        this.packageService = packageService;
        this.csrf = csrf;
        this.slotService = slotService;
        this.instructors = instructors;
        this.auth = auth;
    }

    @GetMapping
    public String list(HttpServletRequest request, HttpServletResponse response,
                       Model model) {
        response.setHeader("Cache-Control", "no-store");
        java.util.List<com.drivingschool.booking.model.Booking> ownBookings =
                bookingService.getOwnBookings(request);
        model.addAttribute("bookings", ownBookings);
        java.util.Map<String, Integer> queuePositions = new java.util.HashMap<>();
        for (com.drivingschool.booking.model.Booking booking
                : ownBookings) {
            if (booking.getStatus() == com.drivingschool.booking.model.BookingStatus.PENDING) {
                queuePositions.put(booking.getId(),
                        bookingService.getOwnQueuePosition(request, booking.getId()));
            }
        }
        model.addAttribute("queuePositions", queuePositions);
        model.addAttribute("packages", packageService.listPackages().stream()
                .filter(LearningPackage::isActive).toList());
        model.addAttribute("instructors", instructors.readAllStrict().stream()
                .filter(com.drivingschool.instructor.model.Instructor::isActive).toList());
        String instructorId = request.getParameter("instructorId");
        String dateValue = request.getParameter("date");
        if (instructorId != null && dateValue != null) {
            try {
                LocalDate date = LocalDate.parse(dateValue);
                model.addAttribute("availableSlots", slotService.availableSlots(
                        instructorId, date, null, auth.requireLogin(request).getId()));
            } catch (DateTimeParseException ignored) {
                model.addAttribute("availableSlots", java.util.List.of());
                model.addAttribute("slotError", "Choose a valid date.");
            }
            model.addAttribute("selectedInstructorId", instructorId);
            model.addAttribute("selectedDate", dateValue);
        }
        addCsrf(request, model);
        return "booking/list";
    }

    @PostMapping
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
    public String cancel(HttpServletRequest request,
                         @RequestParam String bookingId,
                         RedirectAttributes flash) {
        bookingService.cancelOwnBooking(request, bookingId);
        flash.addFlashAttribute("success", "Lesson request cancelled.");
        return "redirect:/bookings";
    }

    @PostMapping("/reschedule")
    public String reschedule(HttpServletRequest request, @RequestParam String bookingId,
                             @RequestParam String startTime, RedirectAttributes flash) {
        bookingService.rescheduleOwnBooking(request, bookingId, parseStartTime(startTime));
        flash.addFlashAttribute("success", "A new pending request was added to the end of the queue.");
        return "redirect:/bookings";
    }

    @PostMapping("/delete")
    public String delete(HttpServletRequest request,
                         @RequestParam String bookingId,
                         RedirectAttributes flash) {
        bookingService.deleteCancelledBooking(request, bookingId);
        flash.addFlashAttribute("success", "Cancelled lesson request deleted.");
        return "redirect:/bookings";
    }

    private void addCsrf(HttpServletRequest request, Model model) {
        model.addAttribute("csrfParameterName", CsrfTokenService.PARAMETER_NAME);
        model.addAttribute("csrfToken", csrf.getOrCreateToken(request));
    }

    private LocalDateTime parseStartTime(String value) {
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException exception) {
            throw new ValidationException("Enter a valid lesson start time.");
        }
    }
}
