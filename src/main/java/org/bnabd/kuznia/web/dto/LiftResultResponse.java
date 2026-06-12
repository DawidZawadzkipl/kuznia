package org.bnabd.kuznia.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record LiftResultResponse(
		Long id,
		Long clientId,
		String liftType,
		String liftDisplayName,
		BigDecimal weightKg,
		int reps,
		BigDecimal estimatedOneRepMax,
		LocalDate resultDate,
		String notes,
		Instant createdAt
) {
}
