package org.bnabd.kuznia.repository;

import java.util.Optional;
import org.bnabd.kuznia.domain.TrainingType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainingTypeRepository extends JpaRepository<TrainingType, Long> {
	Optional<TrainingType> findByName(String name);
}
