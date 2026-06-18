package org.bnabd.kuznia.web.dto;

import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.Set;

public record TrainerProfileRequest(
		String firstName,
		String lastName,
		String phone,
		String bio,
		String photoUrl,
		Integer experienceYears,
		BigDecimal hourlyRate,
		@NotEmpty Set<Long> specializationIds
) {
}
