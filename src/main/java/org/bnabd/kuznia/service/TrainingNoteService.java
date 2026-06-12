package org.bnabd.kuznia.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bnabd.kuznia.domain.Reservation;
import org.bnabd.kuznia.domain.TrainingNote;
import org.bnabd.kuznia.repository.ReservationRepository;
import org.bnabd.kuznia.repository.TrainingNoteRepository;
import org.bnabd.kuznia.web.dto.TrainingNoteRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TrainingNoteService {

	private final TrainingNoteRepository noteRepository;
	private final ReservationRepository reservationRepository;

	@Transactional(readOnly = true)
	public List<TrainingNote> findByClient(Long clientId) {
		return noteRepository.findByClient_IdOrderByCreatedAtDesc(clientId);
	}

	@Transactional(readOnly = true)
	public List<TrainingNote> findByTrainer(Long trainerId) {
		return noteRepository.findByTrainerProfile_IdOrderByCreatedAtDesc(trainerId);
	}

	@Transactional
	public TrainingNote addNote(Long trainerId, TrainingNoteRequest request) {
		Reservation reservation = reservationRepository.findById(request.reservationId())
				.orElseThrow(() -> new DomainException("Nie znaleziono rezerwacji."));
		if (!reservation.getTrainerProfile().getId().equals(trainerId)) {
			throw new DomainException("To nie jest rezerwacja tego trenera.");
		}

		TrainingNote note = new TrainingNote();
		note.setReservation(reservation);
		note.setTrainerProfile(reservation.getTrainerProfile());
		note.setClient(reservation.getClient());
		note.setNote(request.note());
		return noteRepository.save(note);
	}
}
