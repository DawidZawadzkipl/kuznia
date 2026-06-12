package org.bnabd.kuznia.web.dto;

import java.time.Instant;

public record ReservationResponse(
		Long id,
		Long clientId,
		String clientName,
		Long trainerId,
		String trainerName,
		Long trainingTypeId,
		String trainingTypeName,
		Long trainingStationId,
		String trainingStationName,
		Instant startTime,
		Instant endTime,
		String status,
		String cancellationReason,
		Instant createdAt
) {
}
