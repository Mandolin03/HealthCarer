package com.evaruiz.healthcarer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.*;


@Entity
@Table(name = "treatments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TreatmentDB {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDateTime startDate;

    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDateTime endDate;

    @Column(nullable = false)
    private Float dispensingFrequency;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "treatment_medication",
            joinColumns = @JoinColumn(name = "treatment_id"),
            inverseJoinColumns = @JoinColumn(name = "medication_id")
    )
    private List<MedicationDB> medications = new ArrayList<>();

    @ManyToOne
    private UserDB user;

}
