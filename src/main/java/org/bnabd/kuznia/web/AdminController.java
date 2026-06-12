package org.bnabd.kuznia.web;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bnabd.kuznia.domain.ReservationStatus;
import org.bnabd.kuznia.domain.RoleName;
import org.bnabd.kuznia.domain.TrainerCertificate;
import org.bnabd.kuznia.domain.TrainerProfile;
import org.bnabd.kuznia.repository.ReservationRepository;
import org.bnabd.kuznia.repository.TrainerCertificateRepository;
import org.bnabd.kuznia.repository.TrainerProfileRepository;
import org.bnabd.kuznia.repository.TrainerSpecializationRepository;
import org.bnabd.kuznia.repository.TrainingStationRepository;
import org.bnabd.kuznia.repository.TrainingTypeRepository;
import org.bnabd.kuznia.repository.UserRepository;
import org.bnabd.kuznia.service.CatalogService;
import org.bnabd.kuznia.service.DomainException;
import org.bnabd.kuznia.service.TrainerService;
import org.bnabd.kuznia.service.UserService;
import org.bnabd.kuznia.web.dto.AdminStatsResponse;
import org.bnabd.kuznia.web.dto.CertificateRequest;
import org.bnabd.kuznia.web.dto.CertificateResponse;
import org.bnabd.kuznia.web.dto.ReservationResponse;
import org.bnabd.kuznia.web.dto.SpecializationRequest;
import org.bnabd.kuznia.web.dto.SpecializationResponse;
import org.bnabd.kuznia.web.dto.TrainerRequest;
import org.bnabd.kuznia.web.dto.TrainerResponse;
import org.bnabd.kuznia.web.dto.TrainingStationRequest;
import org.bnabd.kuznia.web.dto.TrainingStationResponse;
import org.bnabd.kuznia.web.dto.TrainingTypeRequest;
import org.bnabd.kuznia.web.dto.TrainingTypeResponse;
import org.bnabd.kuznia.web.dto.UserResponse;
import org.bnabd.kuznia.web.dto.UserStatusRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

	private final UserRepository userRepository;
	private final TrainerSpecializationRepository specializationRepository;
	private final TrainingTypeRepository trainingTypeRepository;
	private final TrainingStationRepository stationRepository;
	private final TrainerCertificateRepository certificateRepository;
	private final TrainerProfileRepository trainerProfileRepository;
	private final ReservationRepository reservationRepository;
	private final UserService userService;
	private final TrainerService trainerService;
	private final CatalogService catalogService;
	private final DtoMapper mapper;

	@GetMapping("/users")
	public List<UserResponse> users() {
		return userRepository.findAll().stream()
				.map(mapper::toUserResponse)
				.toList();
	}

	@PutMapping("/users/{id}/status")
	public UserResponse updateUserStatus(@PathVariable Long id, @Valid @RequestBody UserStatusRequest request) {
		return mapper.toUserResponse(userService.setActive(id, request.active()));
	}

	@GetMapping("/trainers")
	public List<TrainerResponse> trainers() {
		return trainerService.findAll().stream()
				.map(mapper::toTrainerResponse)
				.toList();
	}

	@PostMapping("/trainers")
	public TrainerResponse createTrainer(@Valid @RequestBody TrainerRequest request) {
		return mapper.toTrainerResponse(trainerService.createTrainer(request));
	}

	@PutMapping("/trainers/{id}")
	public TrainerResponse updateTrainer(@PathVariable Long id, @Valid @RequestBody TrainerRequest request) {
		return mapper.toTrainerResponse(trainerService.updateTrainer(id, request));
	}

	@GetMapping("/specializations")
	public List<SpecializationResponse> specializations() {
		return specializationRepository.findAll().stream()
				.map(mapper::toSpecializationResponse)
				.toList();
	}

	@PostMapping("/specializations")
	public SpecializationResponse createSpecialization(@Valid @RequestBody SpecializationRequest request) {
		return mapper.toSpecializationResponse(catalogService.createSpecialization(request));
	}

	@PutMapping("/specializations/{id}")
	public SpecializationResponse updateSpecialization(
			@PathVariable Long id,
			@Valid @RequestBody SpecializationRequest request
	) {
		return mapper.toSpecializationResponse(catalogService.updateSpecialization(id, request));
	}

	@GetMapping("/training-types")
	public List<TrainingTypeResponse> trainingTypes() {
		return trainingTypeRepository.findAll().stream()
				.map(mapper::toTrainingTypeResponse)
				.toList();
	}

	@PostMapping("/training-types")
	public TrainingTypeResponse createTrainingType(@Valid @RequestBody TrainingTypeRequest request) {
		return mapper.toTrainingTypeResponse(catalogService.createTrainingType(request));
	}

	@PutMapping("/training-types/{id}")
	public TrainingTypeResponse updateTrainingType(
			@PathVariable Long id,
			@Valid @RequestBody TrainingTypeRequest request
	) {
		return mapper.toTrainingTypeResponse(catalogService.updateTrainingType(id, request));
	}

	@GetMapping("/training-stations")
	public List<TrainingStationResponse> trainingStations() {
		return stationRepository.findAll().stream()
				.map(mapper::toTrainingStationResponse)
				.toList();
	}

	@PostMapping("/training-stations")
	public TrainingStationResponse createTrainingStation(@Valid @RequestBody TrainingStationRequest request) {
		return mapper.toTrainingStationResponse(catalogService.createStation(request));
	}

	@PutMapping("/training-stations/{id}")
	public TrainingStationResponse updateTrainingStation(
			@PathVariable Long id,
			@Valid @RequestBody TrainingStationRequest request
	) {
		return mapper.toTrainingStationResponse(catalogService.updateStation(id, request));
	}

	@GetMapping("/certificates")
	public List<CertificateResponse> certificates() {
		return certificateRepository.findAll().stream()
				.map(mapper::toCertificateResponse)
				.toList();
	}

	@GetMapping("/trainers/{trainerId}/certificates")
	public List<CertificateResponse> trainerCertificates(@PathVariable Long trainerId) {
		return certificateRepository.findByTrainerProfile_Id(trainerId).stream()
				.map(mapper::toCertificateResponse)
				.toList();
	}

	@PostMapping("/certificates")
	@Transactional
	public CertificateResponse createCertificate(@Valid @RequestBody CertificateRequest request) {
		TrainerProfile trainer = trainerProfileRepository.findById(request.trainerId())
				.orElseThrow(() -> new DomainException("Nie znaleziono trenera."));
		TrainerCertificate certificate = new TrainerCertificate();
		certificate.setTrainerProfile(trainer);
		applyCertificate(certificate, request);
		return mapper.toCertificateResponse(certificateRepository.save(certificate));
	}

	@PutMapping("/certificates/{id}")
	@Transactional
	public CertificateResponse updateCertificate(@PathVariable Long id, @Valid @RequestBody CertificateRequest request) {
		TrainerCertificate certificate = certificateRepository.findById(id)
				.orElseThrow(() -> new DomainException("Nie znaleziono certyfikatu."));
		TrainerProfile trainer = trainerProfileRepository.findById(request.trainerId())
				.orElseThrow(() -> new DomainException("Nie znaleziono trenera."));
		certificate.setTrainerProfile(trainer);
		applyCertificate(certificate, request);
		return mapper.toCertificateResponse(certificate);
	}

	@GetMapping("/reservations")
	public List<ReservationResponse> reservations() {
		return reservationRepository.findAll().stream()
				.map(mapper::toReservationResponse)
				.toList();
	}

	@GetMapping("/stats")
	public AdminStatsResponse stats() {
		return new AdminStatsResponse(
				userRepository.count(),
				trainerProfileRepository.count(),
				userRepository.countByRole_Name(RoleName.CLIENT),
				reservationRepository.count(),
				reservationRepository.countByStatus(ReservationStatus.PENDING),
				reservationRepository.countByStatus(ReservationStatus.COMPLETED)
		);
	}

	private void applyCertificate(TrainerCertificate certificate, CertificateRequest request) {
		certificate.setName(request.name());
		certificate.setIssuingOrganization(request.issuingOrganization());
		certificate.setIssueDate(request.issueDate());
		certificate.setExpirationDate(request.expirationDate());
		certificate.setCertificateNumber(request.certificateNumber());
	}
}
