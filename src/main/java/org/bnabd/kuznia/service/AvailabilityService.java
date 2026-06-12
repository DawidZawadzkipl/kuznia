package org.bnabd.kuznia.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bnabd.kuznia.domain.TrainerAvailability;
import org.bnabd.kuznia.domain.TrainerProfile;
import org.bnabd.kuznia.repository.TrainerAvailabilityRepository;
import org.bnabd.kuznia.repository.TrainerProfileRepository;
import org.bnabd.kuznia.web.dto.AvailabilityRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

	private final TrainerAvailabilityRepository availabilityRepository;
	private final TrainerProfileRepository trainerProfileRepository;

	@Transactional(readOnly = true)
	public List<TrainerAvailability> findByTrainer(Long trainerId) {
		return availabilityRepository.findByTrainerProfile_IdOrderByStartTimeAsc(trainerId);
	}

	@Transactional
	public TrainerAvailability create(Long trainerId, AvailabilityRequest request) {
		if (!request.endTime().isAfter(request.startTime())) {
			throw new DomainException("Koniec dostepnosci musi byc po starcie.");
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
		TrainerAvailability availability = update(id, request);
		if (!availability.getTrainerProfile().getId().equals(trainerId)) {
			throw new DomainException("To nie jest dostepnosc tego trenera.");
		}
		return availability;
	}
}
