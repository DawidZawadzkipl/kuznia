package org.bnabd.kuznia.service;

import lombok.RequiredArgsConstructor;
import org.bnabd.kuznia.domain.User;
import org.bnabd.kuznia.repository.UserRepository;
import org.bnabd.kuznia.web.dto.UpdateProfileRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;

	@Transactional
	public User updateProfile(Long userId, UpdateProfileRequest request) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new DomainException("Nie znaleziono uzytkownika."));
		user.setFirstName(request.firstName());
		user.setLastName(request.lastName());
		user.setPhone(request.phone());
		return user;
	}

	@Transactional
	public User setActive(Long userId, boolean active) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new DomainException("Nie znaleziono uzytkownika."));
		user.setActive(active);
		return user;
	}
}
