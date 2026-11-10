package com.drivingschool.instructor.controller;

import com.drivingschool.auth.annotation.RequireAdmin;
import com.drivingschool.auth.service.CsrfTokenService;
import com.drivingschool.common.exception.DuplicateRecordException;
import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.instructor.form.InstructorAvailabilityForm;
import com.drivingschool.instructor.form.InstructorCreateForm;
import com.drivingschool.instructor.form.InstructorEditForm;
import com.drivingschool.instructor.model.Instructor;
import com.drivingschool.instructor.service.InstructorAvailabilityService;
import com.drivingschool.instructor.service.InstructorService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@Controller
@RequireAdmin
@RequestMapping("/admin/instructors")
public class InstructorController {

    private final InstructorService instructorService;
    private final InstructorAvailabilityService availabilityService;
    private final CsrfTokenService csrfTokenService;

    public InstructorController(
            InstructorService instructorService,
            InstructorAvailabilityService availabilityService,
            CsrfTokenService csrfTokenService) {

        this.instructorService = instructorService;
        this.availabilityService = availabilityService;
        this.csrfTokenService = csrfTokenService;
    }

    @InitBinder("instructorCreateForm")
    public void configureCreateBinding(WebDataBinder binder) {
        binder.setAllowedFields(
                "name", "email", "phone", "yearsOfExperience");
        binder.setAutoGrowNestedPaths(false);
    }

    @InitBinder("instructorEditForm")
    public void configureEditBinding(WebDataBinder binder) {
        binder.setAllowedFields(
                "name", "email", "phone", "yearsOfExperience");
        binder.setAutoGrowNestedPaths(false);
    }

    @InitBinder("instructorAvailabilityForm")
    public void configureAvailabilityBinding(WebDataBinder binder) {
        binder.setAllowedFields("start", "end");
        binder.setAutoGrowNestedPaths(false);
    }

    @GetMapping
    public String listInstructors(
            HttpServletResponse response,
            Model model) {

        response.setHeader("Cache-Control", "no-store");

        model.addAttribute(
                "instructors",
                instructorService.getInstructorsSortedByExperience());

        return "instructor/list";
    }

