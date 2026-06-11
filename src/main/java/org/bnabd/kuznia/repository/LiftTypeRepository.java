package org.bnabd.kuznia.repository;

import java.util.Optional;
import org.bnabd.kuznia.domain.LiftType;
import org.bnabd.kuznia.domain.LiftTypeName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LiftTypeRepository extends JpaRepository<LiftType, Long> {
	Optional<LiftType> findByName(LiftTypeName name);
}
