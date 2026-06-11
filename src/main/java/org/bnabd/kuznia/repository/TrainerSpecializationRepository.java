package org.bnabd.kuznia.repository;

import java.util.Optional;
import org.bnabd.kuznia.domain.TrainerSpecialization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerSpecializationRepository extends JpaRepository<TrainerSpecialization, Long> {
	Optional<TrainerSpecialization> findByName(String name);
}
