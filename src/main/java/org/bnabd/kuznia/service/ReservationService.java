package org.bnabd.kuznia.service;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bnabd.kuznia.domain.Reservation;
import org.bnabd.kuznia.domain.ReservationStatus;
import org.bnabd.kuznia.domain.TrainerProfile;
import org.bnabd.kuznia.domain.TrainingStation;
import org.bnabd.kuznia.repository.ReservationRepository;
import org.bnabd.kuznia.repository.TrainerAvailabilityRepository;
import org.bnabd.kuznia.repository.TrainerProfileRepository;
import org.bnabd.kuznia.repository.TrainingStationRepository;
import org.bnabd.kuznia.repository.TrainingTypeRepository;
import org.bnabd.kuznia.repository.UserRepository;
import org.bnabd.kuznia.web.dto.CancelReservationRequest;
import org.bnabd.kuznia.web.dto.ReservationRequest;
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
	private final TrainingStationRepository trainingStationRepository;
	private final TrainerAvailabilityRepository availabilityRepository;

	@Transactional
	public Reservation requestReservation(Long clientId, Long trainerId, Long trainingTypeId, Instant startTime) {
		return requestReservation(clientId, new ReservationRequest(trainerId, trainingTypeId, null, startTime));
	}

	@Transactional
	public Reservation requestReservation(Long clientId, ReservationRequest request) {
		Instant startTime = request.startTime();
		Instant endTime = startTime.plus(SESSION_DURATION);
		Long trainerId = request.trainerId();
		if (hasOverlappingReservation(trainerId, startTime, endTime)) {
			throw new DomainException("Ten termin jest juz zajety.");
		}

		TrainerProfile trainer = trainerProfileRepository.findById(trainerId)
				.orElseThrow(() -> new DomainException("Nie znaleziono trenera."));
		if (availabilityRepository.findMatchingAvailability(trainerId, startTime, endTime).isEmpty()) {
			throw new DomainException("Trener nie oznaczyl tego terminu jako dostepnego.");
		}

		Reservation reservation = new Reservation();
		reservation.setClient(userRepository.findById(clientId)
				.orElseThrow(() -> new DomainException("Nie znaleziono klienta.")));
		reservation.setTrainerProfile(trainer);
		reservation.setTrainingType(trainingTypeRepository.findById(request.trainingTypeId())
				.orElseThrow(() -> new DomainException("Nie znaleziono typu treningu.")));
		if (request.trainingStationId() != null) {
			TrainingStation station = trainingStationRepository.findById(request.trainingStationId())
					.orElseThrow(() -> new DomainException("Nie znaleziono stanowiska."));
			reservation.setTrainingStation(station);
		}
		reservation.setStartTime(startTime);
		reservation.setEndTime(endTime);
		reservation.setStatus(ReservationStatus.PENDING);

		return reservationRepository.save(reservation);
	}

	@Transactional(readOnly = true)
	public List<Reservation> findAll() {
		return reservationRepository.findAll();
	}

	@Transactional(readOnly = true)
	public List<Reservation> findByClient(Long clientId) {
		return reservationRepository.findByClient_IdOrderByStartTimeDesc(clientId);
	}

	@Transactional(readOnly = true)
	public List<Reservation> findCompletedByClient(Long clientId) {
		return reservationRepository.findByClient_IdAndStatusOrderByStartTimeDesc(clientId, ReservationStatus.COMPLETED);
	}

	@Transactional(readOnly = true)
	public List<Reservation> findByTrainer(Long trainerId) {
		return reservationRepository.findByTrainerProfile_IdOrderByStartTimeDesc(trainerId);
	}

	@Transactional(readOnly = true)
	public List<Reservation> findByTrainerUser(Long trainerUserId) {
		return reservationRepository.findByTrainerProfile_User_IdOrderByStartTimeDesc(trainerUserId);
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
	public Reservation confirmTrainerReservation(Long trainerId, Long reservationId) {
		Reservation reservation = ensureTrainerReservation(trainerId, reservationId);
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

	@Transactional
	public Reservation rejectTrainerReservation(Long trainerId, Long reservationId) {
		Reservation reservation = ensureTrainerReservation(trainerId, reservationId);
		if (reservation.getStatus() != ReservationStatus.PENDING) {
			throw new DomainException("Odrzucic mozna tylko rezerwacje oczekujaca.");
		}
		reservation.setStatus(ReservationStatus.REJECTED);
		return reservation;
	}

	@Transactional
	public Reservation cancelReservation(Long reservationId, CancelReservationRequest request) {
		Reservation reservation = getReservation(reservationId);
		if (reservation.getStatus() == ReservationStatus.COMPLETED || reservation.getStatus() == ReservationStatus.REJECTED) {
			throw new DomainException("Nie mozna anulowac tej rezerwacji.");
		}
		reservation.setStatus(ReservationStatus.CANCELLED);
		reservation.setCancellationReason(request == null ? null : request.reason());
		return reservation;
	}

	@Transactional
	public Reservation cancelClientReservation(Long clientId, Long reservationId, CancelReservationRequest request) {
		Reservation reservation = getReservation(reservationId);
		if (!reservation.getClient().getId().equals(clientId)) {
			throw new DomainException("To nie jest rezerwacja tego klienta.");
		}
		return cancelReservation(reservationId, request);
	}

	@Transactional
	public Reservation cancelTrainerReservation(Long trainerId, Long reservationId, CancelReservationRequest request) {
		ensureTrainerReservation(trainerId, reservationId);
		return cancelReservation(reservationId, request);
	}

	@Transactional
	public Reservation completeReservation(Long reservationId) {
		Reservation reservation = getReservation(reservationId);
		if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
			throw new DomainException("Jako zrealizowana mozna oznaczyc tylko potwierdzona rezerwacje.");
		}
		reservation.setStatus(ReservationStatus.COMPLETED);
		return reservation;
	}

	@Transactional
	public Reservation completeTrainerReservation(Long trainerId, Long reservationId) {
		Reservation reservation = ensureTrainerReservation(trainerId, reservationId);
		if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
			throw new DomainException("Jako zrealizowana mozna oznaczyc tylko potwierdzona rezerwacje.");
		}
		reservation.setStatus(ReservationStatus.COMPLETED);
		return reservation;
	}

	@Transactional(readOnly = true)
	public void ensureTrainerHasClient(Long trainerId, Long clientId) {
		boolean hasClient = findByTrainer(trainerId).stream()
				.anyMatch(reservation -> reservation.getClient().getId().equals(clientId));
		if (!hasClient) {
			throw new DomainException("Ten klient nie jest podopiecznym trenera.");
		}
	}

	private Reservation getReservation(Long reservationId) {
		return reservationRepository.findDetailedById(reservationId)
				.orElseThrow(() -> new DomainException("Nie znaleziono rezerwacji."));
	}

	private Reservation ensureTrainerReservation(Long trainerId, Long reservationId) {
		Reservation reservation = getReservation(reservationId);
		if (!reservation.getTrainerProfile().getId().equals(trainerId)) {
			throw new DomainException("To nie jest rezerwacja tego trenera.");
		}
		return reservation;
	}

	private boolean hasOverlappingReservation(Long trainerId, Instant startTime, Instant endTime) {
		return reservationRepository.countOverlappingReservations(trainerId, startTime, endTime, BLOCKING_STATUSES) > 0;
	}
}
