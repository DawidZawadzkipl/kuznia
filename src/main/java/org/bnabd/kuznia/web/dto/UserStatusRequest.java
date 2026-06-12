package org.bnabd.kuznia.web.dto;

import jakarta.validation.constraints.NotNull;

public record UserStatusRequest(
		@NotNull Boolean active
) {
}
