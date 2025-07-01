package com.evaruiz.healthcarer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private Date startDate;

    private Date endDate;

    @Column(nullable = false)
    private int dispensingFrequency;

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
