/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : StudentProgressController.java
WHAT IT DOES (simple words): Prepares pages and accepts forms for student progress.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.student.controller;

import com.drivingschool.auth.annotation.RequireLogin;
import com.drivingschool.instructor.service.InstructorLessonService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequireLogin
@RequestMapping("/student/progress")
// SOLID: S - This class is responsible for web requests and pages.
public class StudentProgressController {
    private final InstructorLessonService lessons;

    // Receives the helpers this class needs so each job stays separate.
    public StudentProgressController(InstructorLessonService lessons) {
        this.lessons = lessons;
    }

    @GetMapping
    // Handles progress for this class without changing another class's job.
    public String progress(HttpServletRequest request,
                           HttpServletResponse response, Model model) {
        response.setHeader("Cache-Control", "no-store");
        model.addAttribute("progress", lessons.studentProgress(request));
        return "student/progress";
    }
}
