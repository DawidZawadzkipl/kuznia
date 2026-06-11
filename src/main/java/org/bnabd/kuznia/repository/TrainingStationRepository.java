package org.bnabd.kuznia.repository;

import java.util.Optional;
import org.bnabd.kuznia.domain.TrainingStation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainingStationRepository extends JpaRepository<TrainingStation, Long> {
	Optional<TrainingStation> findByName(String name);
}
