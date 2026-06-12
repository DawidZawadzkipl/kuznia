package org.bnabd.kuznia.web;

import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bnabd.kuznia.domain.LiftTypeName;
import org.bnabd.kuznia.domain.TrainerProfile;
import org.bnabd.kuznia.domain.User;
import org.bnabd.kuznia.service.AvailabilityService;
import org.bnabd.kuznia.service.CurrentUserService;
import org.bnabd.kuznia.service.LiftResultService;
import org.bnabd.kuznia.service.ReservationService;
import org.bnabd.kuznia.service.TrainerService;
import org.bnabd.kuznia.service.TrainingNoteService;
import org.bnabd.kuznia.web.dto.AvailabilityRequest;
import org.bnabd.kuznia.web.dto.AvailabilityResponse;
import org.bnabd.kuznia.web.dto.CancelReservationRequest;
import org.bnabd.kuznia.web.dto.LiftResultResponse;
import org.bnabd.kuznia.web.dto.ProgressPointResponse;
import org.bnabd.kuznia.web.dto.ReservationResponse;
import org.bnabd.kuznia.web.dto.TrainerProfileRequest;
import org.bnabd.kuznia.web.dto.TrainerResponse;
import org.bnabd.kuznia.web.dto.TrainingNoteRequest;
import org.bnabd.kuznia.web.dto.TrainingNoteResponse;
import org.bnabd.kuznia.web.dto.UserResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trainer")
@RequiredArgsConstructor
public class TrainerController {

	private final CurrentUserService currentUserService;
	private final TrainerService trainerService;
	private final AvailabilityService availabilityService;
	private final ReservationService reservationService;
	private final LiftResultService liftResultService;
	private final TrainingNoteService noteService;
	private final DtoMapper mapper;

	@GetMapping("/profile")
	public TrainerResponse profile() {
		return mapper.toTrainerResponse(currentTrainer());
	}

	@PutMapping("/profile")
	public TrainerResponse updateProfile(@Valid @RequestBody TrainerProfileRequest request) {
		User user = currentUserService.getCurrentUser();
		return mapper.toTrainerResponse(trainerService.updateOwnProfile(user.getId(), request));
	}

	@GetMapping("/availability")
	public List<AvailabilityResponse> availability() {
		return availabilityService.findByTrainer(currentTrainer().getId()).stream()
				.map(mapper::toAvailabilityResponse)
				.toList();
	}

	@PostMapping("/availability")
	public AvailabilityResponse createAvailability(@Valid @RequestBody AvailabilityRequest request) {
		return mapper.toAvailabilityResponse(availabilityService.create(currentTrainer().getId(), request));
	}

	@PutMapping("/availability/{id}")
	public AvailabilityResponse updateAvailability(@PathVariable Long id, @Valid @RequestBody AvailabilityRequest request) {
		return mapper.toAvailabilityResponse(availabilityService.updateForTrainer(currentTrainer().getId(), id, request));
	}

	@GetMapping("/reservations")
	public List<ReservationResponse> reservations() {
		return reservationService.findByTrainer(currentTrainer().getId()).stream()
				.map(mapper::toReservationResponse)
				.toList();
	}

	@PutMapping("/reservations/{id}/confirm")
	public ReservationResponse confirmReservation(@PathVariable Long id) {
		return mapper.toReservationResponse(reservationService.confirmTrainerReservation(currentTrainer().getId(), id));
	}

	@PutMapping("/reservations/{id}/reject")
	public ReservationResponse rejectReservation(@PathVariable Long id) {
		return mapper.toReservationResponse(reservationService.rejectTrainerReservation(currentTrainer().getId(), id));
	}

	@PutMapping("/reservations/{id}/cancel")
	public ReservationResponse cancelReservation(
			@PathVariable Long id,
			@RequestBody(required = false) CancelReservationRequest request
	) {
		return mapper.toReservationResponse(reservationService.cancelTrainerReservation(currentTrainer().getId(), id, request));
	}

	@PutMapping("/reservations/{id}/complete")
	public ReservationResponse completeReservation(@PathVariable Long id) {
		return mapper.toReservationResponse(reservationService.completeTrainerReservation(currentTrainer().getId(), id));
	}

	@GetMapping("/clients")
	public List<UserResponse> clients() {
		return reservationService.findByTrainer(currentTrainer().getId()).stream()
				.map(reservation -> reservation.getClient())
				.distinct()
				.sorted(Comparator.comparing(User::getLastName).thenComparing(User::getFirstName))
				.map(mapper::toUserResponse)
				.toList();
	}

	@GetMapping("/clients/{clientId}/lift-results")
	public List<LiftResultResponse> clientLiftResults(
			@PathVariable Long clientId,
			@RequestParam(required = false) LiftTypeName liftType
	) {
		reservationService.ensureTrainerHasClient(currentTrainer().getId(), clientId);
		return (liftType == null
				? liftResultService.findByClient(clientId)
				: liftResultService.findByClientAndLift(clientId, liftType)).stream()
				.map(mapper::toLiftResultResponse)
				.toList();
	}

	@GetMapping("/clients/{clientId}/progress")
	public List<ProgressPointResponse> clientProgress(
			@PathVariable Long clientId,
			@RequestParam(required = false) LiftTypeName liftType
	) {
		reservationService.ensureTrainerHasClient(currentTrainer().getId(), clientId);
		return (liftType == null
				? liftResultService.findByClient(clientId)
				: liftResultService.findByClientAndLift(clientId, liftType)).stream()
				.map(result -> new ProgressPointResponse(
						result.getResultDate(),
						result.getLiftType().getName().name(),
						result.getEstimatedOneRepMax(),
						result.getWeightKg(),
						result.getReps()
				))
				.toList();
	}

	@GetMapping("/notes")
	public List<TrainingNoteResponse> notes() {
		return noteService.findByTrainer(currentTrainer().getId()).stream()
				.map(mapper::toTrainingNoteResponse)
				.toList();
	}

	@PostMapping("/notes")
	public TrainingNoteResponse addNote(@Valid @RequestBody TrainingNoteRequest request) {
		return mapper.toTrainingNoteResponse(noteService.addNote(currentTrainer().getId(), request));
	}

	private TrainerProfile currentTrainer() {
		return trainerService.findByUserId(currentUserService.getCurrentUser().getId());
	}
}
