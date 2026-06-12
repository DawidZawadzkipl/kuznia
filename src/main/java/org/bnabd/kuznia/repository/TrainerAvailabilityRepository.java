package org.bnabd.kuznia.repository;

import java.time.Instant;
import java.util.List;
import org.bnabd.kuznia.domain.TrainerAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrainerAvailabilityRepository extends JpaRepository<TrainerAvailability, Long> {
	List<TrainerAvailability> findByTrainerProfile_IdOrderByStartTimeAsc(Long trainerId);

	@Query("""
			select availability from TrainerAvailability availability
			where availability.trainerProfile.id = :trainerId
			  and availability.available = true
			  and availability.startTime <= :startTime
			  and availability.endTime >= :endTime
			""")
	List<TrainerAvailability> findMatchingAvailability(
			@Param("trainerId") Long trainerId,
			@Param("startTime") Instant startTime,
			@Param("endTime") Instant endTime
	);
}
