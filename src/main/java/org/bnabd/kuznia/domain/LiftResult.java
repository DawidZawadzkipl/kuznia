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
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lift_results")
@Getter
@Setter
@NoArgsConstructor
public class LiftResult {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "client_user_id", nullable = false)
	private User client;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "lift_type_id", nullable = false)
	private LiftType liftType;

	@Column(name = "weight_kg", nullable = false, precision = 6, scale = 2)
	private BigDecimal weightKg;

	@Column(nullable = false)
	private int reps;

	@Column(name = "estimated_one_rep_max", precision = 6, scale = 2)
	private BigDecimal estimatedOneRepMax;

	@Column(name = "result_date", nullable = false)
	private LocalDate resultDate;

	@Column(columnDefinition = "text")
	private String notes;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt = Instant.now();
}
