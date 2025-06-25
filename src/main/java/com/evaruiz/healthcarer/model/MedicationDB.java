package com.evaruiz.healthcarer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "medications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicationDB {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Float stock;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String instructions;

    @Column(nullable = false)
    private Float dose;

    private String imagePath;

    @ManyToMany
    private Set<TreatmentDB> treatments = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserDB user;

    @ManyToMany
    private Set<TakeDB> takes = new HashSet<>();

}
