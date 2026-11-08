// Module owner: IT26101135
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
public class StudentProgressController {
    private final InstructorLessonService lessons;

    public StudentProgressController(InstructorLessonService lessons) {
        this.lessons = lessons;
    }

    @GetMapping
    public String progress(HttpServletRequest request,
                           HttpServletResponse response, Model model) {
        response.setHeader("Cache-Control", "no-store");
        model.addAttribute("progress", lessons.studentProgress(request));
        return "student/progress";
    }
}
