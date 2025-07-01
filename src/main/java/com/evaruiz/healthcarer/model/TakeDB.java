package com.evaruiz.healthcarer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm")
    LocalDateTime date;

    @ManyToMany(fetch = FetchType.EAGER)
    private List<MedicationDB> medications;

    @ManyToOne
    private UserDB user;

}
