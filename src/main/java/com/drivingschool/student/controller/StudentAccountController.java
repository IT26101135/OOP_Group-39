/**
============================================================
MEMBER   : IT26101135 - [Name]
MODULE   : Authentication, Students and integration
FILE     : StudentAccountController.java
WHAT IT DOES (simple words): Prepares pages and accepts forms for student account.
REAL-LIFE EXAMPLE: A reception desk checks accounts and keeps student details organised.
OOP USED : See the labelled examples in this module; views only show data.
CRUD     : See CRUD labels on the record operations.
============================================================
*/
package com.drivingschool.student.controller;

import com.drivingschool.auth.annotation.RequireLogin;
import com.drivingschool.auth.service.AuthSessionService;
import com.drivingschool.student.service.StudentAccountService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequireLogin
@RequestMapping("/student/account")
// SOLID: S - This class is responsible for web requests and pages.
public class StudentAccountController {

    private final StudentAccountService accountService;
    private final AuthSessionService auth;

    // Receives the helpers this class needs so each job stays separate.
    public StudentAccountController(StudentAccountService accountService,
                                    AuthSessionService auth) {
        this.accountService = accountService;
        this.auth = auth;
    }

    @PostMapping("/delete")
    // Checks the operation rules before removing the selected record.
    // CRUD: DELETE - removes a student account
    public String delete(HttpServletRequest request,
                         RedirectAttributes redirectAttributes) {
        accountService.deleteOwnAccount(request);
        auth.logout(request);
        redirectAttributes.addFlashAttribute("accountDeleted", true);
        return "redirect:/login?accountDeleted";
    }
}
