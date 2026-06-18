package org.bnabd.kuznia.service;

import lombok.RequiredArgsConstructor;
import org.bnabd.kuznia.domain.TrainerSpecialization;
import org.bnabd.kuznia.domain.TrainingStation;
import org.bnabd.kuznia.domain.TrainingType;
import org.bnabd.kuznia.repository.TrainerSpecializationRepository;
import org.bnabd.kuznia.repository.TrainingStationRepository;
import org.bnabd.kuznia.repository.TrainingTypeRepository;
import org.bnabd.kuznia.web.dto.SpecializationRequest;
import org.bnabd.kuznia.web.dto.TrainingStationRequest;
import org.bnabd.kuznia.web.dto.TrainingTypeRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CatalogService {

	private final TrainerSpecializationRepository specializationRepository;
	private final TrainingTypeRepository trainingTypeRepository;
	private final TrainingStationRepository stationRepository;

	@Transactional
	public TrainerSpecialization createSpecialization(SpecializationRequest request) {
		specializationRepository.findByName(request.name()).ifPresent(existing -> {
			throw new DomainException("Specjalizacja o tej nazwie juz istnieje.");
		});
		TrainerSpecialization specialization = new TrainerSpecialization();
		specialization.setName(request.name());
		specialization.setDescription(request.description());
		return specializationRepository.save(specialization);
	}

	@Transactional
	public TrainerSpecialization updateSpecialization(Long id, SpecializationRequest request) {
		TrainerSpecialization specialization = specializationRepository.findById(id)
				.orElseThrow(() -> new DomainException("Nie znaleziono specjalizacji."));
		specialization.setName(request.name());
		specialization.setDescription(request.description());
		return specialization;
	}

	@Transactional
	public TrainingType createTrainingType(TrainingTypeRequest request) {
		trainingTypeRepository.findByName(request.name()).ifPresent(existing -> {
			throw new DomainException("Typ treningu o tej nazwie juz istnieje.");
		});
		TrainingType type = new TrainingType();
		applyTrainingType(type, request);
		return trainingTypeRepository.save(type);
	}

	@Transactional
	public TrainingType updateTrainingType(Long id, TrainingTypeRequest request) {
		TrainingType type = trainingTypeRepository.findById(id)
				.orElseThrow(() -> new DomainException("Nie znaleziono typu treningu."));
		applyTrainingType(type, request);
		return type;
	}

	@Transactional
	public TrainingStation createStation(TrainingStationRequest request) {
		stationRepository.findByName(request.name()).ifPresent(existing -> {
			throw new DomainException("Stanowisko o tej nazwie juz istnieje.");
		});
		TrainingStation station = new TrainingStation();
		applyStation(station, request);
		return stationRepository.save(station);
	}

	@Transactional
	public TrainingStation updateStation(Long id, TrainingStationRequest request) {
		TrainingStation station = stationRepository.findById(id)
				.orElseThrow(() -> new DomainException("Nie znaleziono stanowiska."));
		applyStation(station, request);
		return station;
	}

	private void applyTrainingType(TrainingType type, TrainingTypeRequest request) {
		type.setName(request.name());
		type.setDescription(request.description());
		type.setDurationMinutes(90);
		type.setPrice(request.price());
		type.setActive(request.active() == null || request.active());
	}

	private void applyStation(TrainingStation station, TrainingStationRequest request) {
		station.setName(request.name());
		station.setDescription(request.description());
		station.setActive(request.active() == null || request.active());
	}
}
