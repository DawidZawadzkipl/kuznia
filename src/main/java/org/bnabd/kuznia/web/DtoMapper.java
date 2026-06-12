package org.bnabd.kuznia.web;

import java.util.Comparator;
import org.bnabd.kuznia.domain.LiftResult;
import org.bnabd.kuznia.domain.LiftType;
import org.bnabd.kuznia.domain.Reservation;
import org.bnabd.kuznia.domain.TrainerAvailability;
import org.bnabd.kuznia.domain.TrainerCertificate;
import org.bnabd.kuznia.domain.TrainerProfile;
import org.bnabd.kuznia.domain.TrainerSpecialization;
import org.bnabd.kuznia.domain.TrainingNote;
import org.bnabd.kuznia.domain.TrainingStation;
import org.bnabd.kuznia.domain.TrainingType;
import org.bnabd.kuznia.domain.User;
import org.bnabd.kuznia.web.dto.AvailabilityResponse;
import org.bnabd.kuznia.web.dto.CertificateResponse;
import org.bnabd.kuznia.web.dto.LiftResultResponse;
import org.bnabd.kuznia.web.dto.LiftTypeResponse;
import org.bnabd.kuznia.web.dto.ReservationResponse;
import org.bnabd.kuznia.web.dto.SpecializationResponse;
import org.bnabd.kuznia.web.dto.TrainerResponse;
import org.bnabd.kuznia.web.dto.TrainingNoteResponse;
import org.bnabd.kuznia.web.dto.TrainingStationResponse;
import org.bnabd.kuznia.web.dto.TrainingTypeResponse;
import org.bnabd.kuznia.web.dto.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class DtoMapper {

	public UserResponse toUserResponse(User user) {
		return new UserResponse(
				user.getId(),
				user.getEmail(),
				user.getFirstName(),
				user.getLastName(),
				user.getPhone(),
				user.getRole().getName().name(),
				user.isActive(),
				user.getCreatedAt()
		);
	}

	public SpecializationResponse toSpecializationResponse(TrainerSpecialization specialization) {
		return new SpecializationResponse(
				specialization.getId(),
				specialization.getName(),
				specialization.getDescription()
		);
	}

	public TrainingTypeResponse toTrainingTypeResponse(TrainingType trainingType) {
		return new TrainingTypeResponse(
				trainingType.getId(),
				trainingType.getName(),
				trainingType.getDescription(),
				trainingType.getDurationMinutes(),
				trainingType.getPrice(),
				trainingType.isActive()
		);
	}

	public TrainingStationResponse toTrainingStationResponse(TrainingStation station) {
		return new TrainingStationResponse(
				station.getId(),
				station.getName(),
				station.getDescription(),
				station.isActive()
		);
	}

	public TrainerResponse toTrainerResponse(TrainerProfile trainer) {
		User user = trainer.getUser();
		return new TrainerResponse(
				trainer.getId(),
				user.getId(),
				user.getEmail(),
				user.getFirstName(),
				user.getLastName(),
				user.getPhone(),
				user.isActive(),
				trainer.getBio(),
				trainer.getExperienceYears(),
				trainer.getHourlyRate(),
				trainer.getSpecializations().stream()
						.sorted(Comparator.comparing(TrainerSpecialization::getName))
						.map(this::toSpecializationResponse)
						.toList()
		);
	}

	public CertificateResponse toCertificateResponse(TrainerCertificate certificate) {
		return new CertificateResponse(
				certificate.getId(),
				certificate.getTrainerProfile().getId(),
				certificate.getName(),
				certificate.getIssuingOrganization(),
				certificate.getIssueDate(),
				certificate.getExpirationDate(),
				certificate.getCertificateNumber()
		);
	}

	public AvailabilityResponse toAvailabilityResponse(TrainerAvailability availability) {
		return new AvailabilityResponse(
				availability.getId(),
				availability.getTrainerProfile().getId(),
				availability.getStartTime(),
				availability.getEndTime(),
				availability.isAvailable()
		);
	}

	public ReservationResponse toReservationResponse(Reservation reservation) {
		User client = reservation.getClient();
		User trainerUser = reservation.getTrainerProfile().getUser();
		TrainingStation station = reservation.getTrainingStation();
		return new ReservationResponse(
				reservation.getId(),
				client.getId(),
				client.getFirstName() + " " + client.getLastName(),
				reservation.getTrainerProfile().getId(),
				trainerUser.getFirstName() + " " + trainerUser.getLastName(),
				reservation.getTrainingType().getId(),
				reservation.getTrainingType().getName(),
				station == null ? null : station.getId(),
				station == null ? null : station.getName(),
				reservation.getStartTime(),
				reservation.getEndTime(),
				reservation.getStatus().name(),
				reservation.getCancellationReason(),
				reservation.getCreatedAt()
		);
	}

	public LiftResultResponse toLiftResultResponse(LiftResult result) {
		return new LiftResultResponse(
				result.getId(),
				result.getClient().getId(),
				result.getLiftType().getName().name(),
				result.getLiftType().getDisplayName(),
				result.getWeightKg(),
				result.getReps(),
				result.getEstimatedOneRepMax(),
				result.getResultDate(),
				result.getNotes(),
				result.getCreatedAt()
		);
	}

	public LiftTypeResponse toLiftTypeResponse(LiftType liftType) {
		return new LiftTypeResponse(
				liftType.getId(),
				liftType.getName().name(),
				liftType.getDisplayName()
		);
	}

	public TrainingNoteResponse toTrainingNoteResponse(TrainingNote note) {
		return new TrainingNoteResponse(
				note.getId(),
				note.getReservation().getId(),
				note.getTrainerProfile().getId(),
				note.getClient().getId(),
				note.getNote(),
				note.getCreatedAt()
		);
	}
}
