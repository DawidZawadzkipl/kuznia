package org.bnabd.kuznia.service;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bnabd.kuznia.domain.ReservationStatus;
import org.bnabd.kuznia.domain.TrainerAvailability;
import org.bnabd.kuznia.domain.TrainerProfile;
import org.bnabd.kuznia.repository.ReservationRepository;
import org.bnabd.kuznia.repository.TrainerAvailabilityRepository;
import org.bnabd.kuznia.repository.TrainerProfileRepository;
import org.bnabd.kuznia.web.dto.AvailabilityRequest;
import org.bnabd.kuznia.web.dto.AvailableSlotResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

	private static final Duration SESSION_DURATION = Duration.ofMinutes(90);
	private static final EnumSet<ReservationStatus> BLOCKING_STATUSES =
			EnumSet.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED);

	private final TrainerAvailabilityRepository availabilityRepository;
	private final TrainerProfileRepository trainerProfileRepository;
	private final ReservationRepository reservationRepository;

	@Transactional(readOnly = true)
	public List<TrainerAvailability> findByTrainer(Long trainerId) {
		return availabilityRepository.findByTrainerProfile_IdOrderByStartTimeAsc(trainerId);
	}

	@Transactional(readOnly = true)
	public List<AvailableSlotResponse> findAvailableSlots(Long trainerId) {
		Instant now = Instant.now();
		return findByTrainer(trainerId).stream()
				.filter(TrainerAvailability::isAvailable)
				.flatMap(availability -> slotsForAvailability(trainerId, availability, now).stream())
				.distinct()
				.toList();
	}

	@Transactional
	public TrainerAvailability create(Long trainerId, AvailabilityRequest request) {
		if (!request.endTime().isAfter(request.startTime())) {
			throw new DomainException("Koniec dostepnosci musi byc po starcie.");
		}
		if (request.available() == null || request.available()) {
			ensureNoAvailabilityOverlap(trainerId, request.startTime(), request.endTime(), null);
		}
		TrainerProfile trainer = trainerProfileRepository.findById(trainerId)
				.orElseThrow(() -> new DomainException("Nie znaleziono trenera."));

		TrainerAvailability availability = new TrainerAvailability();
		availability.setTrainerProfile(trainer);
		availability.setStartTime(request.startTime());
		availability.setEndTime(request.endTime());
		availability.setAvailable(request.available() == null || request.available());
		return availabilityRepository.save(availability);
	}

	@Transactional
	public TrainerAvailability update(Long id, AvailabilityRequest request) {
		if (!request.endTime().isAfter(request.startTime())) {
			throw new DomainException("Koniec dostepnosci musi byc po starcie.");
		}
		TrainerAvailability availability = availabilityRepository.findById(id)
				.orElseThrow(() -> new DomainException("Nie znaleziono dostepnosci."));
		availability.setStartTime(request.startTime());
		availability.setEndTime(request.endTime());
		availability.setAvailable(request.available() == null || request.available());
		return availability;
	}

	@Transactional
	public TrainerAvailability updateForTrainer(Long trainerId, Long id, AvailabilityRequest request) {
		TrainerAvailability availability = availabilityRepository.findById(id)
				.orElseThrow(() -> new DomainException("Nie znaleziono dostepnosci."));
		if (!availability.getTrainerProfile().getId().equals(trainerId)) {
			throw new DomainException("To nie jest dostepnosc tego trenera.");
		}
		if (request.available() == null || request.available()) {
			ensureNoAvailabilityOverlap(trainerId, request.startTime(), request.endTime(), id);
		}
		availability.setStartTime(request.startTime());
		availability.setEndTime(request.endTime());
		availability.setAvailable(request.available() == null || request.available());
		return availability;
	}

	private List<AvailableSlotResponse> slotsForAvailability(Long trainerId, TrainerAvailability availability, Instant now) {
		java.util.ArrayList<AvailableSlotResponse> slots = new java.util.ArrayList<>();
		Instant start = availability.getStartTime();
		while (!start.plus(SESSION_DURATION).isAfter(availability.getEndTime())) {
			Instant end = start.plus(SESSION_DURATION);
			if (start.isAfter(now) && !hasBlockingReservation(trainerId, start, end)) {
				slots.add(new AvailableSlotResponse(start, end));
			}
			start = end;
		}
		return slots;
	}

	private boolean hasBlockingReservation(Long trainerId, Instant startTime, Instant endTime) {
		return reservationRepository.countOverlappingReservations(trainerId, startTime, endTime, BLOCKING_STATUSES) > 0;
	}

	private void ensureNoAvailabilityOverlap(Long trainerId, Instant startTime, Instant endTime, Long ignoredAvailabilityId) {
		boolean overlaps = findByTrainer(trainerId).stream()
				.filter(TrainerAvailability::isAvailable)
				.filter(existing -> ignoredAvailabilityId == null || !existing.getId().equals(ignoredAvailabilityId))
				.anyMatch(existing -> existing.getStartTime().isBefore(endTime) && existing.getEndTime().isAfter(startTime));
		if (overlaps) {
			throw new DomainException("Ten zakres dostepnosci naklada sie na istniejacy termin.");
		}
	}
}
