/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : AdminManagementController.java
WHAT IT DOES (simple words): Prepares pages and accepts forms for admin management.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.admin.controller;

import com.drivingschool.auth.annotation.RequireAdmin;
import com.drivingschool.auth.service.CsrfTokenService;
import com.drivingschool.instructor.service.InstructorService;
import com.drivingschool.instructor.service.InstructorAccountService;
import com.drivingschool.learningpackage.service.LearningPackageService;
import com.drivingschool.student.service.StudentAdministrationService;
import com.drivingschool.learningpackage.model.PackageType;
import com.drivingschool.auth.model.AccountStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
@Controller
@RequireAdmin
@RequestMapping("/admin")
// SOLID: S - This class is responsible for web requests and pages.
public class AdminManagementController {

    private final InstructorService instructorService;
    private final InstructorAccountService instructorAccountService;
    private final LearningPackageService packageService;
    private final CsrfTokenService csrfTokenService;
    private final StudentAdministrationService studentAdministrationService;

    // Receives the helpers this class needs so each job stays separate.
    public AdminManagementController(
            InstructorService instructorService,
            InstructorAccountService instructorAccountService,
            LearningPackageService packageService,
            CsrfTokenService csrfTokenService,
            StudentAdministrationService studentAdministrationService) {
        this.instructorService = instructorService;
        this.instructorAccountService = instructorAccountService;
        this.packageService = packageService;
        this.csrfTokenService = csrfTokenService;
        this.studentAdministrationService = studentAdministrationService;
    }

    @GetMapping("/students")
    // Handles students for this class without changing another class's job.
    // CRUD: READ   - finds or lists student accounts
    public String students(HttpServletRequest request, HttpServletResponse response,
                           @RequestParam(defaultValue = "") String q, Model model) {
        response.setHeader("Cache-Control", "no-store");
        model.addAttribute("students", studentAdministrationService.search(request, q));
        model.addAttribute("query", q);
        model.addAttribute("statuses", AccountStatus.values());
        addCsrf(request, model);
        return "admin/students";
    }

    @PostMapping("/students/status")
    // Handles student status for this class without changing another class's job.
    // CRUD: UPDATE - changes an existing student account
    public String studentStatus(HttpServletRequest request,
                                @RequestParam String id,
                                @RequestParam AccountStatus status,
                                RedirectAttributes flash) {
        studentAdministrationService.setStatus(request, id, status);
        flash.addFlashAttribute("success", "Student account status updated.");
        return "redirect:/admin/students";
    }

    @PostMapping("/students/update")
    // Handles update student for this class without changing another class's job.
    // CRUD: UPDATE - changes an existing student account
    public String updateStudent(HttpServletRequest request,
                                @RequestParam String id,
                                @RequestParam String name,
                                @RequestParam String email,
                                @RequestParam String phone,
                                RedirectAttributes flash) {
        studentAdministrationService.update(request, id, name, email, phone);
        flash.addFlashAttribute("success", "Student details updated.");
        return "redirect:/admin/students";
    }

    @PostMapping("/students/delete")
    // Handles delete student for this class without changing another class's job.
    // CRUD: DELETE - removes a student account
    public String deleteStudent(HttpServletRequest request,
                                @RequestParam String id,
                                RedirectAttributes flash) {
        studentAdministrationService.delete(request, id);
        flash.addFlashAttribute("success", "Student deleted.");
        return "redirect:/admin/students";
    }

    @GetMapping("/instructors")
    // Handles instructors for this class without changing another class's job.
    // CRUD: READ   - finds or lists instructor profiles
    public String instructors(HttpServletRequest request,
                              HttpServletResponse response, Model model) {
        response.setHeader("Cache-Control", "no-store");
        model.addAttribute("instructors", instructorService.getAllInstructors());
        addCsrf(request, model);
        return "admin/instructors";
    }

