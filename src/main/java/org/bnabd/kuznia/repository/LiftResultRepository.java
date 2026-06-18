package org.bnabd.kuznia.repository;

import java.util.List;
import org.bnabd.kuznia.domain.LiftResult;
import org.bnabd.kuznia.domain.LiftTypeName;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LiftResultRepository extends JpaRepository<LiftResult, Long> {
	@EntityGraph(attributePaths = {"client", "liftType"})
	List<LiftResult> findByClient_IdOrderByResultDateAsc(Long clientId);

	@EntityGraph(attributePaths = {"client", "liftType"})
	List<LiftResult> findByClient_IdAndLiftType_NameOrderByResultDateAsc(Long clientId, LiftTypeName liftTypeName);
}
