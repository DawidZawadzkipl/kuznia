package org.bnabd.kuznia.web.dto;

import java.math.BigDecimal;
import java.util.List;

public record TrainerResponse(
		Long id,
		Long userId,
		String email,
		String firstName,
		String lastName,
		String phone,
		boolean active,
		String bio,
		Integer experienceYears,
		BigDecimal hourlyRate,
		List<SpecializationResponse> specializations
) {
}
