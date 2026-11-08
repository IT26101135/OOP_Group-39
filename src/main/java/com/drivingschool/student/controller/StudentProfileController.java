/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : StudentProfileController.java
WHAT IT DOES (simple words): Prepares pages and accepts forms for student profile.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.student.controller;

import com.drivingschool.auth.annotation.RequireLogin;
import com.drivingschool.auth.service.CsrfTokenService;
import com.drivingschool.common.exception.DuplicateRecordException;
import com.drivingschool.common.exception.ValidationException;
import com.drivingschool.student.dto.StudentProfile;
import com.drivingschool.student.form.StudentProfileForm;
import com.drivingschool.student.api.StudentProfiles;
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
// SOLID: S - This class is responsible for web requests and pages.
public class StudentProfileController {
    // SOLID: D - Receive the small profile contract through the constructor.
    private final StudentProfiles profileService;
    private final CsrfTokenService csrfTokenService;

    // Receives the helpers this class needs so each job stays separate.
    public StudentProfileController(StudentProfiles profileService,
                                    CsrfTokenService csrfTokenService) {
        this.profileService = profileService;
        this.csrfTokenService = csrfTokenService;
    }

    @InitBinder("profileForm")
    // Limits form fields so students cannot overwrite their role or lesson balance.
    public void configureBinding(WebDataBinder binder) {
        binder.setAllowedFields("name", "email", "phone", "nic", "dateOfBirth",
                "address", "licenseTypeWanted", "emergencyContact");
        binder.setAutoGrowNestedPaths(false);
    }

    @GetMapping
    // Prepares the profile page and its existing form details.
    // CRUD: READ   - finds or lists student profiles
    public String showProfile(HttpServletRequest request,
                              HttpServletResponse response, Model model) {
        response.setHeader("Cache-Control", "no-store");
        // The service additionally enforces student-only access.
        model.addAttribute("profile", profileService.getOwnProfile(request));
        model.addAttribute("navProfileActive", true);
        return "student/profile";
    }

    @GetMapping("/edit")
    // Prepares the edit page and its existing form details.
    // CRUD: READ   - finds or lists student profiles
    public String showEdit(HttpServletRequest request,
                           HttpServletResponse response, Model model) {
        response.setHeader("Cache-Control", "no-store");
        StudentProfile profile = profileService.getOwnProfile(request);
        StudentProfileForm form = new StudentProfileForm();
        form.setName(profile.getName());
        form.setEmail(profile.getEmail());
        form.setPhone(profile.getPhone());
        form.setNic(profile.getNic());
        form.setDateOfBirth(profile.getDateOfBirth());
        form.setAddress(profile.getAddress());
        form.setLicenseTypeWanted(profile.getLicenseTypeWanted());
        form.setEmergencyContact(profile.getEmergencyContact());
        model.addAttribute("profileForm", form);
        return editPage(request, model);
    }

    @PostMapping("/edit")
    // Accepts the profile form and keeps helpful messages beside invalid details.
    // CRUD: UPDATE - changes an existing student profile
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
                    form.getName(), form.getEmail(), form.getPhone(),
                    form.getNic(), form.getDateOfBirth(), form.getAddress(),
                    form.getLicenseTypeWanted(), form.getEmergencyContact());
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

    // Handles edit page for this class without changing another class's job.
    private String editPage(HttpServletRequest request, Model model) {
        model.addAttribute("csrfParameterName", CsrfTokenService.PARAMETER_NAME);
        model.addAttribute("csrfToken", csrfTokenService.getOrCreateToken(request));
        return "student/profile-edit";
    }
}
