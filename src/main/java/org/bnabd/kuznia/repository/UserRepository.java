package org.bnabd.kuznia.repository;

import java.util.Optional;
import org.bnabd.kuznia.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);
}
