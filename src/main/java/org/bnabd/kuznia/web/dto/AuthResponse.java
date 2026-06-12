package org.bnabd.kuznia.web.dto;

public record AuthResponse(
		String token,
		UserResponse user
) {
}
