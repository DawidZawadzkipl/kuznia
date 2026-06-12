package org.bnabd.kuznia.web.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record AvailabilityRequest(
		@NotNull Instant startTime,
		@NotNull Instant endTime,
		Boolean available
) {
}
