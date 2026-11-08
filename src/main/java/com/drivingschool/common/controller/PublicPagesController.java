// Module owner: IT26101135
package com.drivingschool.common.controller;

import com.drivingschool.auth.service.CsrfTokenService;
import com.drivingschool.common.service.ContactService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PublicPagesController {

    private final ContactService contactService;
    private final CsrfTokenService csrfTokenService;

    public PublicPagesController(ContactService contactService,
                                 CsrfTokenService csrfTokenService) {
        this.contactService = contactService;
        this.csrfTokenService = csrfTokenService;
    }

    @GetMapping("/about")
    public String about() { return "public/about"; }

    @GetMapping("/faq")
    public String faq() { return "public/faq"; }

    @GetMapping("/contact")
    public String contact(HttpServletRequest request, Model model) {
        model.addAttribute("csrfParameterName", CsrfTokenService.PARAMETER_NAME);
        model.addAttribute("csrfToken", csrfTokenService.getOrCreateToken(request));
        return "public/contact";
    }

    @PostMapping("/contact")
    public String submitContact(@RequestParam String name,
                                @RequestParam String email,
                                @RequestParam String message,
                                RedirectAttributes flash) {
        contactService.submit(name, email, message);
        flash.addFlashAttribute("messageSent", true);
        return "redirect:/contact";
    }
}
