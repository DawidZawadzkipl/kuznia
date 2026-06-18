package org.bnabd.kuznia.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Set;

public record TrainerRequest(
		@NotBlank @Email String email,
		@Size(min = 6) String password,
		@NotBlank String firstName,
		@NotBlank String lastName,
		String phone,
		String bio,
		String photoUrl,
		Integer experienceYears,
		BigDecimal hourlyRate,
		@NotEmpty Set<Long> specializationIds,
		Boolean active
) {
}
