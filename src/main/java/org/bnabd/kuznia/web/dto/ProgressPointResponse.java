package org.bnabd.kuznia.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProgressPointResponse(
		LocalDate date,
		String liftType,
		BigDecimal estimatedOneRepMax,
		BigDecimal weightKg,
		int reps
) {
}
