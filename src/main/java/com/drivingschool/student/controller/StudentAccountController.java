// Module owner: IT26101135
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
public class StudentAccountController {

    private final StudentAccountService accountService;
    private final AuthSessionService auth;

    public StudentAccountController(StudentAccountService accountService,
                                    AuthSessionService auth) {
        this.accountService = accountService;
        this.auth = auth;
    }

    @PostMapping("/delete")
    public String delete(HttpServletRequest request,
                         RedirectAttributes redirectAttributes) {
        accountService.deleteOwnAccount(request);
        auth.logout(request);
        redirectAttributes.addFlashAttribute("accountDeleted", true);
        return "redirect:/login?accountDeleted";
    }
}
