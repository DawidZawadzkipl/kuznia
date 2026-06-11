package org.bnabd.kuznia.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "trainer_profiles")
@Getter
@Setter
@NoArgsConstructor
public class TrainerProfile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private User user;

	@ManyToMany
	@JoinTable(
			name = "trainer_profile_specializations",
			joinColumns = @JoinColumn(name = "trainer_id"),
			inverseJoinColumns = @JoinColumn(name = "specialization_id"),
			uniqueConstraints = @UniqueConstraint(columnNames = {"trainer_id", "specialization_id"})
	)
	private Set<TrainerSpecialization> specializations = new HashSet<>();

	@Column(columnDefinition = "text")
	private String bio;

	@Column(name = "experience_years")
	private Integer experienceYears;

	@Column(name = "hourly_rate", precision = 10, scale = 2)
	private BigDecimal hourlyRate;
}
