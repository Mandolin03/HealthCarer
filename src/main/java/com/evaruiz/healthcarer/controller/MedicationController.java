package com.evaruiz.healthcarer.controller;


import com.evaruiz.healthcarer.model.DTO.CreateMedicationDTO;
import com.evaruiz.healthcarer.model.LoggedUser;
import com.evaruiz.healthcarer.model.MedicationDB;
import com.evaruiz.healthcarer.model.UserDB;
import com.evaruiz.healthcarer.service.ImageService;
import com.evaruiz.healthcarer.service.MedicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import java.util.List;
import java.util.Optional;


@Controller
@RequiredArgsConstructor
@RequestMapping("/medications")
public class MedicationController {

    private final MedicationService medicationService;
    private final ImageService imageService;


    @GetMapping("/")
    public String listMedications(@AuthenticationPrincipal LoggedUser currentUser, Model model) {
        List<MedicationDB> medications = medicationService.findMedicationsByUser(currentUser.getUser());
        model.addAttribute("medications", medications);
        return "/medications/medications";
    }

    @GetMapping("/{id}")
    public String showMedicationDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<MedicationDB> medicationOptional = medicationService.findById(id);
        if (medicationOptional.isPresent()) {
            model.addAttribute("medication", medicationOptional.get());
            return "/medications/medication";
        } else {
            redirectAttributes.addFlashAttribute("error", "La medicación que busca no se ha encontrado o no existe.");
            return "redirect:/errorPage";
        }
    }

    @GetMapping("/new")
    public String showCreationForm() {
        return "/medications/createMedication";
    }

    @PostMapping("/save")
    public String saveMedication(@ModelAttribute CreateMedicationDTO medication,
                                 @RequestParam("imageFile") MultipartFile imageFile,
                                 @AuthenticationPrincipal LoggedUser currentUser,
                                 RedirectAttributes redirectAttributes) {
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Debes haber iniciado sesión para crear una medicación.");
            return "redirect:/errorPage";
        }
        try {
            MedicationDB newMedication = new MedicationDB();
            newMedication.setName(medication.name());
            newMedication.setStock(medication.stock());
            newMedication.setInstructions(medication.instructions());
            newMedication.setDose(medication.dose());
            newMedication.setUser(currentUser.getUser());

            if (imageFile != null && !imageFile.isEmpty()) {
                String imagePath = imageService.uploadImage(imageFile);
                newMedication.setImagePath(imagePath);
            } else{
                redirectAttributes.addFlashAttribute("error", "La imagen es obligatoria.");
            }
            medicationService.saveMedication(newMedication);
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error al subir la imagen: " + e.getMessage());
            return "redirect:/errorPage";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ocurrió un error inesperado al crear la medicación: " + e.getMessage());
            return "redirect:/errorPage";
        }
        return "redirect:/medications/";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes,
                               @AuthenticationPrincipal UserDB currentUser) {
        Optional<MedicationDB> medicationOptional = medicationService.findById(id);
        if (medicationOptional.isPresent()) {
            MedicationDB medication = medicationOptional.get();
            if (!medication.getUser().getId().equals(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("error", "You are not authorized to edit this medication.");
                return "redirect:/medications/list";
            }
            model.addAttribute("medication", medication);
            return "/medications/editMedication";
        } else {
            redirectAttributes.addFlashAttribute("error", "Medication not found for editing.");
            return "redirect:/medications/medications";
        }
    }

    @PostMapping("/update")
    public String updateMedication(@ModelAttribute MedicationDB medication,
                                   @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                   @RequestParam(value = "deleteExistingImage", defaultValue = "false") boolean deleteExistingImage,
                                   @AuthenticationPrincipal LoggedUser currentUser,
                                   RedirectAttributes redirectAttributes) {

        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to update a medication.");
            return "redirect:/login";
        }
        return "";
    }

    @PostMapping("/delete/{id}")
    public String deleteMedication(@PathVariable Long id,
                                   @AuthenticationPrincipal LoggedUser currentUser,
                                   RedirectAttributes redirectAttributes) {

        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to delete a medication.");
            return "redirect:/login";
        }

        Optional<MedicationDB> medicationOptional = medicationService.findById(id);
        if (medicationOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Medication not found for deletion.");
            return "redirect:/medications/list";
        }
        MedicationDB medicationToDelete = medicationOptional.get();
        if (!medicationToDelete.getUser().getId().equals(currentUser.getUser().getId())) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to delete this medication.");
            return "redirect:/medications/list";
        }
        try {
            if (medicationToDelete.getImagePath() != null && !medicationToDelete.getImagePath().isEmpty()) {
                imageService.deleteImageFile(medicationToDelete.getImagePath());
            }
            medicationService.deleteMedication(id);
            redirectAttributes.addFlashAttribute("message", "Medication deleted successfully!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting image file: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred during deletion: " + e.getMessage());
        }
        return "redirect:/medications/list";
    }


}
