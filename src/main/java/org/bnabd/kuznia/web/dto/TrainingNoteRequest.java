package org.bnabd.kuznia.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TrainingNoteRequest(
		@NotNull Long reservationId,
		@NotBlank String note
) {
}
