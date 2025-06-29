package com.evaruiz.healthcarer.controller;


import com.evaruiz.healthcarer.model.DTO.CreateMedicationDTO;
import com.evaruiz.healthcarer.model.DTO.LoggedUser;
import com.evaruiz.healthcarer.model.MedicationDB;
import com.evaruiz.healthcarer.model.UserDB;
import com.evaruiz.healthcarer.service.ImageService;
import com.evaruiz.healthcarer.service.MedicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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


    private static UserDB getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        LoggedUser current = (LoggedUser) authentication.getPrincipal();
        return current.getUser();
    }

    @GetMapping("/")
    public String listMedications(Model model, RedirectAttributes redirectAttributes) {
        UserDB currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Debes haber iniciado sesión para ver tus medicamentos.");
            return "redirect:/errorPage";
        }
        List<MedicationDB> medications = medicationService.findMedicationsByUser(currentUser);
        model.addAttribute("medications", medications);
        return "/medications/medications";
    }

    @GetMapping("/{id}")
    public String showMedicationDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        UserDB currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Debes haber iniciado sesión para ver tu medicación.");
            return "redirect:/errorPage";
        }
        Optional<MedicationDB> medicationOptional = medicationService.findById(id);
        if (medicationOptional.isPresent()) {
            model.addAttribute("medication", medicationOptional.get());
            return "/medications/medication";
        } else {
            redirectAttributes.addFlashAttribute("error", "La medicación que busca no se ha encontrado o no existe.");
            return "redirect:/errorPage";
        }
    }

    @GetMapping("/image/{imageName}")
    public ResponseEntity<FileSystemResource> serveMedicationImage(@PathVariable String imageName) {
        FileSystemResource image = imageService.getImageFile(imageName);
        if (image != null && image.exists()) {
            return ResponseEntity.ok()
                    .header("Content-Type", "image/jpeg")
                    .body(image);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/new")
    public String showCreationForm(RedirectAttributes redirectAttributes) {
        UserDB currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Debes haber iniciado sesión para crear una medicación.");
            return "redirect:/errorPage";
        }
        return "/medications/createMedication";
    }

    @PostMapping("/save")
    public String saveMedication(@ModelAttribute CreateMedicationDTO medication,
                                 @RequestParam("imageFile") MultipartFile imageFile,
                                 RedirectAttributes redirectAttributes) {
        UserDB currentUser = getCurrentUser();
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
            newMedication.setUser(currentUser);

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
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        UserDB currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Debes haber iniciado sesión para editar tu medicación.");
            return "redirect:/errorPage";
        }
        Optional<MedicationDB> medicationOptional = medicationService.findById(id);
        if (medicationOptional.isPresent()) {
            MedicationDB medication = medicationOptional.get();
            if (!medication.getUser().getId().equals(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("error", "No estás autorizado para editar esta medicación.");
                return "redirect:/errorPage";
            }
            model.addAttribute("medication", medication);
            return "/medications/editMedication";
        } else {
            redirectAttributes.addFlashAttribute("error", "Medication not found for editing.");
            return "redirect:/errorPage";
        }
    }



    @PostMapping("/update/{id}")
    public String updateMedication(@ModelAttribute MedicationDB medication,
                                   @PathVariable Long id,
                                   @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                   @RequestParam(value = "deleteExistingImage", defaultValue = "false") boolean deleteExistingImage,
                                   RedirectAttributes redirectAttributes) {

        UserDB currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Debes haber iniciado sesión para editar tu medicación.");
            return "redirect:/errorPage";
        }
        Optional<MedicationDB> optionalMedication = medicationService.findById(id);
        if (optionalMedication.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "La medicación que intenta editar no existe.");
            return "redirect:/errorPage";
        }
        MedicationDB existingMedication = optionalMedication.get();
        existingMedication.setName(medication.getName());
        existingMedication.setStock(medication.getStock());
        existingMedication.setInstructions(medication.getInstructions());
        existingMedication.setDose(medication.getDose());
        if (deleteExistingImage) {
            if (imageFile == null || imageFile.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "La imagen es obligatoria.");
            }
            try {
                if (existingMedication.getImagePath() != null && !existingMedication.getImagePath().isEmpty()) {
                    imageService.deleteImageFile(existingMedication.getImagePath());
                }
                try {
                    if (imageFile != null && !imageFile.isEmpty()) {
                        String imagePath = imageService.uploadImage(imageFile);
                        existingMedication.setImagePath(imagePath);
                    } else{
                        redirectAttributes.addFlashAttribute("error", "La imagen es obligatoria.");
                    }
                } catch (IOException e) {
                    redirectAttributes.addFlashAttribute("error", "Error al subir la imagen: " + e.getMessage());
                    return "redirect:/errorPage";
                }
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Error al eliminar la imagen: " + e.getMessage());
                return "redirect:/errorPage";
            }
        }
        medicationService.saveMedication(existingMedication);
        return "redirect:/medications/{id}";
    }

    @PostMapping("/delete/{id}")
    public String deleteMedication(@PathVariable Long id,
                                   RedirectAttributes redirectAttributes) {
        UserDB currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Debes haber iniciado sesión para borrar tu medicación.");
            return "redirect:/errorPage";
        }

        Optional<MedicationDB> medicationOptional = medicationService.findById(id);
        if (medicationOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "La medicación que intenta borrar no existe.");
            return "redirect:/errorPage";
        }
        MedicationDB medicationToDelete = medicationOptional.get();
        if (!medicationToDelete.getUser().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("error", "No estás autorizado para borrar esta medicación.");
            return "redirect:/errorPage";
        }
        try {
            if (medicationToDelete.getImagePath() != null && !medicationToDelete.getImagePath().isEmpty()) {
                imageService.deleteImageFile(medicationToDelete.getImagePath());
            }
            medicationService.deleteMedication(id);
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Ha habido un error al borrar la imagen: " + e.getMessage());
            return "redirect:/errorPage";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ha habido un error inesperado al borrar la imagen " + e.getMessage());
            return "redirect:/errorPage";
        }
        return "redirect:/medications/";
    }


}
