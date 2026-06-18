package org.bnabd.kuznia.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.bnabd.kuznia.domain.LiftResult;
import org.bnabd.kuznia.domain.LiftType;
import org.bnabd.kuznia.domain.LiftTypeName;
import org.bnabd.kuznia.domain.User;
import org.bnabd.kuznia.repository.LiftResultRepository;
import org.bnabd.kuznia.repository.LiftTypeRepository;
import org.bnabd.kuznia.repository.UserRepository;
import org.bnabd.kuznia.web.dto.LiftResultRequest;
import org.bnabd.kuznia.web.dto.PowerliftingTotalResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LiftResultService {

	private final LiftResultRepository liftResultRepository;
	private final LiftTypeRepository liftTypeRepository;
	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public List<LiftResult> findByClient(Long clientId) {
		return liftResultRepository.findByClient_IdOrderByResultDateAsc(clientId);
	}

	@Transactional(readOnly = true)
	public List<LiftResult> findByClientAndLift(Long clientId, LiftTypeName liftTypeName) {
		return liftResultRepository.findByClient_IdAndLiftType_NameOrderByResultDateAsc(clientId, liftTypeName);
	}

	@Transactional
	public LiftResult addResult(Long clientId, LiftResultRequest request) {
		User client = userRepository.findById(clientId)
				.orElseThrow(() -> new DomainException("Nie znaleziono klienta."));
		LiftType liftType = liftTypeRepository.findByName(request.liftType())
				.orElseThrow(() -> new DomainException("Nie znaleziono typu boju."));

		LiftResult result = new LiftResult();
		result.setClient(client);
		result.setLiftType(liftType);
		result.setWeightKg(request.weightKg());
		result.setReps(request.reps());
		result.setEstimatedOneRepMax(calculateOneRepMax(request.weightKg(), request.reps()));
		result.setResultDate(request.resultDate());
		result.setNotes(request.notes());
		return liftResultRepository.save(result);
	}

	@Transactional(readOnly = true)
	public PowerliftingTotalResponse calculateTotal(Long clientId) {
		Map<LiftTypeName, BigDecimal> best = new EnumMap<>(LiftTypeName.class);
		findByClient(clientId).stream()
				.filter(result -> result.getEstimatedOneRepMax() != null)
				.forEach(result -> best.merge(
						result.getLiftType().getName(),
						result.getEstimatedOneRepMax(),
						BigDecimal::max
				));
		BigDecimal squat = best.getOrDefault(LiftTypeName.SQUAT, BigDecimal.ZERO);
		BigDecimal bench = best.getOrDefault(LiftTypeName.BENCH_PRESS, BigDecimal.ZERO);
		BigDecimal deadlift = best.getOrDefault(LiftTypeName.DEADLIFT, BigDecimal.ZERO);
		return new PowerliftingTotalResponse(squat, bench, deadlift, squat.add(bench).add(deadlift));
	}

	@Transactional(readOnly = true)
	public List<User> findClientsForTrainer(Long trainerId, List<org.bnabd.kuznia.domain.Reservation> reservations) {
		return reservations.stream()
				.map(org.bnabd.kuznia.domain.Reservation::getClient)
				.distinct()
				.sorted(Comparator.comparing(User::getLastName).thenComparing(User::getFirstName))
				.toList();
	}

	private BigDecimal calculateOneRepMax(BigDecimal weightKg, Integer reps) {
		if (reps <= 1) {
			return weightKg.setScale(2, RoundingMode.HALF_UP);
		}
		BigDecimal multiplier = BigDecimal.ONE.add(BigDecimal.valueOf(reps).divide(BigDecimal.valueOf(30), 4, RoundingMode.HALF_UP));
		return weightKg.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
	}
}
