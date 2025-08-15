package com.evaruiz.healthcarer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;


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

    @ManyToMany(mappedBy = "medications" , fetch = FetchType.EAGER)
    @ToString.Exclude
    private List<TreatmentDB> treatments = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @ToString.Exclude
    private UserDB user;

    @ManyToMany(mappedBy = "medications", fetch = FetchType.EAGER)
    @ToString.Exclude
    private List<TakeDB> takes = new ArrayList<>();
}
