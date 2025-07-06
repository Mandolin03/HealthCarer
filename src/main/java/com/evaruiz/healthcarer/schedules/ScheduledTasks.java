package com.evaruiz.healthcarer.schedules;
import com.evaruiz.healthcarer.model.MedicationDB;
import com.evaruiz.healthcarer.model.TreatmentDB;

import com.evaruiz.healthcarer.repository.TreatmentRepository;
import com.evaruiz.healthcarer.service.EmailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Component
public class ScheduledTasks {


    @Autowired
    private TreatmentRepository treatmentRepository;
    @Autowired
    private EmailServiceImpl emailService;

    @Scheduled(fixedRate = 5000)
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
                treatment.setLastTakenDate(LocalDateTime.now());
                treatmentRepository.save(treatment);
            }
        }
    }

}
