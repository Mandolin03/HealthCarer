package com.evaruiz.healthcarer.model.DTO;

public record CreateMedicationDTO(
    String name,
    Float stock,
    String instructions,
    Float dose
) {
    public boolean validate() {
        return name != null && stock != null && dose != null && instructions != null
                && !name.isBlank() && stock > 0 && !instructions.isBlank() && dose > 0;
    }
}
