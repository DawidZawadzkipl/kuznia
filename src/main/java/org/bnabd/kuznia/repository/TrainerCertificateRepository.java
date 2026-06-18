package org.bnabd.kuznia.repository;

import java.util.List;
import org.bnabd.kuznia.domain.TrainerCertificate;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerCertificateRepository extends JpaRepository<TrainerCertificate, Long> {
	@EntityGraph(attributePaths = {"trainerProfile"})
	List<TrainerCertificate> findByTrainerProfile_Id(Long trainerId);
}
