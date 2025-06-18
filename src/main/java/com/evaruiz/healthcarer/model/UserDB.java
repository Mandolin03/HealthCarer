package com.evaruiz.healthcarer.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDB {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String encodedPassword;

    @OneToMany
    @JsonManagedReference
    private Set<TreatmentDB> treatments = new HashSet<>();

    @OneToMany
    @JsonManagedReference
    private Set<MedicationDB> medications = new HashSet<>();

    @OneToMany
    @JsonManagedReference
    private Set<TakeDB> takes = new HashSet<>();

}
