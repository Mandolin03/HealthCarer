package com.evaruiz.healthcarer.model.DTO;

import com.evaruiz.healthcarer.model.MedicationDB;

import java.util.List;

public record FormattedDateTreatment(
        Long id,
        String name,
        String startDate,
        String endDate,
        Float dispensingFrequency,
        List<MedicationDB> medications
) {
}
