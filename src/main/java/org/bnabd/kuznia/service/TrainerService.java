package org.bnabd.kuznia.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.bnabd.kuznia.domain.Role;
import org.bnabd.kuznia.domain.RoleName;
import org.bnabd.kuznia.domain.TrainerProfile;
import org.bnabd.kuznia.domain.TrainerSpecialization;
import org.bnabd.kuznia.domain.User;
import org.bnabd.kuznia.repository.RoleRepository;
import org.bnabd.kuznia.repository.TrainerProfileRepository;
import org.bnabd.kuznia.repository.TrainerSpecializationRepository;
import org.bnabd.kuznia.repository.UserRepository;
import org.bnabd.kuznia.web.dto.TrainerProfileRequest;
import org.bnabd.kuznia.web.dto.TrainerRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TrainerService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final TrainerProfileRepository trainerProfileRepository;
	private final TrainerSpecializationRepository specializationRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional(readOnly = true)
	public List<TrainerProfile> findAll() {
		return trainerProfileRepository.findAll();
	}

	@Transactional(readOnly = true)
	public TrainerProfile findById(Long trainerId) {
		return trainerProfileRepository.findWithUserAndSpecializationsById(trainerId)
				.orElseThrow(() -> new DomainException("Nie znaleziono trenera."));
	}

	@Transactional(readOnly = true)
	public TrainerProfile findByUserId(Long userId) {
		return trainerProfileRepository.findByUser_Id(userId)
				.orElseThrow(() -> new DomainException("Nie znaleziono profilu trenera."));
	}

	@Transactional
	public TrainerProfile createTrainer(TrainerRequest request) {
		if (request.password() == null || request.password().isBlank()) {
			throw new DomainException("Haslo trenera jest wymagane.");
		}
		if (userRepository.existsByEmail(request.email())) {
			throw new DomainException("Email jest juz zajety.");
		}

		Role trainerRole = roleRepository.findByName(RoleName.TRAINER)
				.orElseThrow(() -> new DomainException("Brakuje roli TRAINER."));

		User user = new User();
		user.setRole(trainerRole);
		user.setEmail(request.email());
		user.setPasswordHash(passwordEncoder.encode(request.password()));
		user.setFirstName(request.firstName());
		user.setLastName(request.lastName());
		user.setPhone(request.phone());
		user.setActive(request.active() == null || request.active());
		userRepository.save(user);

		TrainerProfile profile = new TrainerProfile();
		profile.setUser(user);
		applyTrainerFields(
				profile,
				request.bio(),
				request.photoUrl(),
				request.experienceYears(),
				request.hourlyRate(),
				request.specializationIds()
		);
		return trainerProfileRepository.save(profile);
	}

	@Transactional
	public TrainerProfile updateTrainer(Long trainerId, TrainerRequest request) {
		TrainerProfile profile = findById(trainerId);
		User user = profile.getUser();

		if (!user.getEmail().equals(request.email()) && userRepository.existsByEmail(request.email())) {
			throw new DomainException("Email jest juz zajety.");
		}

		user.setEmail(request.email());
		user.setFirstName(request.firstName());
		user.setLastName(request.lastName());
		user.setPhone(request.phone());
		if (request.password() != null && !request.password().isBlank()) {
			user.setPasswordHash(passwordEncoder.encode(request.password()));
		}
		if (request.active() != null) {
			user.setActive(request.active());
		}

		applyTrainerFields(
				profile,
				request.bio(),
				request.photoUrl(),
				request.experienceYears(),
				request.hourlyRate(),
				request.specializationIds()
		);
		return profile;
	}

	@Transactional
	public TrainerProfile updateOwnProfile(Long trainerUserId, TrainerProfileRequest request) {
		TrainerProfile profile = findByUserId(trainerUserId);
		User user = profile.getUser();
		if (request.firstName() != null && !request.firstName().isBlank()) {
			user.setFirstName(request.firstName());
		}
		if (request.lastName() != null && !request.lastName().isBlank()) {
			user.setLastName(request.lastName());
		}
		user.setPhone(request.phone());
		applyTrainerFields(
				profile,
				request.bio(),
				profile.getPhotoUrl(),
				request.experienceYears(),
				request.hourlyRate(),
				request.specializationIds()
		);
		return profile;
	}

	private void applyTrainerFields(
			TrainerProfile profile,
			String bio,
			String photoUrl,
			Integer experienceYears,
			java.math.BigDecimal hourlyRate,
			Set<Long> specializationIds
	) {
		profile.setBio(bio);
		profile.setPhotoUrl(photoUrl);
		profile.setExperienceYears(experienceYears);
		profile.setHourlyRate(hourlyRate);
		profile.setSpecializations(loadSpecializations(specializationIds));
	}

	private Set<TrainerSpecialization> loadSpecializations(Set<Long> specializationIds) {
		List<TrainerSpecialization> specializations = specializationRepository.findAllById(specializationIds);
		if (specializations.size() != specializationIds.size()) {
			throw new DomainException("Nie znaleziono jednej ze specjalizacji.");
		}
		return new HashSet<>(specializations);
	}
}
