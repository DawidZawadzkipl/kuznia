package org.bnabd.kuznia.web.dto;

import java.time.LocalDate;

public record CertificateResponse(
		Long id,
		Long trainerId,
		String name,
		String issuingOrganization,
		LocalDate issueDate,
		LocalDate expirationDate,
		String certificateNumber
) {
}
