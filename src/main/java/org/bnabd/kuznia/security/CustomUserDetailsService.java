package org.bnabd.kuznia.security;

import lombok.RequiredArgsConstructor;
import org.bnabd.kuznia.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) {
		return userRepository.findByEmail(username)
				.map(UserPrincipal::new)
				.orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono uzytkownika."));
	}
}
