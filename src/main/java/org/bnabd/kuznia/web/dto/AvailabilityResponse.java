package org.bnabd.kuznia.web.dto;

import java.time.Instant;

public record AvailabilityResponse(
		Long id,
		Long trainerId,
		Instant startTime,
		Instant endTime,
		boolean available
) {
}
