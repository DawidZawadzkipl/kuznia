package org.bnabd.kuznia.web.dto;

import java.math.BigDecimal;

public record TrainingTypeResponse(
		Long id,
		String name,
		String description,
		int durationMinutes,
		BigDecimal price,
		boolean active
) {
}
