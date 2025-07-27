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
    private java.lang.Long id;

    @Column(nullable = false)
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm")
    LocalDateTime date;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "take_medication",
            joinColumns = @JoinColumn(name = "take_id"),
            inverseJoinColumns = @JoinColumn(name = "medication_id")
    )
    private List<MedicationDB> medications = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    private UserDB user;

}
