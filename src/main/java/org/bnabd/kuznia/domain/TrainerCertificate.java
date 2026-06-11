package org.bnabd.kuznia.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "trainer_certificates")
@Getter
@Setter
@NoArgsConstructor
public class TrainerCertificate {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "trainer_id", nullable = false)
	private TrainerProfile trainerProfile;

	@Column(nullable = false, length = 150)
	private String name;

	@Column(name = "issuing_organization", nullable = false, length = 150)
	private String issuingOrganization;

	@Column(name = "issue_date", nullable = false)
	private LocalDate issueDate;

	@Column(name = "expiration_date")
	private LocalDate expirationDate;

	@Column(name = "certificate_number", length = 100)
	private String certificateNumber;
}
