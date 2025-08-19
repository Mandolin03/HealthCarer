package com.evaruiz.healthcarer.schedules;
import com.evaruiz.healthcarer.model.MedicationDB;
import com.evaruiz.healthcarer.model.TakeDB;
import com.evaruiz.healthcarer.model.TreatmentDB;

import com.evaruiz.healthcarer.repository.TakeRepository;
import com.evaruiz.healthcarer.repository.TreatmentRepository;
import com.evaruiz.healthcarer.repository.UserRepository;
import com.evaruiz.healthcarer.service.EmailServiceImpl;
import com.evaruiz.healthcarer.service.MedicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Component
public class ScheduledTasks {


    @Autowired
    private TreatmentRepository treatmentRepository;

    @Autowired
    private TakeRepository takeRepository;

    @Autowired
    private MedicationService medicationService;

    @Autowired
    private EmailServiceImpl emailService;

    @Scheduled(fixedRate = 18000) // 5 minutes
    @Transactional
    public void checkIntakeDates() {
        List<TreatmentDB> treatments = treatmentRepository.findAll();
        for (TreatmentDB treatment : treatments) {
            boolean sendMail = treatment.checkIntakeDates();
            if (sendMail) {
                String subject = "Hora de tomar la medicación";
                StringBuilder text = new StringBuilder("Debes tomar la siguiente medicacion: ");
                for (MedicationDB medication : treatment.getMedications()) {
                    text.append(medication.getName()).append(" ");
                    text.append(medication.getDose()).append("mg ");
                }
                emailService.sendSimpleMessage(treatment.getUser().getEmail(), subject, text.toString());

                for (MedicationDB medication : treatment.getMedications()) {
                    try {
                        medicationService.discountMedicationStock(medication.getId());

                    } catch (IllegalStateException e) {
                        String errorSubject = "Error al tomar la medicación";
                        String errorText = "No hay suficiente stock para la medicación: " + medication.getName();
                        emailService.sendSimpleMessage(treatment.getUser().getEmail(), errorSubject, errorText);
                    }
                }

                TakeDB take = new TakeDB();

                take.setMedications(new ArrayList<>(treatment.getMedications()));
                take.setDate(LocalDateTime.now());
                take.setUser(treatment.getUser());
                takeRepository.save(take);


                treatment.setLastTakenDate(LocalDateTime.now());
                treatmentRepository.save(treatment);
            }
        }
    }

    @Scheduled(fixedRate = 43200) // 12 hours
    @Transactional
    public void checkMedicationStock() {
        List<TreatmentDB> treatments = treatmentRepository.findAll();
        for (TreatmentDB treatment : treatments) {
            for (MedicationDB medication : treatment.getMedications()) {
                if (medication.getStock() <= 5) {
                    String subject = "Stock de medicación bajo";
                    String text = "La medicación " + medication.getName() + " está por debajo del nivel mínimo de stock. Por favor, reabastece lo antes posible.";
                    emailService.sendSimpleMessage(treatment.getUser().getEmail(), subject, text);
                }
            }
        }
    }

}
