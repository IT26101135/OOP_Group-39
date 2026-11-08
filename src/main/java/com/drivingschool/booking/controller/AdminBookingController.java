// Module owner: IT26101134
package com.drivingschool.booking.controller;

import com.drivingschool.auth.annotation.RequireAdmin;
import com.drivingschool.auth.service.CsrfTokenService;
import com.drivingschool.booking.service.BookingService;
import com.drivingschool.instructor.repository.InstructorRepository;
import com.drivingschool.vehicle.repository.VehicleRepository;
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
import com.drivingschool.booking.model.BookingStatus;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequireAdmin
@RequestMapping("/admin/bookings")
public class AdminBookingController {

    private final BookingService bookingService;
    private final InstructorRepository instructors;
    private final VehicleRepository vehicles;
    private final CsrfTokenService csrf;

    public AdminBookingController(BookingService bookingService,
                                  InstructorRepository instructors,
                                  VehicleRepository vehicles,
                                  CsrfTokenService csrf) {
        this.bookingService = bookingService;
        this.instructors = instructors;
        this.vehicles = vehicles;
        this.csrf = csrf;
    }

    @GetMapping
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
        List<Booking> pendingQueue = bookingService.getPendingQueue(request);
        model.addAttribute("pendingQueue", pendingQueue);
        Map<String, Integer> queuePositions = new HashMap<>();
        for (Booking booking : bookingList) {
            if (booking.getStatus() == BookingStatus.PENDING && booking.getInstructorId() == null) {
                queuePositions.put(booking.getId(), bookingService.getQueuePosition(request, booking.getId()));
            }
        }
        model.addAttribute("queuePositions", queuePositions);
        model.addAttribute("queueSize", pendingQueue.size());
        model.addAttribute("filterFrom", from);
        model.addAttribute("filterTo", to);
        model.addAttribute("filterInstructor", instructorId);
        model.addAttribute("filterVehicle", vehicleId);
        model.addAttribute("filterStatus", status);
        model.addAttribute("instructors", instructors.readAllStrict().stream()
                .filter(com.drivingschool.instructor.model.Instructor::isActive).toList());
        model.addAttribute("vehicles", vehicles.readAllStrict().stream()
                .filter(v -> v.getOperationalStatus()
                        == com.drivingschool.vehicle.model.VehicleOperationalStatus.IN_SERVICE).toList());
        model.addAttribute("csrfParameterName", CsrfTokenService.PARAMETER_NAME);
        model.addAttribute("csrfToken", csrf.getOrCreateToken(request));
        return "admin/bookings";
    }

    @PostMapping("/reassign")
    public String reassign(HttpServletRequest request, @RequestParam String bookingId,
                           @RequestParam String instructorId, @RequestParam String vehicleId,
                           RedirectAttributes flash) {
        bookingService.reassign(request, bookingId, instructorId, vehicleId);
        flash.addFlashAttribute("success", "Booking reassigned.");
        return "redirect:/admin/bookings";
    }

    @PostMapping("/force-cancel")
    public String forceCancel(HttpServletRequest request, @RequestParam String bookingId,
                              @RequestParam String reason, RedirectAttributes flash) {
        bookingService.forceCancel(request, bookingId, reason);
        flash.addFlashAttribute("success", "Booking force-cancelled.");
        return "redirect:/admin/bookings";
    }

    @PostMapping("/assign")
    public String assign(HttpServletRequest request,
                         @RequestParam String bookingId,
                         @RequestParam String instructorId,
                         @RequestParam String vehicleId,
                         RedirectAttributes flash) {
        bookingService.assign(request, bookingId, instructorId, vehicleId);
        flash.addFlashAttribute("success", "Instructor and vehicle assigned.");
        return "redirect:/admin/bookings";
    }

    @PostMapping("/process-next")
    public String processNext(HttpServletRequest request,
                              @RequestParam String instructorId,
                              @RequestParam String vehicleId,
                              RedirectAttributes flash) {
        Booking result = bookingService.processNext(request, instructorId, vehicleId);
        flash.addFlashAttribute(result.getStatus() == BookingStatus.REJECTED ? "error" : "success",
                result.getStatus() == BookingStatus.REJECTED
                        ? "Request rejected: " + result.getRejectionReason()
                        : "The oldest request was assigned and sent to its instructor.");
        return "redirect:/admin/bookings";
    }

    @PostMapping("/complete")
    public String complete(HttpServletRequest request,
                           @RequestParam String bookingId,
                           RedirectAttributes flash) {
        bookingService.complete(request, bookingId);
        flash.addFlashAttribute("success", "Lesson marked complete.");
        return "redirect:/admin/bookings";
    }
}
