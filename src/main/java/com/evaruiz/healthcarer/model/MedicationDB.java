package com.evaruiz.healthcarer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private java.lang.Long id;

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
    @JsonIgnore
    private List<TreatmentDB> treatments = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnore
    private UserDB user;

    @ManyToMany(mappedBy = "medications", fetch = FetchType.EAGER)
    @JsonIgnore
    private List<TakeDB> takes = new ArrayList<>();


}
