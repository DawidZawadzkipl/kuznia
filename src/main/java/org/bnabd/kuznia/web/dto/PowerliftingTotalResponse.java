package org.bnabd.kuznia.web.dto;

import java.math.BigDecimal;

public record PowerliftingTotalResponse(
		BigDecimal squat,
		BigDecimal benchPress,
		BigDecimal deadlift,
		BigDecimal total
) {
}
