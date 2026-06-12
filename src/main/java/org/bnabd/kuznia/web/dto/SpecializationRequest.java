package org.bnabd.kuznia.web.dto;

import jakarta.validation.constraints.NotBlank;

public record SpecializationRequest(
		@NotBlank String name,
		String description
) {
}
