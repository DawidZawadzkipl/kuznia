package org.bnabd.kuznia.web;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bnabd.kuznia.domain.LiftTypeName;
import org.bnabd.kuznia.domain.User;
import org.bnabd.kuznia.service.CurrentUserService;
import org.bnabd.kuznia.service.LiftResultService;
import org.bnabd.kuznia.service.ReservationService;
import org.bnabd.kuznia.service.TrainingNoteService;
import org.bnabd.kuznia.web.dto.CancelReservationRequest;
import org.bnabd.kuznia.web.dto.LiftResultRequest;
import org.bnabd.kuznia.web.dto.LiftResultResponse;
import org.bnabd.kuznia.web.dto.PowerliftingTotalResponse;
import org.bnabd.kuznia.web.dto.ProgressPointResponse;
import org.bnabd.kuznia.web.dto.ReservationRequest;
import org.bnabd.kuznia.web.dto.ReservationResponse;
import org.bnabd.kuznia.web.dto.TrainingNoteResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
public class ClientController {

	private final CurrentUserService currentUserService;
	private final ReservationService reservationService;
	private final LiftResultService liftResultService;
	private final TrainingNoteService noteService;
	private final DtoMapper mapper;

	@GetMapping("/reservations")
	public List<ReservationResponse> reservations() {
		User user = currentUserService.getCurrentUser();
		return reservationService.findByClient(user.getId()).stream()
				.map(mapper::toReservationResponse)
				.toList();
	}

	@GetMapping("/training-history")
	public List<ReservationResponse> trainingHistory() {
		User user = currentUserService.getCurrentUser();
		return reservationService.findCompletedByClient(user.getId()).stream()
				.map(mapper::toReservationResponse)
				.toList();
	}

	@PostMapping("/reservations")
	public ReservationResponse requestReservation(@Valid @RequestBody ReservationRequest request) {
		User user = currentUserService.getCurrentUser();
		return mapper.toReservationResponse(reservationService.requestReservation(user.getId(), request));
	}

	@PutMapping("/reservations/{id}/cancel")
	public ReservationResponse cancelReservation(
			@PathVariable Long id,
			@RequestBody(required = false) CancelReservationRequest request
	) {
		User user = currentUserService.getCurrentUser();
		return mapper.toReservationResponse(reservationService.cancelClientReservation(user.getId(), id, request));
	}

	@GetMapping("/lift-results")
	public List<LiftResultResponse> liftResults(@RequestParam(required = false) LiftTypeName liftType) {
		User user = currentUserService.getCurrentUser();
		return (liftType == null
				? liftResultService.findByClient(user.getId())
				: liftResultService.findByClientAndLift(user.getId(), liftType)).stream()
				.map(mapper::toLiftResultResponse)
				.toList();
	}

	@PostMapping("/lift-results")
	public LiftResultResponse addLiftResult(@Valid @RequestBody LiftResultRequest request) {
		User user = currentUserService.getCurrentUser();
		return mapper.toLiftResultResponse(liftResultService.addResult(user.getId(), request));
	}

	@GetMapping("/progress")
	public List<ProgressPointResponse> progress(@RequestParam(required = false) LiftTypeName liftType) {
		User user = currentUserService.getCurrentUser();
		return (liftType == null
				? liftResultService.findByClient(user.getId())
				: liftResultService.findByClientAndLift(user.getId(), liftType)).stream()
				.map(result -> new ProgressPointResponse(
						result.getResultDate(),
						result.getLiftType().getName().name(),
						result.getEstimatedOneRepMax(),
						result.getWeightKg(),
						result.getReps()
				))
				.toList();
	}

	@GetMapping("/total")
	public PowerliftingTotalResponse total() {
		return liftResultService.calculateTotal(currentUserService.getCurrentUser().getId());
	}

	@GetMapping("/notes")
	public List<TrainingNoteResponse> notes() {
		User user = currentUserService.getCurrentUser();
		return noteService.findByClient(user.getId()).stream()
				.map(mapper::toTrainingNoteResponse)
				.toList();
	}
}
