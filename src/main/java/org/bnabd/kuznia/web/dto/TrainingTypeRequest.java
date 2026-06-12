package org.bnabd.kuznia.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record TrainingTypeRequest(
		@NotBlank String name,
		String description,
		@Positive Integer durationMinutes,
		@NotNull @Positive BigDecimal price,
		Boolean active
) {
}
