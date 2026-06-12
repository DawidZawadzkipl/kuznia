package org.bnabd.kuznia.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.bnabd.kuznia.domain.Reservation;
import org.bnabd.kuznia.domain.ReservationStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
	@Override
	@EntityGraph(attributePaths = {"client.role", "trainerProfile.user.role", "trainingType", "trainingStation"})
	List<Reservation> findAll();

	@EntityGraph(attributePaths = {"client.role", "trainerProfile.user.role", "trainingType", "trainingStation"})
	List<Reservation> findByClient_IdOrderByStartTimeDesc(Long clientId);

	@EntityGraph(attributePaths = {"client.role", "trainerProfile.user.role", "trainingType", "trainingStation"})
	Optional<Reservation> findDetailedById(Long id);

	@EntityGraph(attributePaths = {"client.role", "trainerProfile.user.role", "trainingType", "trainingStation"})
	List<Reservation> findByTrainerProfile_IdOrderByStartTimeDesc(Long trainerId);

	@EntityGraph(attributePaths = {"client.role", "trainerProfile.user.role", "trainingType", "trainingStation"})
	List<Reservation> findByTrainerProfile_User_IdOrderByStartTimeDesc(Long trainerUserId);

	@EntityGraph(attributePaths = {"client.role", "trainerProfile.user.role", "trainingType", "trainingStation"})
	List<Reservation> findByClient_IdAndStatusOrderByStartTimeDesc(Long clientId, ReservationStatus status);

	@EntityGraph(attributePaths = {"client.role", "trainerProfile.user.role", "trainingType", "trainingStation"})
	List<Reservation> findByTrainerProfile_IdAndStatusOrderByStartTimeDesc(Long trainerId, ReservationStatus status);

	long countByStatus(ReservationStatus status);

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
