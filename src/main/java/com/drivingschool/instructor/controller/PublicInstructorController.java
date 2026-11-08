// Module owner: IT26101133
package com.drivingschool.instructor.controller;

import com.drivingschool.instructor.service.InstructorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PublicInstructorController {
    private final InstructorService instructorService;

    public PublicInstructorController(InstructorService instructorService) {
        this.instructorService = instructorService;
    }

    @GetMapping("/instructors")
    public String list(@RequestParam(defaultValue = "experience") String sortBy,
                       @RequestParam(defaultValue = "desc") String direction,
                       Model model) {
        boolean ascending = "asc".equalsIgnoreCase(direction);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", ascending ? "asc" : "desc");
        model.addAttribute("instructors",
                instructorService.sortInstructors(sortBy, ascending).stream()
                        .filter(com.drivingschool.instructor.model.Instructor::isActive)
                        .toList());
        return "public/instructors";
    }
}
