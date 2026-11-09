/**
============================================================
MEMBER   : IT26101134 - [Name]
MODULE   : Bookings, FIFO queue and scheduling
FILE     : BookingDetailsController.java
WHAT IT DOES (simple words): Opens one authorized booking as a read-only page.
REAL-LIFE EXAMPLE: A student checks their own lesson slip; an admin checks the school diary.
OOP USED : Encapsulation and service-based authorization.
CRUD     : READ only; no booking or lesson balance is changed.
============================================================
*/
package com.drivingschool.booking.controller;

import com.drivingschool.auth.annotation.RequireLogin;
import com.drivingschool.auth.model.Role;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.booking.model.Booking;
import com.drivingschool.booking.model.BookingStatus;
import com.drivingschool.booking.service.BookingService;
import com.drivingschool.learningpackage.service.EnrollmentPresentationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@Controller
@RequireLogin
// SOLID: S - This controller prepares one read-only page after service authorization.
public class BookingDetailsController {
    private final BookingService bookings;
    private final AuthSessionService auth;
    private final EnrollmentPresentationService presentation;

    public BookingDetailsController(BookingService bookings, AuthSessionService auth,
                                    EnrollmentPresentationService presentation) {
        this.bookings = bookings;
        this.auth = auth;
        this.presentation = presentation;
    }

    @GetMapping("/bookings/{bookingId}/details")
    // CRUD: READ - prepare a lesson slip only after checking role and ownership.
    public String details(HttpServletRequest request, HttpServletResponse response,
                          @PathVariable String bookingId, Model model) {
        response.setHeader("Cache-Control", "no-store");
        // Step 1: The service permits only the owning student or an administrator.
        Booking booking = bookings.getBookingDetails(request, bookingId);
        boolean admin = auth.requireLogin(request).getRole() == Role.ADMIN;
        // Step 2: Reuse the existing FIFO rules without changing the waiting line.
        int position = 0;
        if (booking.getStatus() == BookingStatus.PENDING && booking.getInstructorId() == null) {
            if (admin) position = bookings.getQueuePosition(request, bookingId);
            else position = bookings.getOwnQueuePosition(request, bookingId);
        }
        model.addAttribute("booking", booking);
        model.addAttribute("queuePosition", position);
        model.addAttribute("packageDetails", presentation.forBookings(List.of(booking)).get(bookingId));
        model.addAttribute("backUrl", admin ? "/admin/bookings" : "/bookings");
        return "booking/details";
    }
}
