package org.bnabd.kuznia.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
		name = "reservations",
		indexes = @Index(name = "idx_reservations_trainer_time", columnList = "trainer_id,start_time,end_time")
)
@Getter
@Setter
@NoArgsConstructor
public class Reservation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "client_user_id", nullable = false)
	private User client;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "trainer_id", nullable = false)
	private TrainerProfile trainerProfile;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "training_type_id", nullable = false)
	private TrainingType trainingType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "training_station_id")
	private TrainingStation trainingStation;

	@Column(name = "start_time", nullable = false)
	private Instant startTime;

	@Column(name = "end_time", nullable = false)
	private Instant endTime;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private ReservationStatus status = ReservationStatus.PENDING;

	@Column(name = "cancellation_reason", columnDefinition = "text")
	private String cancellationReason;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt = Instant.now();
}
