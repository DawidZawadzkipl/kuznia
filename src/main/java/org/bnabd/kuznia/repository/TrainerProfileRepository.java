package org.bnabd.kuznia.repository;

import java.util.List;
import java.util.Optional;
import org.bnabd.kuznia.domain.TrainerProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerProfileRepository extends JpaRepository<TrainerProfile, Long> {
	@Override
	@EntityGraph(attributePaths = {"user", "specializations"})
	List<TrainerProfile> findAll();

	@EntityGraph(attributePaths = {"user", "specializations"})
	Optional<TrainerProfile> findWithUserAndSpecializationsById(Long id);

	@EntityGraph(attributePaths = {"user", "specializations"})
	Optional<TrainerProfile> findByUser_Id(Long userId);
}
