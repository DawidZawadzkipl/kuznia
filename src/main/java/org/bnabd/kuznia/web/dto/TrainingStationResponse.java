package org.bnabd.kuznia.web.dto;

public record TrainingStationResponse(
		Long id,
		String name,
		String description,
		boolean active
) {
}
