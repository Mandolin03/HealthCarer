package com.evaruiz.healthcarer.controller;


import com.evaruiz.healthcarer.model.DTO.RegisterUserDTO;
import com.evaruiz.healthcarer.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/errorPage")
    public String errorPage() {
        return "errorPage";
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }


   @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/register")
    public String registerUser(RegisterUserDTO newUser, RedirectAttributes redirectAttributes) {
        boolean validate = newUser.validate();
        if (!validate) {
            redirectAttributes.addFlashAttribute("error", "Los datos introducidos no son válidos");
            return "redirect:/errorPage";
        }
        String normalizedEmail = newUser.email().trim();
        if (userService.findByEmail(normalizedEmail)) {
            redirectAttributes.addFlashAttribute("error", "El email ya está en uso");
            return "redirect:/errorPage";
        }
        userService.registerUser(newUser);
        return "redirect:/login";
    }

}
