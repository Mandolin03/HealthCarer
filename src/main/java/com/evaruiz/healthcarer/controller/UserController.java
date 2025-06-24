package com.evaruiz.healthcarer.controller;


import com.evaruiz.healthcarer.model.DTO.RegisterUserDTO;
import com.evaruiz.healthcarer.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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
    public String registerUser(RegisterUserDTO newUser, Model model) {
        boolean validate = newUser.validate();
        if (!validate) {
            model.addAttribute("error", "Invalid data provided");
            return "register";
        }
        userService.registerUser(newUser);
        return "redirect:/login";
    }

}
