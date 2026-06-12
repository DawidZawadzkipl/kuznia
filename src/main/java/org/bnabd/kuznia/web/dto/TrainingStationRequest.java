package org.bnabd.kuznia.web.dto;

import jakarta.validation.constraints.NotBlank;

public record TrainingStationRequest(
		@NotBlank String name,
		String description,
		Boolean active
) {
}
