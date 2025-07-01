package com.evaruiz.healthcarer.model.DTO;

public record TakeMedicationDTO(
    Long id,
    String name,
    boolean selected
) {
}
