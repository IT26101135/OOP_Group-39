// Module owner: IT26101133
package com.drivingschool.instructor.controller;

import com.drivingschool.auth.annotation.RequireLogin;
import com.drivingschool.auth.model.Role;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.auth.service.CsrfTokenService;
import com.drivingschool.booking.service.BookingService;
import com.drivingschool.instructor.service.InstructorLessonService;
import com.drivingschool.review.service.ReviewService;
import com.drivingschool.instructor.repository.InstructorRepository;
import com.drivingschool.student.api.StudentLookup;
import com.drivingschool.student.dto.StudentSummary;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequireLogin
@RequestMapping("/instructor")
public class InstructorDashboardController {

    private final AuthSessionService auth;
    private final BookingService bookings;
    private final CsrfTokenService csrf;
    private final InstructorLessonService lessonService;
    private final InstructorRepository instructors;
    private final StudentLookup students;
    private final Clock clock;
    private final ReviewService reviews;

    public InstructorDashboardController(AuthSessionService auth,
                                          BookingService bookings,
                                          CsrfTokenService csrf,
                                          InstructorLessonService lessonService,
                                          InstructorRepository instructors,
                                          StudentLookup students,
                                          Clock clock,
                                          ReviewService reviews) {
        this.auth = auth;
        this.bookings = bookings;
        this.csrf = csrf;
        this.lessonService = lessonService;
        this.instructors = instructors;
        this.students = students;
        this.clock = clock;
        this.reviews = reviews;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request,
                            HttpServletResponse response, Model model) {
        requireInstructor(request);
        response.setHeader("Cache-Control", "no-store");
        List<com.drivingschool.booking.model.Booking> assigned =
                bookings.getInstructorBookings(request);
        model.addAttribute("bookings", assigned);
        LocalDate today = LocalDate.now(clock);
        model.addAttribute("todayCount", assigned.stream()
                .filter(booking -> booking.getStartTime().toLocalDate().equals(today)).count());
        model.addAttribute("upcomingCount", assigned.stream()
                .filter(booking -> booking.getStartTime().toLocalDate().isAfter(today)).count());
        model.addAttribute("studentCount", assigned.stream()
                .map(com.drivingschool.booking.model.Booking::getStudentId).distinct().count());
        model.addAttribute("averageRating", instructors.findByIdStrict(
                auth.requireLogin(request).getId()).orElseThrow().getAverageRating());
        model.addAttribute("ownReviews", reviews.reviewsForInstructor(auth.requireLogin(request).getId()));
        model.addAttribute("studentDetails", assigned.stream().collect(
                java.util.stream.Collectors.toMap(
                        com.drivingschool.booking.model.Booking::getId,
                        booking -> students.findById(booking.getStudentId())
                                .orElseGet(() -> new StudentSummary(
                                        booking.getStudentId(), "Unavailable")))));
        model.addAttribute("csrfParameterName", CsrfTokenService.PARAMETER_NAME);
        model.addAttribute("csrfToken", csrf.getOrCreateToken(request));
        return "instructor/lesson-dashboard";
    }

    @PostMapping("/complete")
    public String complete(HttpServletRequest request,
                           @RequestParam String bookingId,
                           RedirectAttributes flash) {
        requireInstructor(request);
        bookings.completeAssignedLesson(request, bookingId);
        flash.addFlashAttribute("success", "Lesson marked complete.");
        return "redirect:/instructor/dashboard";
    }

    @PostMapping("/accept")
    public String accept(HttpServletRequest request, @RequestParam String bookingId,
                         RedirectAttributes flash) {
        bookings.acceptAssignedLesson(request, bookingId);
        flash.addFlashAttribute("success", "Lesson accepted and confirmed.");
        return "redirect:/instructor/dashboard";
    }

    @PostMapping("/decline")
    public String decline(HttpServletRequest request, @RequestParam String bookingId,
                          @RequestParam String reason, RedirectAttributes flash) {
        bookings.declineAssignedLesson(request, bookingId, reason);
        flash.addFlashAttribute("success", "Lesson declined. The student can see your reason and rebook.");
        return "redirect:/instructor/dashboard";
    }

    @PostMapping("/no-show")
    public String noShow(HttpServletRequest request,
                         @RequestParam String bookingId,
                         RedirectAttributes flash) {
        requireInstructor(request);
        bookings.markNoShowAssignedLesson(request, bookingId);
        flash.addFlashAttribute("success", "Attendance marked no-show.");
        return "redirect:/instructor/dashboard";
    }

    @PostMapping("/lesson-notes")
    public String saveNotes(HttpServletRequest request,
                            @RequestParam String bookingId,
                            @RequestParam String notes,
                            @RequestParam int clutchControl,
                            @RequestParam int parking,
                            @RequestParam int roadSigns,
                            @RequestParam int laneDiscipline,
                            @RequestParam(defaultValue = "false") boolean readyForTest,
                            RedirectAttributes flash) {
        lessonService.saveNotes(request, bookingId, notes,
                Map.of("Clutch control", clutchControl, "Parking", parking,
                        "Road signs", roadSigns, "Lane discipline", laneDiscipline),
                readyForTest);
        flash.addFlashAttribute("success", "Lesson notes saved.");
        return "redirect:/instructor/dashboard";
    }

    private void requireInstructor(HttpServletRequest request) {
        if (auth.requireLogin(request).getRole() != Role.INSTRUCTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Instructor access is required.");
        }
    }
}
