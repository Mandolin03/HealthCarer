package com.evaruiz.healthcarer.model.DTO;

public record MedicationDTO(
    Long id,
    String name,
    boolean selected
) {
}