    @PostMapping("/instructors")
    // Handles create instructor for this class without changing another class's job.
    // CRUD: CREATE - adds a new instructor profile and saves it
    public String createInstructor(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam int yearsOfExperience,
            @RequestParam String password,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) String licenseNumber,
            @RequestParam(required = false) String qualifications,
            @RequestParam(required = false) String photoPath,
            @RequestParam(required = false) List<String> vehicleTypes,
            RedirectAttributes redirectAttributes) {
        instructorAccountService.create(name, email, phone, yearsOfExperience, password,
                bio, licenseNumber, qualifications, photoPath, vehicleTypes);
        redirectAttributes.addFlashAttribute("success", "Instructor and login account added.");
        return "redirect:/admin/instructors";
    }

    @PostMapping("/instructors/update")
    // Handles update instructor for this class without changing another class's job.
    // CRUD: UPDATE - changes an existing instructor profile
    public String updateInstructor(@RequestParam String id,
                                  @RequestParam String name,
                                  @RequestParam String email,
                                  @RequestParam String phone,
                                  @RequestParam int yearsOfExperience,
                                  @RequestParam(required = false) String bio,
                                  @RequestParam(required = false) String licenseNumber,
                                  @RequestParam(required = false) String qualifications,
                                  @RequestParam(required = false) String photoPath,
                                  @RequestParam(required = false) List<String> vehicleTypes,
                                  RedirectAttributes redirectAttributes) {
        instructorAccountService.update(id, name, email, phone, yearsOfExperience,
                bio, licenseNumber, qualifications, photoPath, vehicleTypes);
        redirectAttributes.addFlashAttribute("success", "Instructor details updated.");
        return "redirect:/admin/instructors";
    }

    @PostMapping("/instructors/profile")
    // Handles update instructor profile for this class without changing another class's job.
    // CRUD: UPDATE - changes an existing instructor profile
    public String updateInstructorProfile(@RequestParam String id,
                                          @RequestParam(required = false) String bio,
                                          @RequestParam(required = false) String licenseNumber,
                                          @RequestParam(required = false) String qualifications,
                                          @RequestParam(required = false) String photoPath,
                                          @RequestParam(required = false) List<String> vehicleTypes,
                                          RedirectAttributes redirectAttributes) {
        var current = instructorService.getInstructorById(id);
        instructorAccountService.update(id, current.getName(), current.getEmail(),
                current.getPhone(), current.getYearsOfExperience(), bio,
                licenseNumber, qualifications, photoPath, vehicleTypes);
        redirectAttributes.addFlashAttribute("success", "Instructor profile updated.");
        return "redirect:/admin/instructors";
    }

    @PostMapping("/instructors/status")
    // Stores the instructor status after the checks required by this record.
    // CRUD: UPDATE - changes an existing instructor profile
    public String setInstructorStatus(@RequestParam String id,
                                      @RequestParam boolean active,
                                      RedirectAttributes redirectAttributes) {
        instructorService.setInstructorActive(id, active);
        redirectAttributes.addFlashAttribute("success", "Instructor availability updated.");
        return "redirect:/admin/instructors";
    }

    @PostMapping("/instructors/delete")
    // Handles delete instructor for this class without changing another class's job.
    // CRUD: DELETE - removes a instructor profile
    public String deleteInstructor(@RequestParam String id,
                                   RedirectAttributes redirectAttributes) {
        instructorAccountService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Instructor deleted.");
        return "redirect:/admin/instructors";
    }

    @GetMapping("/packages")
    // Handles packages for this class without changing another class's job.
    // CRUD: READ   - finds or lists packages
    public String packages(HttpServletRequest request,
                           HttpServletResponse response, Model model) {
        response.setHeader("Cache-Control", "no-store");
        model.addAttribute("packages", packageService.listPackages());
        model.addAttribute("packageTypes", PackageType.values());
        addCsrf(request, model);
        return "admin/packages";
    }

    @PostMapping("/packages")
    // Handles create package for this class without changing another class's job.
    // CRUD: CREATE - adds a new package and saves it
    public String createPackage(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam BigDecimal price,
            @RequestParam int lessonCount,
            @RequestParam int validityDays,
            @RequestParam PackageType type,
            @RequestParam int durationHours,
            @RequestParam(defaultValue = "false") boolean active,
            RedirectAttributes redirectAttributes) {
        packageService.createPackage(name, description, price, lessonCount,
                validityDays, active, type, durationHours);
        redirectAttributes.addFlashAttribute("success", "Package added.");
        return "redirect:/admin/packages";
    }

    @PostMapping("/packages/update")
    // Handles update package for this class without changing another class's job.
    // CRUD: UPDATE - changes an existing package
    public String updatePackage(@RequestParam String id,
                                @RequestParam String name,
                                @RequestParam String description,
                                @RequestParam BigDecimal price,
                                @RequestParam int lessonCount,
                                @RequestParam int validityDays,
                                @RequestParam PackageType type,
                                @RequestParam int durationHours,
                                RedirectAttributes redirectAttributes) {
        packageService.updatePackage(id, name, description, price, lessonCount,
                validityDays, type, durationHours);
        redirectAttributes.addFlashAttribute("success", "Package details updated.");
        return "redirect:/admin/packages";
    }

    @PostMapping("/packages/status")
    // Stores the package status after the checks required by this record.
    // CRUD: UPDATE - changes an existing package
    public String setPackageStatus(@RequestParam String id,
                                   @RequestParam boolean active,
                                   RedirectAttributes redirectAttributes) {
        packageService.setPackageActive(id, active);
        redirectAttributes.addFlashAttribute("success", "Package availability updated.");
        return "redirect:/admin/packages";
    }

    @PostMapping("/packages/delete")
    // Handles delete package for this class without changing another class's job.
    // CRUD: DELETE - removes a package
    public String deletePackage(@RequestParam String id,
                                RedirectAttributes redirectAttributes) {
        packageService.deletePackage(id);
        redirectAttributes.addFlashAttribute("success", "Package deleted.");
        return "redirect:/admin/packages";
    }

    // Handles add csrf for this class without changing another class's job.
    private void addCsrf(HttpServletRequest request, Model model) {
        model.addAttribute("csrfParameterName", CsrfTokenService.PARAMETER_NAME);
        model.addAttribute("csrfToken", csrfTokenService.getOrCreateToken(request));
    }
}
