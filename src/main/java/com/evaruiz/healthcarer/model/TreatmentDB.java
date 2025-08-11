package com.evaruiz.healthcarer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;


@Entity
@Table(name = "treatments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TreatmentDB {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private java.lang.Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDateTime startDate;

    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDateTime endDate;

    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDateTime lastTakenDate;

    @Column(nullable = false)
    private Float dispensingFrequency;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "treatment_medication",
            joinColumns = @JoinColumn(name = "treatment_id"),
            inverseJoinColumns = @JoinColumn(name = "medication_id")
    )
    @ToString.Exclude
    private List<MedicationDB> medications = new ArrayList<>();

    @ManyToOne
    @ToString.Exclude
    private UserDB user;

    public boolean checkIntakeDates() {
        LocalDateTime objectiveDate = lastTakenDate.plusHours(dispensingFrequency.longValue());
        LocalDateTime now = LocalDateTime.now();
        long difference = ChronoUnit.MINUTES.between(objectiveDate, now);
        return difference >= -10 && difference <= 0;

    }

}
