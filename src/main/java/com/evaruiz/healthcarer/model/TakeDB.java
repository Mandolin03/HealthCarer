package com.evaruiz.healthcarer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "takes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TakeDB {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Date date;

    @ManyToMany
    private List<MedicationDB> medications = new ArrayList<>();

    @ManyToOne
    private UserDB user;

}
