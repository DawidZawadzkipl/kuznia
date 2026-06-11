package org.bnabd.kuznia.repository;

import java.time.Instant;
import java.util.Collection;
import org.bnabd.kuznia.domain.Reservation;
import org.bnabd.kuznia.domain.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

	@Query("""
			select count(reservation) from Reservation reservation
			where reservation.trainerProfile.id = :trainerId
			  and reservation.status in :statuses
			  and reservation.startTime < :endTime
			  and reservation.endTime > :startTime
			""")
	long countOverlappingReservations(
			@Param("trainerId") Long trainerId,
			@Param("startTime") Instant startTime,
			@Param("endTime") Instant endTime,
			@Param("statuses") Collection<ReservationStatus> statuses
	);
}
