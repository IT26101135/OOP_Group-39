/**
============================================================
MEMBER   : IT26101133 - [Name]
MODULE   : Instructors and bubble sort
FILE     : InstructorDashboardController.java
WHAT IT DOES (simple words): Prepares pages and accepts forms for instructor dashboard.
REAL-LIFE EXAMPLE: A teacher diary keeps teaching hours, lesson notes and profile details organised.
OOP USED : Encapsulation (private fields and public methods)
CRUD     : See the labelled operations; model and view files support these operations.
============================================================
*/
package com.drivingschool.instructor.controller;

import com.drivingschool.auth.annotation.RequireLogin;
import com.drivingschool.auth.model.Role;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.auth.service.CsrfTokenService;
import com.drivingschool.booking.service.BookingService;
import com.drivingschool.instructor.service.InstructorLessonService;
import com.drivingschool.review.service.ReviewService;
import com.drivingschool.instructor.service.InstructorCatalog;
import com.drivingschool.booking.model.Booking;
import com.drivingschool.instructor.model.Instructor;
import com.drivingschool.review.model.Review;
import com.drivingschool.review.model.ReviewStatus;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Set;
import com.drivingschool.student.api.StudentLookup;
import com.drivingschool.student.dto.StudentSummary;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import com.drivingschool.learningpackage.service.EnrollmentPresentationService;
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
// SOLID: S - This class is responsible for web requests and page data.
public class InstructorDashboardController {

    private final AuthSessionService auth;
    private final BookingService bookings;
    private final CsrfTokenService csrf;
    private final InstructorLessonService lessonService;
    // SOLID: D - Instructor details are read through the catalog contract.
    private final InstructorCatalog instructors;
    private final StudentLookup students;
    private final Clock clock;
    private final ReviewService reviews;

    private final EnrollmentPresentationService presentation;

    // Receives the helpers or validates the details needed to create this object.
    public InstructorDashboardController(AuthSessionService auth,
                                          BookingService bookings,
                                          CsrfTokenService csrf,
                                          InstructorLessonService lessonService,
                                          InstructorCatalog instructors,
                                          StudentLookup students,
                                          Clock clock,
                                          ReviewService reviews, EnrollmentPresentationService presentation) {
        this.presentation = presentation;
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
    // Prepares the signed-in instructor's schedule, profile and feedback for the page.
    public String dashboard(HttpServletRequest request,
                            HttpServletResponse response, Model model) {
        requireInstructor(request);
        response.setHeader("Cache-Control", "no-store");
        List<Booking> assigned = bookings.getInstructorBookings(request);
        String instructorId = auth.requireLogin(request).getId();
        Instructor profile = instructors.getInstructorById(instructorId);
        List<Review> ownReviews = reviews.reviewsForInstructor(instructorId);
        model.addAttribute("bookings", assigned);
        model.addAttribute("bookingPackages", presentation.forBookings(assigned));
        model.addAttribute("instructor", profile);
        model.addAttribute("averageRating", profile.getAverageRating());
        model.addAttribute("ownReviews", ownReviews);
        model.addAttribute("lessonRecords", lessonService.ownRecords(request));
        addLessonCounts(assigned, model);
        addRatingCounts(ownReviews, model);
        model.addAttribute("studentDetails", studentDetails(assigned));
        model.addAttribute("csrfParameterName", CsrfTokenService.PARAMETER_NAME);
        model.addAttribute("csrfToken", csrf.getOrCreateToken(request));
        return "instructor/lesson-dashboard";
    }

    @PostMapping("/complete")
    // Marks a confirmed lesson complete through the booking rules.
    public String complete(HttpServletRequest request,
                           @RequestParam String bookingId,
                           RedirectAttributes flash) {
        requireInstructor(request);
        bookings.completeAssignedLesson(request, bookingId);
        flash.addFlashAttribute("success", "Lesson marked complete.");
        return "redirect:/instructor/dashboard";
    }

    @PostMapping("/accept")
    // Checks an assigned lesson again before confirming it.
    public String accept(HttpServletRequest request, @RequestParam String bookingId,
                         RedirectAttributes flash) {
        bookings.acceptAssignedLesson(request, bookingId);
        flash.addFlashAttribute("success", "Lesson accepted and confirmed.");
        return "redirect:/instructor/dashboard";
    }

    @PostMapping("/decline")
    // Records the instructor's reason so the student knows why the lesson was refused.
    public String decline(HttpServletRequest request, @RequestParam String bookingId,
                          @RequestParam String reason, RedirectAttributes flash) {
        bookings.declineAssignedLesson(request, bookingId, reason);
        flash.addFlashAttribute("success", "Lesson declined. The student can see your reason and rebook.");
        return "redirect:/instructor/dashboard";
    }

    @PostMapping("/no-show")
    // Records attendance when the student did not attend the assigned lesson.
    public String noShow(HttpServletRequest request,
                         @RequestParam String bookingId,
                         RedirectAttributes flash) {
        requireInstructor(request);
        bookings.markNoShowAssignedLesson(request, bookingId);
        flash.addFlashAttribute("success", "Attendance marked no-show.");
        return "redirect:/instructor/dashboard";
    }

    @PostMapping("/lesson-notes")
    // Creates or updates feedback only for a completed lesson assigned to this instructor.
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

    // Counts assigned lesson dates and distinct student IDs without changing bookings.
    private void addLessonCounts(List<Booking> assigned, Model model) {
        LocalDate today = LocalDate.now(clock);
        int todayCount = 0;
        int upcomingCount = 0;
        Set<String> studentIds = new HashSet<>();
        for (Booking booking : assigned) {
            LocalDate lessonDate = booking.getStartTime().toLocalDate();
            if (lessonDate.equals(today)) todayCount++;
            if (lessonDate.isAfter(today)) upcomingCount++;
            studentIds.add(booking.getStudentId());
        }
        model.addAttribute("todayCount", todayCount);
        model.addAttribute("upcomingCount", upcomingCount);
        model.addAttribute("studentCount", studentIds.size());
    }

    private Map<String, StudentSummary> studentDetails(List<Booking> assigned) {
        Map<String, StudentSummary> details = new LinkedHashMap<>();
        for (Booking booking : assigned) {
            StudentSummary student = students.findById(booking.getStudentId())
                    .orElse(new StudentSummary(booking.getStudentId(), "Unavailable"));
            details.put(booking.getId(), student);
        }
        return details;
    }

    // Counts approved reviews into one bar per star rating.
    private void addRatingCounts(List<Review> ownReviews, Model model) {
        int[] ratingCounts = new int[6];
        int approvedCount = 0;
        for (Review review : ownReviews) {
            if (review.getStatus() == ReviewStatus.APPROVED) {
                ratingCounts[review.getRating()]++;
                approvedCount++;
            }
        }
        model.addAttribute("ratingCounts", ratingCounts);
        model.addAttribute("approvedReviewCount", approvedCount);
    }

    // Refuses instructor-only work unless the signed-in account has that role.
    private void requireInstructor(HttpServletRequest request) {
        if (auth.requireLogin(request).getRole() != Role.INSTRUCTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Instructor access is required.");
        }
    }
}
