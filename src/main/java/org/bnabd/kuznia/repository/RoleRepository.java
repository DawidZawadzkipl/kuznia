package org.bnabd.kuznia.repository;

import java.util.Optional;
import org.bnabd.kuznia.domain.Role;
import org.bnabd.kuznia.domain.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
	Optional<Role> findByName(RoleName name);
}
