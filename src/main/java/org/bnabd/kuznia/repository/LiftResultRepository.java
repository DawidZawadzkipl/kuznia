package org.bnabd.kuznia.repository;

import java.util.List;
import org.bnabd.kuznia.domain.LiftResult;
import org.bnabd.kuznia.domain.LiftTypeName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LiftResultRepository extends JpaRepository<LiftResult, Long> {
	List<LiftResult> findByClient_IdOrderByResultDateAsc(Long clientId);

	List<LiftResult> findByClient_IdAndLiftType_NameOrderByResultDateAsc(Long clientId, LiftTypeName liftTypeName);
}
