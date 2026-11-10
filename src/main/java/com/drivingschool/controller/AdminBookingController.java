/**
============================================================
MEMBER   : IT26101134 - [Name]
MODULE   : Bookings, FIFO queue and scheduling
FILE     : AdminBookingController.java
WHAT IT DOES (simple words): Prepares pages and accepts forms for admin booking.
REAL-LIFE EXAMPLE: A reception waiting line handles the oldest request first.
OOP USED : Encapsulation (private fields and public methods)
CRUD     : See the labelled operations; model and view files support these operations.
============================================================
*/
package com.drivingschool.booking.controller;

import com.drivingschool.auth.annotation.RequireAdmin;
import com.drivingschool.auth.service.CsrfTokenService;
import com.drivingschool.booking.service.AdminBookingOperations;
import com.drivingschool.instructor.model.Instructor;
import com.drivingschool.vehicle.model.Vehicle;
import com.drivingschool.vehicle.model.VehicleOperationalStatus;
import java.util.ArrayList;
import com.drivingschool.instructor.repository.InstructorRepository;
import com.drivingschool.vehicle.repository.VehicleRepository;
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
import com.drivingschool.booking.model.Booking;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequireAdmin
@RequestMapping("/admin/bookings")
// SOLID: S - This class is responsible for web requests and page data.
public class AdminBookingController {

    // SOLID: D - Admin pages receive the booking administration contract.
    private final AdminBookingOperations bookingService;
    private final InstructorRepository instructors;
    private final VehicleRepository vehicles;
    private final CsrfTokenService csrf;

    private final EnrollmentPresentationService presentation;

    // Receives the helpers or validates the details needed to create this object.
    public AdminBookingController(AdminBookingOperations bookingService,
                                  InstructorRepository instructors,
                                  VehicleRepository vehicles,
                                  CsrfTokenService csrf, EnrollmentPresentationService presentation) {
        this.presentation = presentation;
        this.bookingService = bookingService;
        this.instructors = instructors;
        this.vehicles = vehicles;
        this.csrf = csrf;
    }

    @GetMapping
    // Prepares the saved records and filter choices needed by this page.
    // CRUD: READ   - finds or lists bookings
    public String list(HttpServletRequest request, HttpServletResponse response,
                       Model model,
                       @RequestParam(required = false) String from,
                       @RequestParam(required = false) String to,
                       @RequestParam(required = false) String instructorId,
                       @RequestParam(required = false) String vehicleId,
                       @RequestParam(required = false) String status) {
        response.setHeader("Cache-Control", "no-store");
        List<Booking> bookingList = bookingService.filterBookings(request, from, to, instructorId, vehicleId, status);
        model.addAttribute("bookings", bookingList);
        model.addAttribute("bookingPackages", presentation.forBookings(bookingList));
        List<Booking> pendingQueue = bookingService.getPendingQueue(request);
        model.addAttribute("pendingQueue", pendingQueue);
        Map<String, Integer> queuePositions = new HashMap<>();
        for (int index = 0; index < pendingQueue.size(); index++) {
            queuePositions.put(pendingQueue.get(index).getId(), index + 1);
        }
        model.addAttribute("queuePositions", queuePositions);
        model.addAttribute("queueSize", pendingQueue.size());
        model.addAttribute("filterFrom", from);
        model.addAttribute("filterTo", to);
        model.addAttribute("filterInstructor", instructorId);
        model.addAttribute("filterVehicle", vehicleId);
        model.addAttribute("filterStatus", status == null ? "" : status.trim().toUpperCase(java.util.Locale.ROOT));
        model.addAttribute("bookingStatuses", Booking.BookingStatus.values());
        model.addAttribute("instructors", activeInstructors());
        model.addAttribute("vehicles", availableVehicles());
        model.addAttribute("csrfParameterName", CsrfTokenService.PARAMETER_NAME);
        model.addAttribute("csrfToken", csrf.getOrCreateToken(request));
        return "admin/bookings";
    }

    @PostMapping("/reassign")
    // Checks conflicts before changing an existing confirmed lesson's resources.
    // CRUD: UPDATE - changes an existing booking
    public String reassign(HttpServletRequest request, @RequestParam String bookingId,
                           @RequestParam String instructorId, @RequestParam String vehicleId,
                           RedirectAttributes flash) {
        bookingService.reassign(request, bookingId, instructorId, vehicleId);
        flash.addFlashAttribute("success", "The instructor and vehicle have been updated.");
        return "redirect:/admin/bookings";
    }

    @PostMapping("/force-cancel")
    // Stores the administrator's reason when a lesson must be cancelled.
    // CRUD: UPDATE - changes an existing booking
    public String forceCancel(HttpServletRequest request, @RequestParam String bookingId,
                              @RequestParam String reason, RedirectAttributes flash) {
        bookingService.forceCancel(request, bookingId, reason);
        flash.addFlashAttribute("success", "The lesson has been cancelled.");
        return "redirect:/admin/bookings";
    }

    @PostMapping("/assign")
    // Allows assignment only for the FIFO head and records any conflict reason.
    // CRUD: UPDATE - changes an existing booking
    public String assign(HttpServletRequest request,
                         @RequestParam String bookingId,
                         @RequestParam String instructorId,
                         @RequestParam String vehicleId,
                         RedirectAttributes flash) {
        Booking result = bookingService.assign(request, bookingId, instructorId, vehicleId);
        if (result.getStatus() == Booking.BookingStatus.REJECTED) {
            flash.addFlashAttribute("error", "Request rejected: " + result.getRejectionReason());
        } else {
            flash.addFlashAttribute("success", "Instructor and vehicle assigned.");
        }
        return "redirect:/admin/bookings";
    }

    @PostMapping("/process-next")
    // Takes only the FIFO head for assignment and checks it again before saving.
    // CRUD: UPDATE - changes an existing booking
    public String processNext(HttpServletRequest request,
                              @RequestParam String instructorId,
                              @RequestParam String vehicleId,
                              RedirectAttributes flash) {
        Booking result = bookingService.processNext(request, instructorId, vehicleId);
        flash.addFlashAttribute(result.getStatus() == Booking.BookingStatus.REJECTED ? "error" : "success",
                result.getStatus() == Booking.BookingStatus.REJECTED
                        ? "Request rejected: " + result.getRejectionReason()
                        : "The oldest request was assigned and sent to its instructor.");
        return "redirect:/admin/bookings";
    }

    @PostMapping("/complete")
    // Marks a confirmed lesson complete through the booking rules.
    // CRUD: UPDATE - changes an existing booking
    public String complete(HttpServletRequest request,
                           @RequestParam String bookingId,
                           RedirectAttributes flash) {
        bookingService.complete(request, bookingId);
        flash.addFlashAttribute("success", "Lesson marked complete.");
        return "redirect:/admin/bookings";
    }
    // Keeps the active instructors step in one place for this module.
    private List<Instructor> activeInstructors() {
        List<Instructor> result = new ArrayList<>();
        for (Instructor instructor : instructors.readAllStrict()) {
            if (instructor.isActive()) result.add(instructor);
        }
        return result;
    }

    // Keeps the available vehicles step in one place for this module.
    private List<Vehicle> availableVehicles() {
        List<Vehicle> result = new ArrayList<>();
        for (Vehicle vehicle : vehicles.readAllStrict()) {
            if (vehicle.getOperationalStatus() == VehicleOperationalStatus.IN_SERVICE) result.add(vehicle);
        }
        return result;
    }
}
