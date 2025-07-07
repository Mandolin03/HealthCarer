package com.evaruiz.healthcarer.model.DTO;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record CreateTreatmentDTO(
        String name,
        @DateTimeFormat(pattern = "dd-MM-yyyy")
        LocalDateTime startDate,
        @DateTimeFormat(pattern = "dd-MM-yyyy")
        LocalDateTime endDate,
        Float dispensingFrequency,
        Long[] medicationIds
) {
        public boolean validate() {
            return name != null && !name.isBlank()
                    && startDate != null
                    && endDate != null
                    && dispensingFrequency != null
                    && dispensingFrequency > 0
                    && medicationIds != null && medicationIds.length > 0;
        }
}
