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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
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
        return "/medications/medications";
    }

    @GetMapping("/{id}")
    public String showMedicationDetails(@PathVariable java.lang.Long id, Model model, RedirectAttributes redirectAttributes) {
        Long currentUser = getCurrentUser();
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
        Long currentUser = getCurrentUser();
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
        MedicationDB savedMed;
        Long currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Debes haber iniciado sesión para crear una medicación.");
            return "redirect:/errorPage";
        }
        try {
            if(!medication.validate()){
                redirectAttributes.addFlashAttribute("error", "Todos los campos son obligatorios.");
                return "redirect:/errorPage";
            }
            MedicationDB newMedication = new MedicationDB();
            newMedication.setName(medication.name());
            newMedication.setStock(medication.stock());
            newMedication.setInstructions(medication.instructions());
            newMedication.setDose(medication.dose());
            UserDB newUser = userService.findById(currentUser);
            if (newUser == null) {
                redirectAttributes.addFlashAttribute("error", "El usuario no existe.");
                return "redirect:/errorPage";
            }
            newMedication.setUser(newUser);

            if (imageFile != null && !imageFile.isEmpty()) {
                String imagePath = imageService.uploadImage(imageFile);
                newMedication.setImagePath(imagePath);
            } else{
                redirectAttributes.addFlashAttribute("error", "La imagen es obligatoria.");
            }
            savedMed = medicationService.saveMedication(newMedication);
            if (savedMed == null) {
                redirectAttributes.addFlashAttribute("error", "Error al guardar la medicación.");
                return "redirect:/errorPage";
            }

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error al subir la imagen: " + e.getMessage());
            return "redirect:/errorPage";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ocurrió un error inesperado al crear la medicación: " + e.getMessage());
            return "redirect:/errorPage";
        }
        return "redirect:/medications/" + savedMed.getId();
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
            return "/medications/editMedication";
        } else {
            redirectAttributes.addFlashAttribute("error", "Medication not found for editing.");
            return "redirect:/errorPage";
        }
    }

    @PostMapping("/update/{id}")
    public String updateMedication(@ModelAttribute MedicationDB medication,
                                   @PathVariable java.lang.Long id,
                                   @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                   @RequestParam(value = "deleteExistingImage", defaultValue = "false") boolean deleteExistingImage,
                                   RedirectAttributes redirectAttributes) {

        Long currentUser = getCurrentUser();
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
    public String deleteMedication(@PathVariable java.lang.Long id,
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
