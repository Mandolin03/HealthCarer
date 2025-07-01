package com.evaruiz.healthcarer.model.DTO;

import com.evaruiz.healthcarer.model.MedicationDB;

import java.util.List;

public record FormattedDateTake(
        Long id,
        String date,
        String time,
        List<MedicationDB> medications
) {
}
