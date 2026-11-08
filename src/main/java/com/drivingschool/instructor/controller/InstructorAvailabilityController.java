// Module owner: IT26101133
package com.drivingschool.instructor.controller;

import com.drivingschool.auth.annotation.RequireLogin;
import com.drivingschool.auth.service.CsrfTokenService;
import com.drivingschool.instructor.service.InstructorScheduleService;
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
public class InstructorAvailabilityController {
    private final InstructorScheduleService scheduleService;
    private final CsrfTokenService csrf;

    public InstructorAvailabilityController(InstructorScheduleService scheduleService,
                                            CsrfTokenService csrf) {
        this.scheduleService = scheduleService;
        this.csrf = csrf;
    }

    @GetMapping
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
    public String addBlock(HttpServletRequest request,
                           @RequestParam DayOfWeek dayOfWeek,
                           @RequestParam LocalTime startTime,
                           @RequestParam LocalTime endTime,
                           RedirectAttributes flash) {
        scheduleService.addAvailability(request, dayOfWeek, startTime, endTime);
        flash.addFlashAttribute("success", "Availability added.");
        return "redirect:/instructor/availability";
    }

    @PostMapping("/leave")
    public String addLeave(HttpServletRequest request,
                           @RequestParam LocalDate date,
                           @RequestParam String reason,
                           RedirectAttributes flash) {
        scheduleService.addLeave(request, date, reason);
        flash.addFlashAttribute("success", "Leave day added.");
        return "redirect:/instructor/availability";
    }

    @PostMapping("/delete")
    public String deleteBlock(HttpServletRequest request,
                              @RequestParam String id,
                              RedirectAttributes flash) {
        scheduleService.deleteAvailability(request, id);
        flash.addFlashAttribute("success", "Availability removed.");
        return "redirect:/instructor/availability";
    }

    @PostMapping("/leave/delete")
    public String deleteLeave(HttpServletRequest request,
                              @RequestParam String id,
                              RedirectAttributes flash) {
        scheduleService.deleteLeave(request, id);
        flash.addFlashAttribute("success", "Leave day removed.");
        return "redirect:/instructor/availability";
    }
}
