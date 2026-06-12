package org.bnabd.kuznia.web.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record ReservationRequest(
		@NotNull Long trainerId,
		@NotNull Long trainingTypeId,
		Long trainingStationId,
		@NotNull Instant startTime
) {
}
