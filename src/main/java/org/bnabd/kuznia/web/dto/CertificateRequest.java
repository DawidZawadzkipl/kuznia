package org.bnabd.kuznia.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CertificateRequest(
		@NotNull Long trainerId,
		@NotBlank String name,
		@NotBlank String issuingOrganization,
		@NotNull LocalDate issueDate,
		LocalDate expirationDate,
		String certificateNumber
) {
}
