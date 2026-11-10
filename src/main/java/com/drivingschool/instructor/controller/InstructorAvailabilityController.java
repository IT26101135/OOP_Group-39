/**
============================================================
MEMBER   : IT26101133 - [Name]
MODULE   : Instructors and bubble sort
FILE     : InstructorAvailabilityController.java
WHAT IT DOES (simple words): Prepares pages and accepts forms for instructor availability.
REAL-LIFE EXAMPLE: A teacher diary keeps teaching hours, lesson notes and profile details organised.
OOP USED : Encapsulation (private fields and public methods)
CRUD     : See the labelled operations; model and view files support these operations.
============================================================
*/
package com.drivingschool.instructor.controller;

import com.drivingschool.auth.annotation.RequireLogin;
import com.drivingschool.auth.service.CsrfTokenService;
import com.drivingschool.instructor.service.InstructorScheduleOperations;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Controller
@RequireLogin
@RequestMapping("/instructor/availability")
// SOLID: S - This class is responsible for web requests and page data.
public class InstructorAvailabilityController {
    // SOLID: D - Receive schedule operations through their small interface.
    private final InstructorScheduleOperations scheduleService;
    private final CsrfTokenService csrf;

    // Receives the helpers or validates the details needed to create this object.
    public InstructorAvailabilityController(InstructorScheduleOperations scheduleService,
                                            CsrfTokenService csrf) {
        this.scheduleService = scheduleService;
        this.csrf = csrf;
    }

    @GetMapping
    // Loads saved working hours and leave for the signed-in instructor.
    // CRUD: READ   - finds or lists availability blocks
    public String page(HttpServletRequest request, HttpServletResponse response,
                       Model model) {
        response.setHeader("Cache-Control", "no-store");
        model.addAttribute("availability", scheduleService.availability(request));
        model.addAttribute("leaves", scheduleService.leaves(request));
        model.addAttribute("days", DayOfWeek.values());
        model.addAttribute("csrfParameterName", CsrfTokenService.PARAMETER_NAME);
        model.addAttribute("csrfToken", csrf.getOrCreateToken(request));
        return "instructor/availability";
    }

    @PostMapping
    // Accepts a new weekly working-hours form.
    // CRUD: CREATE - adds a new availability block and saves it
    public String addBlock(HttpServletRequest request,
                           @RequestParam DayOfWeek dayOfWeek,
                           @RequestParam LocalTime startTime,
                           @RequestParam LocalTime endTime,
                           RedirectAttributes flash) {
        scheduleService.addAvailability(request, dayOfWeek, startTime, endTime);
        flash.addFlashAttribute("success", "Your working hours have been saved.");
        return "redirect:/instructor/availability";
    }

    @PostMapping("/leave")
    // Records a future day off after checking for duplicate leave and confirmed lessons.
    // CRUD: CREATE - adds a new leave record and saves it
    public String addLeave(HttpServletRequest request,
                           @RequestParam LocalDate date,
                           @RequestParam String reason,
                           RedirectAttributes flash) {
        scheduleService.addLeave(request, date, reason);
        flash.addFlashAttribute("success", "Your day off has been saved.");
        return "redirect:/instructor/availability";
    }

    @PostMapping("/update")
    // Accepts edits to one weekly block and lets the service enforce ownership.
    // CRUD: UPDATE - changes an existing availability block
    public String updateBlock(HttpServletRequest request, @RequestParam String id,
            @RequestParam DayOfWeek dayOfWeek, @RequestParam LocalTime startTime,
            @RequestParam LocalTime endTime, RedirectAttributes flash) {
        scheduleService.updateAvailability(request, id, dayOfWeek, startTime, endTime);
        flash.addFlashAttribute("success", "Working hours updated.");
        return "redirect:/instructor/availability";
    }

    @PostMapping("/leave/update")
    // Changes an owned leave record after checking its new date is safe.
    // CRUD: UPDATE - changes an existing leave record
    public String updateLeave(HttpServletRequest request, @RequestParam String id,
            @RequestParam LocalDate date, @RequestParam String reason, RedirectAttributes flash) {
        scheduleService.updateLeave(request, id, date, reason);
        flash.addFlashAttribute("success", "Your day off has been updated.");
        return "redirect:/instructor/availability";
    }

    @PostMapping("/delete")
    // Keeps the delete block step in one place for this module.
    // CRUD: DELETE - removes a availability block
    public String deleteBlock(HttpServletRequest request,
                              @RequestParam String id,
                              RedirectAttributes flash) {
        scheduleService.deleteAvailability(request, id);
        flash.addFlashAttribute("success", "The working hours have been removed.");
        return "redirect:/instructor/availability";
    }

    @PostMapping("/leave/delete")
    // Removes only a leave record belonging to this instructor.
    // CRUD: DELETE - removes a leave record
    public String deleteLeave(HttpServletRequest request,
                              @RequestParam String id,
                              RedirectAttributes flash) {
        scheduleService.deleteLeave(request, id);
        flash.addFlashAttribute("success", "The day off has been removed.");
        return "redirect:/instructor/availability";
    }
}
