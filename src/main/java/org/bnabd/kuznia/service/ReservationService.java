package org.bnabd.kuznia.service;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import lombok.RequiredArgsConstructor;
import org.bnabd.kuznia.domain.Reservation;
import org.bnabd.kuznia.domain.ReservationStatus;
import org.bnabd.kuznia.repository.ReservationRepository;
import org.bnabd.kuznia.repository.TrainerProfileRepository;
import org.bnabd.kuznia.repository.TrainingTypeRepository;
import org.bnabd.kuznia.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationService {

	private static final Duration SESSION_DURATION = Duration.ofMinutes(90);
	private static final EnumSet<ReservationStatus> BLOCKING_STATUSES =
			EnumSet.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED);

	private final ReservationRepository reservationRepository;
	private final UserRepository userRepository;
	private final TrainerProfileRepository trainerProfileRepository;
	private final TrainingTypeRepository trainingTypeRepository;

	@Transactional
	public Reservation requestReservation(Long clientId, Long trainerId, Long trainingTypeId, Instant startTime) {
		Instant endTime = startTime.plus(SESSION_DURATION);
		if (hasOverlappingReservation(trainerId, startTime, endTime)) {
			throw new DomainException("Ten termin jest juz zajety.");
		}

		Reservation reservation = new Reservation();
		reservation.setClient(userRepository.findById(clientId)
				.orElseThrow(() -> new DomainException("Nie znaleziono klienta.")));
		reservation.setTrainerProfile(trainerProfileRepository.findById(trainerId)
				.orElseThrow(() -> new DomainException("Nie znaleziono trenera.")));
		reservation.setTrainingType(trainingTypeRepository.findById(trainingTypeId)
				.orElseThrow(() -> new DomainException("Nie znaleziono typu treningu.")));
		reservation.setStartTime(startTime);
		reservation.setEndTime(endTime);
		reservation.setStatus(ReservationStatus.PENDING);

		return reservationRepository.save(reservation);
	}

	@Transactional
	public Reservation confirmReservation(Long reservationId) {
		Reservation reservation = getReservation(reservationId);
		if (reservation.getStatus() != ReservationStatus.PENDING) {
			throw new DomainException("Potwierdzic mozna tylko rezerwacje oczekujaca.");
		}
		reservation.setStatus(ReservationStatus.CONFIRMED);
		return reservation;
	}

	@Transactional
	public Reservation rejectReservation(Long reservationId) {
		Reservation reservation = getReservation(reservationId);
		if (reservation.getStatus() != ReservationStatus.PENDING) {
			throw new DomainException("Odrzucic mozna tylko rezerwacje oczekujaca.");
		}
		reservation.setStatus(ReservationStatus.REJECTED);
		return reservation;
	}

	private Reservation getReservation(Long reservationId) {
		return reservationRepository.findById(reservationId)
				.orElseThrow(() -> new DomainException("Nie znaleziono rezerwacji."));
	}

	private boolean hasOverlappingReservation(Long trainerId, Instant startTime, Instant endTime) {
		return reservationRepository.countOverlappingReservations(trainerId, startTime, endTime, BLOCKING_STATUSES) > 0;
	}
}
