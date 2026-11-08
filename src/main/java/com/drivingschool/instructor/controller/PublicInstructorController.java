/**
============================================================
MEMBER   : IT26101133 - [Name]
MODULE   : Instructors and bubble sort
FILE     : PublicInstructorController.java
WHAT IT DOES (simple words): Prepares pages and accepts forms for public instructor.
REAL-LIFE EXAMPLE: A teacher diary keeps teaching hours, lesson notes and profile details organised.
OOP USED : Encapsulation (private fields and public methods)
CRUD     : See the labelled operations; model and view files support these operations.
============================================================
*/
package com.drivingschool.instructor.controller;

import com.drivingschool.instructor.service.InstructorCatalog;
import com.drivingschool.instructor.model.Instructor;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
// SOLID: S - This class is responsible for web requests and page data.
public class PublicInstructorController {
    // SOLID: D - Receive the instructor catalog contract through the constructor.
    private final InstructorCatalog instructorService;

    // Receives the helpers or validates the details needed to create this object.
    public PublicInstructorController(InstructorCatalog instructorService) {
        this.instructorService = instructorService;
    }

    @GetMapping("/instructors")
    // Prepares the saved records and filter choices needed by this page.
    // CRUD: READ   - finds or lists instructor profiles
    public String list(@RequestParam(defaultValue = "experience") String sortBy,
                       @RequestParam(defaultValue = "desc") String direction,
                       Model model) {
        boolean ascending = "asc".equalsIgnoreCase(direction);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", ascending ? "asc" : "desc");
        List<Instructor> active = new ArrayList<>();
        for (Instructor instructor : instructorService.sortInstructors(sortBy, ascending)) {
            if (instructor.isActive()) active.add(instructor);
        }
        model.addAttribute("instructors", active);
        return "public/instructors";
    }
}
