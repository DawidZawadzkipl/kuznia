package org.bnabd.kuznia.repository;

import java.util.Optional;
import org.bnabd.kuznia.domain.RoleName;
import org.bnabd.kuznia.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
	@Override
	@EntityGraph(attributePaths = "role")
	java.util.List<User> findAll();

	@EntityGraph(attributePaths = "role")
	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);

	long countByRole_Name(RoleName name);
}