    @GetMapping("/create")
    public String showCreateForm(
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {

        response.setHeader("Cache-Control", "no-store");
        model.addAttribute(
                "instructorCreateForm", new InstructorCreateForm());

        return createPage(request, model);
    }

    @PostMapping("/create")
    public String createInstructor(
            @ModelAttribute("instructorCreateForm")
            InstructorCreateForm form,
            BindingResult bindingResult,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model,
            RedirectAttributes redirectAttributes) {

        response.setHeader("Cache-Control", "no-store");

        if (bindingResult.hasErrors()
                || form.getYearsOfExperience() == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            model.addAttribute(
                    "formError",
                    "Please check the instructor information and try again.");
            return createPage(request, model);
        }

        try {
            instructorService.createInstructor(
                    form.getName(),
                    form.getEmail(),
                    form.getPhone(),
                    form.getYearsOfExperience());

            redirectAttributes.addFlashAttribute(
                    "instructorCreated", true);

            return "redirect:/admin/instructors";

        } catch (ValidationException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            model.addAttribute("formError", exception.getMessage());
            return createPage(request, model);

        } catch (DuplicateRecordException exception) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            model.addAttribute("formError", exception.getMessage());
            return createPage(request, model);
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(
            @PathVariable String id,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {

        response.setHeader("Cache-Control", "no-store");

        Instructor instructor = instructorService.getInstructorById(id);

        InstructorEditForm form = new InstructorEditForm();
        form.setName(instructor.getName());
        form.setEmail(instructor.getEmail());
        form.setPhone(instructor.getPhone());
        form.setYearsOfExperience(instructor.getYearsOfExperience());

        model.addAttribute("instructor", instructor);
        model.addAttribute("instructorEditForm", form);

        return editPage(request, model);
    }

    @PostMapping("/{id}/edit")
    public String updateInstructor(
            @PathVariable String id,
            @ModelAttribute("instructorEditForm")
            InstructorEditForm form,
            BindingResult bindingResult,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model,
            RedirectAttributes redirectAttributes) {

        response.setHeader("Cache-Control", "no-store");

        Instructor existing = instructorService.getInstructorById(id);

        if (bindingResult.hasErrors()
                || form.getYearsOfExperience() == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            model.addAttribute(
                    "formError",
                    "Please check the instructor information and try again.");
            model.addAttribute("instructor", existing);
            return editPage(request, model);
        }

        try {
            instructorService.updateInstructor(
                    id,
                    form.getName(),
                    form.getEmail(),
                    form.getPhone(),
                    form.getYearsOfExperience());

            redirectAttributes.addFlashAttribute(
                    "instructorUpdated", true);

            return "redirect:/admin/instructors";

        } catch (ValidationException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            model.addAttribute("formError", exception.getMessage());
            model.addAttribute("instructor", existing);
            return editPage(request, model);

        } catch (DuplicateRecordException exception) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            model.addAttribute("formError", exception.getMessage());
            model.addAttribute("instructor", existing);
            return editPage(request, model);
        }
    }

    @GetMapping("/{id}/availability/create")
    public String showAvailabilityForm(
            @PathVariable String id,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {

        response.setHeader("Cache-Control", "no-store");

        Instructor instructor = instructorService.getInstructorById(id);
        ensureActive(instructor);

        model.addAttribute("instructor", instructor);
        model.addAttribute(
                "instructorAvailabilityForm",
                new InstructorAvailabilityForm());

        return availabilityPage(request, model);
    }

    @PostMapping("/{id}/availability/create")
    public String createAvailability(
            @PathVariable String id,
            @ModelAttribute("instructorAvailabilityForm")
            InstructorAvailabilityForm form,
            BindingResult bindingResult,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model,
            RedirectAttributes redirectAttributes) {

        response.setHeader("Cache-Control", "no-store");

        Instructor instructor = instructorService.getInstructorById(id);
        ensureActive(instructor);
        model.addAttribute("instructor", instructor);

        if (bindingResult.hasErrors()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            model.addAttribute(
                    "formError",
                    "Please check the availability information and try again.");
            return availabilityPage(request, model);
        }

        try {
            LocalDateTime start = parseDateTime(form.getStart());
            LocalDateTime end = parseDateTime(form.getEnd());

            if (!start.isBefore(end)) {
                throw new ValidationException(
                        "Start time must be before end time.");
            }

            availabilityService.addAvailability(
                    request, id, start, end);

            redirectAttributes.addFlashAttribute(
                    "availabilityCreated", true);

            return "redirect:/admin/instructors";

        } catch (ValidationException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            model.addAttribute("formError", exception.getMessage());
            return availabilityPage(request, model);
        }
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(
                    "Start and end times are required.");
        }

        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException exception) {
            throw new ValidationException(
                    "Enter a valid date and time.");
        }
    }

    private void ensureActive(Instructor instructor) {
        if (!instructor.isActive()) {
            throw new ValidationException(
                    "Cannot add availability for an inactive instructor.");
        }
    }

    private String createPage(
            HttpServletRequest request, Model model) {
        addCsrfAttributes(request, model);
        return "instructor/create";
    }

    private String editPage(
            HttpServletRequest request, Model model) {
        addCsrfAttributes(request, model);
        return "instructor/edit";
    }

    private String availabilityPage(
            HttpServletRequest request, Model model) {
        addCsrfAttributes(request, model);
        return "instructor/availability-create";
    }

    private void addCsrfAttributes(
            HttpServletRequest request, Model model) {

        model.addAttribute(
                "csrfParameterName",
                CsrfTokenService.PARAMETER_NAME);

        model.addAttribute(
                "csrfToken",
                csrfTokenService.getOrCreateToken(request));
    }
}