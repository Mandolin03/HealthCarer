package com.evaruiz.healthcarer.controller;


import com.evaruiz.healthcarer.model.DTO.CreateMedicationDTO;
import com.evaruiz.healthcarer.model.DTO.LoggedUser;
import com.evaruiz.healthcarer.model.MedicationDB;
import com.evaruiz.healthcarer.model.UserDB;
import com.evaruiz.healthcarer.service.ImageService;
import com.evaruiz.healthcarer.service.MedicationService;
import com.evaruiz.healthcarer.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


@Controller
@RequiredArgsConstructor
@RequestMapping("/medications")
public class MedicationController {

    private final MedicationService medicationService;
    private final ImageService imageService;
    private final UserService userService;


    private static Long getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        LoggedUser current = (LoggedUser) authentication.getPrincipal();
        return current.getId();
    }

    @GetMapping("/")
    public String listMedications(Model model, RedirectAttributes redirectAttributes) {
        Long currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Debes haber iniciado sesión para ver tus medicamentos.");
            return "redirect:/errorPage";
        }
        List<MedicationDB> medications = medicationService.findMedicationsByUserId(currentUser);
        medications.sort(Comparator.comparing(MedicationDB::getName));
        model.addAttribute("medications", medications);
        return "medications/medication-list";
    }

    @GetMapping("/{id}")
    public String showMedicationDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Long currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Debes haber iniciado sesión para ver tu medicación.");
            return "redirect:/errorPage";
        }
        Optional<MedicationDB> medicationOptional = medicationService.findById(id);
        if (medicationOptional.isPresent()) {
            model.addAttribute("medication", medicationOptional.get());
            return "medications/medication";
        } else {
            redirectAttributes.addFlashAttribute("error", "La medicación que busca no se ha encontrado o no existe.");
            return "redirect:/errorPage";
        }
    }

    @GetMapping("/image/{imageName}")
    public ResponseEntity<InputStreamResource> serveMedicationImage(@PathVariable String imageName) {
        try {
            InputStream imageStream = imageService.getImageFile(imageName);
            if (imageStream != null) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(new InputStreamResource(imageStream));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/new")
    public String showCreationForm(RedirectAttributes redirectAttributes) {
        Long currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Debes haber iniciado sesión para crear una medicación.");
            return "redirect:/errorPage";
        }
        return "medications/createMedication";
    }

    @PostMapping("/save")
    public String saveMedication(@ModelAttribute CreateMedicationDTO medication,
                                 @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                 RedirectAttributes redirectAttributes) {
        MedicationDB savedMed;
        Long currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Debes haber iniciado sesión.");
            return "redirect:/errorPage";
        }
        try {
            if(!medication.validate()){
                redirectAttributes.addFlashAttribute("error", "Campos obligatorios vacíos.");
                return "redirect:/errorPage";
            }

            MedicationDB newMedication = new MedicationDB();
            newMedication.setName(medication.name());
            newMedication.setStock(medication.stock());
            newMedication.setInstructions(medication.instructions());
            newMedication.setDose(medication.dose());

            UserDB newUser = userService.findById(currentUser);
            newMedication.setUser(newUser);

            if (imageFile != null && !imageFile.isEmpty()) {

                String imagePath = imageService.uploadImage(imageFile);
                newMedication.setImagePath(imagePath);
            }

            savedMed = medicationService.saveMedication(newMedication);
            return "redirect:/medications/" + savedMed.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/errorPage";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable java.lang.Long id, Model model, RedirectAttributes redirectAttributes) {
        Long currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Debes haber iniciado sesión para editar tu medicación.");
            return "redirect:/errorPage";
        }
        Optional<MedicationDB> medicationOptional = medicationService.findById(id);
        if (medicationOptional.isPresent()) {
            MedicationDB medication = medicationOptional.get();
            if (!medication.getUser().getId().equals(currentUser)) {
                redirectAttributes.addFlashAttribute("error", "No estás autorizado para editar esta medicación.");
                return "redirect:/errorPage";
            }
            model.addAttribute("medication", medication);
            return "medications/editMedication";
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

        Long currentUser = getCurrentUser();
        Optional<MedicationDB> optionalMedication = medicationService.findById(id);

        if (optionalMedication.isEmpty()) return "redirect:/errorPage";

        MedicationDB existingMedication = optionalMedication.get();
        existingMedication.setName(medication.getName());
        existingMedication.setStock(medication.getStock());
        existingMedication.setInstructions(medication.getInstructions());
        existingMedication.setDose(medication.getDose());

        try {
            // Lógica de borrado/reemplazo en MinIO
            if (deleteExistingImage && existingMedication.getImagePath() != null) {
                imageService.deleteImageFile(existingMedication.getImagePath());
                existingMedication.setImagePath(null);
            }

            if (imageFile != null && !imageFile.isEmpty()) {
                // Si ya tenía imagen, borramos la vieja del bucket
                if (existingMedication.getImagePath() != null) {
                    imageService.deleteImageFile(existingMedication.getImagePath());
                }
                String imagePath = imageService.uploadImage(imageFile);
                existingMedication.setImagePath(imagePath);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error en MinIO: " + e.getMessage());
            return "redirect:/errorPage";
        }

        medicationService.saveMedication(existingMedication);
        return "redirect:/medications/" + id;
    }

    @PostMapping("/delete/{id}")
    public String deleteMedication(@PathVariable Long id,
                                   RedirectAttributes redirectAttributes) {
        Long currentUser = getCurrentUser();
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
        if (!medicationToDelete.getUser().getId().equals(currentUser)) {
            redirectAttributes.addFlashAttribute("error", "No estás autorizado para borrar esta medicación.");
            return "redirect:/errorPage";
        }
        medicationService.removeMedicationFromUser(id);
        return "redirect:/medications/";
    }
}
