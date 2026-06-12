package org.bnabd.kuznia.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.bnabd.kuznia.domain.LiftTypeName;

public record LiftResultRequest(
		@NotNull LiftTypeName liftType,
		@NotNull @Positive BigDecimal weightKg,
		@Positive int reps,
		@NotNull LocalDate resultDate,
		String notes
) {
}
