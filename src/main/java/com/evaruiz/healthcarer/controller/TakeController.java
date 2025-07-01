package com.evaruiz.healthcarer.controller;

import com.evaruiz.healthcarer.model.DTO.CreateTakeDTO;
import com.evaruiz.healthcarer.model.DTO.FormattedDateTake;
import com.evaruiz.healthcarer.model.DTO.TakeMedicationDTO;
import com.evaruiz.healthcarer.model.DTO.LoggedUser;
import com.evaruiz.healthcarer.model.MedicationDB;
import com.evaruiz.healthcarer.model.TakeDB;
import com.evaruiz.healthcarer.model.UserDB;
import com.evaruiz.healthcarer.service.MedicationService;
import com.evaruiz.healthcarer.service.TakeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/takes")
public class TakeController {

    private final TakeService takeService;
    private final MedicationService medicationService;

    private static UserDB getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        LoggedUser current = (LoggedUser) authentication.getPrincipal();
        return current.getUser();
    }

    private boolean setDateAndMedications(CreateTakeDTO take, RedirectAttributes redirectAttributes, TakeDB newTake) {
        newTake.setDate(take.date());
        List<MedicationDB> medications = new ArrayList<>();
        for (Long medicationId : take.medications()) {
            Optional<MedicationDB> medicationOptional = medicationService.findById(medicationId);
            if (medicationOptional.isPresent()) {
                medications.add(medicationOptional.get());
            } else {
                redirectAttributes.addFlashAttribute("error", "Una o más medicaciones no existen.");
                return true;
            }
        }
        newTake.setMedications(medications);
        return false;
    }

    @GetMapping("/")
    public String listTakes(Model model, RedirectAttributes redirectAttributes) {
        UserDB currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Debes haber iniciado sesión para ver tu historial de tomas.");
            return "redirect:/errorPage";
        }
        List<TakeDB> takes = takeService.findTakesByUser(currentUser);
        List<FormattedDateTake> formattedTakes = new ArrayList<>();
        for (TakeDB take : takes) {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            String formattedDate = take.getDate().format(dateFormatter);
            String formattedTime = take.getDate().format(timeFormatter);
            List<MedicationDB> medications = take.getMedications();
            medications.sort(Comparator.comparing(MedicationDB::getName));
            formattedTakes.add(new FormattedDateTake(take.getId(), formattedDate, formattedTime, medications));
        }
        formattedTakes.sort(Comparator.comparing(FormattedDateTake::date).reversed());
        model.addAttribute("takes", formattedTakes);
        return "/takes/takes";

    }

    @GetMapping("/{id}")
    public String showTakeDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        UserDB currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Debes haber iniciado sesión para ver tu toma.");
            return "redirect:/errorPage";
        }
        Optional<TakeDB> takeOptional = takeService.findById(id);
        if (takeOptional.isPresent()) {
            TakeDB take = takeOptional.get();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            String formattedDate = take.getDate().format(dateFormatter);
            String formattedTime = take.getDate().format(timeFormatter);
            List<MedicationDB> medications = take.getMedications();
            medications.sort(Comparator.comparing(MedicationDB::getName));
            model.addAttribute("take", new FormattedDateTake(take.getId(), formattedDate, formattedTime, medications));
            return "/takes/take";
        } else {
            redirectAttributes.addFlashAttribute("error", "La toma que busca no se ha encontrado o no existe.");
            return "redirect:/errorPage";
        }
    }

    @GetMapping("/new")
    public String showCreateForm(Model model, RedirectAttributes redirectAttributes) {
        UserDB currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Debes haber iniciado sesión para crear una toma.");
            return "redirect:/errorPage";
        }
        List<MedicationDB> medications = medicationService.findMedicationsByUser(currentUser);
        if (medications.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Debes tener al menos una medicación para crear una toma.");
            return "redirect:/errorPage";
        }
        model.addAttribute("medications", medications);
        return "/takes/createTake";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        UserDB currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Debes haber iniciado sesión para editar una toma.");
            return "redirect:/errorPage";
        }
        Optional<TakeDB> takeOptional = takeService.findById(id);
        if (takeOptional.isPresent()) {
            TakeDB take = takeOptional.get();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String formattedDate = take.getDate().format(formatter);
            List<MedicationDB> medications = medicationService.findMedicationsByUser(currentUser);
            List<TakeMedicationDTO> medicationDTOs = new ArrayList<>();
            for (MedicationDB med : medications) {
                boolean isSelected = take.getMedications().contains(med);
                medicationDTOs.add(new TakeMedicationDTO(med.getId(), med.getName(), isSelected));
            }
            model.addAttribute("medicationDTOs", medicationDTOs);
            model.addAttribute("take", take);
            model.addAttribute("formattedTakeDate", formattedDate);
            return "/takes/editTake";
        } else {
            redirectAttributes.addFlashAttribute("error", "La toma que busca no se ha encontrado o no existe.");
            return "redirect:/errorPage";
        }
    }

    @PostMapping("/save")
    public String saveTake(CreateTakeDTO take, RedirectAttributes redirectAttributes) {
        UserDB currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Debes haber iniciado sesión para guardar una toma.");
            return "redirect:/errorPage";
        }
        if(take.validate()){
            redirectAttributes.addFlashAttribute("error", "Todos los campos son obligatorios.");
            return "redirect:/errorPage";
        }
        TakeDB newTake = new TakeDB();
        if (setDateAndMedications(take, redirectAttributes, newTake)) return "redirect:/errorPage";
        newTake.setUser(currentUser);
        takeService.save(newTake);
        return "redirect:/takes/";
    }



    @PostMapping("/edit/{id}")
    public String editTake(@PathVariable Long id, CreateTakeDTO take, RedirectAttributes redirectAttributes) {
        UserDB currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Debes haber iniciado sesión para editar una toma.");
            return "redirect:/errorPage";
        }
        if(take.validate()){
            redirectAttributes.addFlashAttribute("error", "Todos los campos son obligatorios.");
            return "redirect:/errorPage";
        }
        Optional<TakeDB> takeOptional = takeService.findById(id);
        if (takeOptional.isPresent()) {
            TakeDB existingTake = takeOptional.get();
            if (!existingTake.getUser().getId().equals(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para editar esta toma.");
                return "redirect:/errorPage";
            }
            if (setDateAndMedications(take, redirectAttributes, existingTake)) return "redirect:/errorPage";
            takeService.save(existingTake);
        } else {
            redirectAttributes.addFlashAttribute("error", "La toma que intenta editar no existe.");
            return "redirect:/errorPage";
        }
        return "redirect:/takes/";
    }

    @PostMapping("/delete/{id}")
    public String deleteTake(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        UserDB currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Debes haber iniciado sesión para eliminar una toma.");
            return "redirect:/errorPage";
        }
        Optional<TakeDB> takeOptional = takeService.findById(id);
        if (takeOptional.isPresent()) {
            TakeDB take = takeOptional.get();
            if (take.getUser().getId().equals(currentUser.getId())) {
                takeService.deleteById(take.getId());
            } else {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para eliminar esta toma.");
                return "redirect:/errorPage";
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "La toma que intenta eliminar no existe.");
            return "redirect:/errorPage";
        }
        return "redirect:/takes/";
    }

}
