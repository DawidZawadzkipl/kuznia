package org.bnabd.kuznia.web.dto;

public record AdminStatsResponse(
		long users,
		long trainers,
		long clients,
		long reservations,
		long pendingReservations,
		long completedReservations
) {
}
