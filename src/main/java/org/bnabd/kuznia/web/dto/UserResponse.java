package org.bnabd.kuznia.web.dto;

import java.time.Instant;

public record UserResponse(
		Long id,
		String email,
		String firstName,
		String lastName,
		String phone,
		String role,
		boolean active,
		Instant createdAt
) {
}
