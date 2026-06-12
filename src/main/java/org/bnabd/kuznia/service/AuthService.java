package org.bnabd.kuznia.service;

import lombok.RequiredArgsConstructor;
import org.bnabd.kuznia.domain.Role;
import org.bnabd.kuznia.domain.RoleName;
import org.bnabd.kuznia.domain.User;
import org.bnabd.kuznia.repository.RoleRepository;
import org.bnabd.kuznia.repository.UserRepository;
import org.bnabd.kuznia.security.JwtService;
import org.bnabd.kuznia.web.DtoMapper;
import org.bnabd.kuznia.web.dto.AuthResponse;
import org.bnabd.kuznia.web.dto.LoginRequest;
import org.bnabd.kuznia.web.dto.RegisterRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final DtoMapper mapper;

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new DomainException("Email jest juz zajety.");
		}

		Role clientRole = roleRepository.findByName(RoleName.CLIENT)
				.orElseThrow(() -> new DomainException("Brakuje roli CLIENT."));

		User user = new User();
		user.setRole(clientRole);
		user.setEmail(request.email());
		user.setPasswordHash(passwordEncoder.encode(request.password()));
		user.setFirstName(request.firstName());
		user.setLastName(request.lastName());
		user.setPhone(request.phone());

		User saved = userRepository.save(user);
		return new AuthResponse(jwtService.generateToken(saved), mapper.toUserResponse(saved));
	}

	@Transactional(readOnly = true)
	public AuthResponse login(LoginRequest request) {
		User user = userRepository.findByEmail(request.email())
				.orElseThrow(() -> new BadCredentialsException("Nieprawidlowy email lub haslo."));
		if (!user.isActive() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw new BadCredentialsException("Nieprawidlowy email lub haslo.");
		}
		return new AuthResponse(jwtService.generateToken(user), mapper.toUserResponse(user));
	}
}
