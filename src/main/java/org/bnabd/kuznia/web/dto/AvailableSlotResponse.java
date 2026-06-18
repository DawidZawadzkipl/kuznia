package org.bnabd.kuznia.web.dto;

import java.time.Instant;

public record AvailableSlotResponse(
		Instant startTime,
		Instant endTime
) {
}
