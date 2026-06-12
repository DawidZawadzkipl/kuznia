package org.bnabd.kuznia.repository;

import java.util.List;
import org.bnabd.kuznia.domain.TrainingNote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainingNoteRepository extends JpaRepository<TrainingNote, Long> {
	List<TrainingNote> findByClient_IdOrderByCreatedAtDesc(Long clientId);

	List<TrainingNote> findByTrainerProfile_IdOrderByCreatedAtDesc(Long trainerId);
}
