package com.evaruiz.healthcarer.controller;

import com.evaruiz.healthcarer.model.DTO.CreateTreatmentDTO;
import com.evaruiz.healthcarer.model.DTO.FormattedDateTreatment;
import com.evaruiz.healthcarer.model.DTO.LoggedUser;
import com.evaruiz.healthcarer.model.DTO.MedicationDTO;
import com.evaruiz.healthcarer.model.MedicationDB;
import com.evaruiz.healthcarer.model.TreatmentDB;
import com.evaruiz.healthcarer.model.UserDB;
import com.evaruiz.healthcarer.service.MedicationService;
import com.evaruiz.healthcarer.service.TreatmentService;
import com.evaruiz.healthcarer.service.UserService;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/treatments")
public class TreatmentController {

    private final TreatmentService treatmentService;
    private final MedicationService medicationService;
    private final UserService userService;

    private static java.lang.Long getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        LoggedUser current = (LoggedUser) authentication.getPrincipal();
        return current.getId();
    }

    @GetMapping("/")
    public String getTreatments(Model model, RedirectAttributes redirectAttributes) {
        Long currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Debes haber iniciado sesión para ver tu historial de tomas.");
            return "redirect:/errorPage";
        }
        List<TreatmentDB> treatments = treatmentService.findTreatmentsByUserId(currentUser);
        List<FormattedDateTreatment> formattedTreatments = new ArrayList<>();
        for (TreatmentDB treatment : treatments) {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            String formattedStartDate = treatment.getStartDate().format(dateFormatter);
            String formattedEndTime = treatment.getEndDate().format(dateFormatter);
            List<MedicationDB> medications = treatment.getMedications();
            medications.sort(Comparator.comparing(MedicationDB::getName));
            formattedTreatments.add(new FormattedDateTreatment(
                    treatment.getId(),
                    treatment.getName(),
                    formattedStartDate,
                    formattedEndTime,
                    treatment.getDispensingFrequency(),
                    medications
            ));
        }
        model.addAttribute("treatments", formattedTreatments);
        return "redirect:/treatments/treatments";
    }

    @GetMapping("/new")
    public String newTreatment(Model model, RedirectAttributes redirectAttributes) {
        Long currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Debes haber iniciado sesión para crear un nuevo tratamiento.");
            return "redirect:/errorPage";
        }
        List<MedicationDB> medications = medicationService.findMedicationsByUserId(currentUser);
        model.addAttribute("medications", medications);
        return "redirect:/treatments/createTreatment";
    }

    @GetMapping("/{id}")
    public String getTreatmentDetails(@PathVariable java.lang.Long id, Model model, RedirectAttributes redirectAttributes) {
        Long currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Debes haber iniciado sesión para ver los detalles del tratamiento.");
            return "redirect:/errorPage";
        }
        Optional<TreatmentDB> treatmentOptional = treatmentService.findById(id);
        if (treatmentOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El tratamiento no existe o no está disponible.");
            return "redirect:/errorPage";
        }
        TreatmentDB treatment = treatmentOptional.get();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String formattedStartDate = treatment.getStartDate().format(dateFormatter);
        String formattedEndDate = treatment.getEndDate().format(dateFormatter);
        List<MedicationDB> medications = treatment.getMedications();
        medications.sort(Comparator.comparing(MedicationDB::getName));
        model.addAttribute("treatment", new FormattedDateTreatment(
                treatment.getId(),
                treatment.getName(),
                formattedStartDate,
                formattedEndDate,
                treatment.getDispensingFrequency(),
                medications
        ));
        return "redirect:/treatments/treatment";
    }
    @GetMapping("/edit/{id}")
    public String editTreatment(@PathVariable java.lang.Long id, Model model, RedirectAttributes redirectAttributes) {
        Long currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Debes haber iniciado sesión para editar un tratamiento.");
            return "redirect:/errorPage";
        }
        Optional<TreatmentDB> treatmentOptional = treatmentService.findById(id);
        if (treatmentOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El tratamiento no existe o no está disponible.");
            return "redirect:/errorPage";
        }
        TreatmentDB treatment = treatmentOptional.get();
        List<MedicationDB> treatmentMedications = treatment.getMedications();
        List<MedicationDB> medications = medicationService.findMedicationsByUserId(currentUser);
        List<MedicationDTO> medicationDTOs = new ArrayList<>();
        for (MedicationDB med : medications) {
            boolean isSelected = treatmentMedications.contains(med);
            medicationDTOs.add(new MedicationDTO(med.getId(), med.getName(), isSelected));
        }
        model.addAttribute("medicationDTOs", medicationDTOs);
        model.addAttribute("treatment", treatment);
        return "redirect:/treatments/editTreatment";
    }

    @PostMapping("/save")
    public String createTreatment(CreateTreatmentDTO treatment, RedirectAttributes redirectAttributes) {
        Long currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Debes haber iniciado sesión para crear un nuevo tratamiento.");
            return "redirect:/errorPage";
        }
        if (!treatment.validate()) {
            redirectAttributes.addFlashAttribute("error", "Todos los campos son obligatorios.");
            return "redirect:/errorPage";
        }
        if (treatment.endDate().isBefore(treatment.startDate())) {
            redirectAttributes.addFlashAttribute("error", "La fecha de finalización no puede ser anterior a la fecha de inicio.");
            return "redirect:/errorPage";
        }
        List<MedicationDB> medications = new ArrayList<>();
        for (java.lang.Long medicationId : treatment.medicationIds()) {
            Optional<MedicationDB> medicationOptional = medicationService.findById(medicationId);
            if (medicationOptional.isPresent()) {
                medications.add(medicationOptional.get());
            } else {
                redirectAttributes.addFlashAttribute("error", "Una o más medicaciones no existen.");
                return "redirect:/errorPage";
            }
        }
        TreatmentDB newTreatment = new TreatmentDB();
        newTreatment.setName(treatment.name());
        newTreatment.setStartDate(treatment.startDate());
        newTreatment.setEndDate(treatment.endDate());
        newTreatment.setLastTakenDate(treatment.startDate());
        newTreatment.setDispensingFrequency(treatment.dispensingFrequency());
        newTreatment.setMedications(medications);
        UserDB newUser = userService.findById(currentUser);
        if (newUser == null) {
            redirectAttributes.addFlashAttribute("error", "El usuario no existe.");
            return "redirect:/errorPage";
        }
        newTreatment.setUser(newUser);
        TreatmentDB t = treatmentService.save(newTreatment);
        return "redirect:/treatments/" + t.getId();
    }

    @PostMapping("/update/{id}")
    public String updateTreatment(@PathVariable java.lang.Long id, CreateTreatmentDTO treatment, RedirectAttributes redirectAttributes) {
        Long currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Debes haber iniciado sesión para editar un tratamiento.");
            return "redirect:/errorPage";
        }
        Optional<TreatmentDB> treatmentOptional = treatmentService.findById(id);
        if (treatmentOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El tratamiento no existe o no está disponible.");
            return "redirect:/treatments/";
        }
        TreatmentDB existingTreatment = treatmentOptional.get();
        if (!treatment.validate()) {
            redirectAttributes.addFlashAttribute("error", "Todos los campos son obligatorios.");
            return "redirect:/errorPage";
        }
        if (treatment.endDate().isBefore(treatment.startDate())) {
            redirectAttributes.addFlashAttribute("error", "La fecha de finalización no puede ser anterior a la fecha de inicio.");
            return "redirect:/errorPage";
        }
        List<MedicationDB> medications = new ArrayList<>();
        for (java.lang.Long medicationId : treatment.medicationIds()) {
            Optional<MedicationDB> medicationOptional = medicationService.findById(medicationId);
            if (medicationOptional.isPresent()) {
                medications.add(medicationOptional.get());
            } else {
                redirectAttributes.addFlashAttribute("error", "Una o más medicaciones no existen.");
                return "redirect:/errorPage";
            }
        }
        existingTreatment.setName(treatment.name());
        existingTreatment.setStartDate(treatment.startDate());
        existingTreatment.setEndDate(treatment.endDate());
        existingTreatment.setDispensingFrequency(treatment.dispensingFrequency());
        existingTreatment.setMedications(medications);
        treatmentService.save(existingTreatment);
        return "redirect:/treatments/" + id;
    }

    @PostMapping("/delete/{id}")
    public String deleteTreatment(@PathVariable java.lang.Long id, RedirectAttributes redirectAttributes) {
        Long currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Debes haber iniciado sesión para eliminar un tratamiento.");
            return "redirect:/errorPage";
        }
        Optional<TreatmentDB> treatmentOptional = treatmentService.findById(id);
        if (treatmentOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El tratamiento no existe o no está disponible.");
            return "redirect:/treatments/";
        }
        treatmentService.deleteById(id);
        return "redirect:/treatments/";
    }
}
