package org.bnabd.kuznia.web.dto;

import java.time.Instant;

public record TrainingNoteResponse(
		Long id,
		Long reservationId,
		Long trainerId,
		Long clientId,
		String note,
		Instant createdAt
) {
}
