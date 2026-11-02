package com.drivingschool.student.controller;

import com.drivingschool.auth.annotation.RequireLogin;
import com.drivingschool.auth.service.CsrfTokenService;
import com.drivingschool.common.exception.DuplicateRecordException;
import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.student.dto.StudentProfile;
import com.drivingschool.student.form.StudentProfileForm;
import com.drivingschool.student.service.StudentProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequireLogin
@RequestMapping("/student/profile")
public class StudentProfileController {
    private final StudentProfileService profileService;
    private final CsrfTokenService csrfTokenService;

    public StudentProfileController(StudentProfileService profileService,
                                    CsrfTokenService csrfTokenService) {
        this.profileService = profileService;
        this.csrfTokenService = csrfTokenService;
    }

    @InitBinder("profileForm")
    public void configureBinding(WebDataBinder binder) {
        binder.setAllowedFields("name", "email", "phone");
        binder.setAutoGrowNestedPaths(false);
    }

    @GetMapping
    public String showProfile(HttpServletRequest request,
                              HttpServletResponse response, Model model) {
        response.setHeader("Cache-Control", "no-store");
        // The service additionally enforces student-only access.
        model.addAttribute("profile", profileService.getOwnProfile(request));
        model.addAttribute("navProfileActive", true);
        return "student/profile";
    }

    @GetMapping("/edit")
    public String showEdit(HttpServletRequest request,
                           HttpServletResponse response, Model model) {
        response.setHeader("Cache-Control", "no-store");
        StudentProfile profile = profileService.getOwnProfile(request);
        StudentProfileForm form = new StudentProfileForm();
        form.setName(profile.getName());
        form.setEmail(profile.getEmail());
        form.setPhone(profile.getPhone());
        model.addAttribute("profileForm", form);
        return editPage(request, model);
    }

    @PostMapping("/edit")
    public String updateProfile(
            @ModelAttribute("profileForm") StudentProfileForm form,
            BindingResult bindingResult,
            HttpServletRequest request, HttpServletResponse response,
            Model model, RedirectAttributes redirectAttributes) {
        response.setHeader("Cache-Control", "no-store");
        // Check the role even when binding errors prevent a service update.
        profileService.getOwnProfile(request);
        if (bindingResult.hasErrors()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            model.addAttribute("formError", "Check your profile information and try again.");
            return editPage(request, model);
        }
        try {
            profileService.updateOwnProfile(request,
                    form.getName(), form.getEmail(), form.getPhone());
            redirectAttributes.addFlashAttribute("profileUpdated", true);
            return "redirect:/student/profile";
        } catch (ValidationException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            model.addAttribute("formError", exception.getMessage());
            return editPage(request, model);
        } catch (DuplicateRecordException exception) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            model.addAttribute("formError", exception.getMessage());
            return editPage(request, model);
        }
        // Storage and authorization failures go to the existing global handler.
    }

    private String editPage(HttpServletRequest request, Model model) {
        model.addAttribute("csrfParameterName", CsrfTokenService.PARAMETER_NAME);
        model.addAttribute("csrfToken", csrfTokenService.getOrCreateToken(request));
        return "student/profile-edit";
    }
}
