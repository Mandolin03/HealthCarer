package com.evaruiz.healthcarer.model.DTO;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record CreateTakeDTO(
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm")
    LocalDateTime date,
    Long[] medications
) {
    public boolean validate() {
        return date == null || !date.isBefore(LocalDateTime.now()) ||
                medications == null || medications.length <= 0;
    }
}
