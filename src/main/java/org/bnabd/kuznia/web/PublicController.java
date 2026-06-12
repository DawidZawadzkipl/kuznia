package org.bnabd.kuznia.web;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bnabd.kuznia.repository.TrainerSpecializationRepository;
import org.bnabd.kuznia.repository.LiftTypeRepository;
import org.bnabd.kuznia.repository.TrainingTypeRepository;
import org.bnabd.kuznia.service.AvailabilityService;
import org.bnabd.kuznia.service.TrainerService;
import org.bnabd.kuznia.web.dto.AvailabilityResponse;
import org.bnabd.kuznia.web.dto.LiftTypeResponse;
import org.bnabd.kuznia.web.dto.SpecializationResponse;
import org.bnabd.kuznia.web.dto.TrainerResponse;
import org.bnabd.kuznia.web.dto.TrainingTypeResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

	private final TrainerService trainerService;
	private final TrainingTypeRepository trainingTypeRepository;
	private final LiftTypeRepository liftTypeRepository;
	private final TrainerSpecializationRepository specializationRepository;
	private final AvailabilityService availabilityService;
	private final DtoMapper mapper;

	@GetMapping("/trainers")
	public List<TrainerResponse> trainers() {
		return trainerService.findAll().stream()
				.map(mapper::toTrainerResponse)
				.toList();
	}

	@GetMapping("/trainers/{id}")
	public TrainerResponse trainer(@PathVariable Long id) {
		return mapper.toTrainerResponse(trainerService.findById(id));
	}

	@GetMapping("/trainers/{id}/availability")
	public List<AvailabilityResponse> availability(@PathVariable Long id) {
		return availabilityService.findByTrainer(id).stream()
				.map(mapper::toAvailabilityResponse)
				.toList();
	}

	@GetMapping("/training-types")
	public List<TrainingTypeResponse> trainingTypes() {
		return trainingTypeRepository.findAll().stream()
				.filter(type -> type.isActive())
				.map(mapper::toTrainingTypeResponse)
				.toList();
	}

	@GetMapping("/specializations")
	public List<SpecializationResponse> specializations() {
		return specializationRepository.findAll().stream()
				.map(mapper::toSpecializationResponse)
				.toList();
	}

	@GetMapping("/lift-types")
	public List<LiftTypeResponse> liftTypes() {
		return liftTypeRepository.findAll().stream()
				.map(mapper::toLiftTypeResponse)
				.toList();
	}
}
