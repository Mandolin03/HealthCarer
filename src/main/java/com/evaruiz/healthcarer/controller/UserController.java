package com.evaruiz.healthcarer.controller;


import com.evaruiz.healthcarer.model.DTO.LoggedUser;
import com.evaruiz.healthcarer.model.DTO.RegisterUserDTO;
import com.evaruiz.healthcarer.model.UserDB;
import com.evaruiz.healthcarer.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private static Long getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        LoggedUser current = (LoggedUser) authentication.getPrincipal();
        return current.getId();
    }

    @GetMapping("/errorPage")
    public String errorPage() {
        return "errorPage";
    }

    @GetMapping("/")
    public String home(Model model, RedirectAttributes redirectAttributes) {
        Long user = getCurrentUser();
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión para acceder a esta página");
            return "redirect:/login";
        }
        model.addAttribute("id", user);
        return "redirect:/index";
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
        if (userService.findByEmail(newUser.email())) {
            redirectAttributes.addFlashAttribute("error", "El email ya está en uso");
            return "redirect:/errorPage";
        }
        userService.registerUser(newUser);
        return "redirect:/login";
    }

    @GetMapping("/users/{id}")
    public String userProfile(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Long userid = getCurrentUser();
        UserDB user = userService.findById(id);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
            return "redirect:/errorPage";
        }
        if (!userid.equals(id)) {
            redirectAttributes.addFlashAttribute("error", "No tienes permiso para ver este perfil");
            return "redirect:/errorPage";
        }
        model.addAttribute("user", user);
        return "redirect:/users/user-profile";
    }

    @GetMapping("/users/edit/{id}")
    public String editUserProfile(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Long userid = getCurrentUser();
        UserDB user = userService.findById(id);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Debes haber iniciado sesión para editar tu perfil");
            return "redirect:/errorPage";
        }
        if (!userid.equals(id)) {
            redirectAttributes.addFlashAttribute("error", "No tienes permiso para editar este perfil");
            return "redirect:/errorPage";
        }
        model.addAttribute("user", user);
        return "redirect:/users/editProfile";
    }

    @PostMapping("/users/edit/{id}")
    public String updateUserProfile(@PathVariable Long id, RegisterUserDTO updatedUser, RedirectAttributes redirectAttributes) {
        Long user = getCurrentUser();
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Debes haber iniciado sesión para editar tu perfil");
            return "redirect:/errorPage";
        }
        if (!user.equals(id)) {
            redirectAttributes.addFlashAttribute("error", "No tienes permiso para editar este perfil");
            return "redirect:/errorPage";
        }

        if (updatedUser.name() == null || updatedUser.name().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El nombre no puede estar vacío");
            return "redirect:/errorPage";
        }
        if (updatedUser.email() == null || updatedUser.email().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El email no puede estar vacío");
            return "redirect:/errorPage";
        }
        if (userService.findByEmail(updatedUser.email()) && !updatedUser.email().equals(userService.findById(user).getEmail())) {
            redirectAttributes.addFlashAttribute("error", "El email ya está en uso");
            return "redirect:/errorPage";
        }
        UserDB newUser = userService.findById(user);
        if (newUser == null) {
            redirectAttributes.addFlashAttribute("error", "El usuario no existe.");
            return "redirect:/errorPage";
        }
        UserDB u = userService.updateUserProfile(newUser, updatedUser);
        return "redirect:/users/" + u.getId();
    }

}
