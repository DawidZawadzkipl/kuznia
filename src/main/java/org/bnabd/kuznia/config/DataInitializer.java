package org.bnabd.kuznia.config;

import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bnabd.kuznia.domain.LiftType;
import org.bnabd.kuznia.domain.LiftTypeName;
import org.bnabd.kuznia.domain.Role;
import org.bnabd.kuznia.domain.RoleName;
import org.bnabd.kuznia.domain.TrainerProfile;
import org.bnabd.kuznia.domain.TrainerSpecialization;
import org.bnabd.kuznia.domain.TrainingStation;
import org.bnabd.kuznia.domain.TrainingType;
import org.bnabd.kuznia.domain.User;
import org.bnabd.kuznia.repository.LiftTypeRepository;
import org.bnabd.kuznia.repository.RoleRepository;
import org.bnabd.kuznia.repository.TrainerProfileRepository;
import org.bnabd.kuznia.repository.TrainerSpecializationRepository;
import org.bnabd.kuznia.repository.TrainingStationRepository;
import org.bnabd.kuznia.repository.TrainingTypeRepository;
import org.bnabd.kuznia.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

	private final RoleRepository roleRepository;
	private final UserRepository userRepository;
	private final LiftTypeRepository liftTypeRepository;
	private final TrainerProfileRepository trainerProfileRepository;
	private final TrainerSpecializationRepository trainerSpecializationRepository;
	private final TrainingTypeRepository trainingTypeRepository;
	private final TrainingStationRepository trainingStationRepository;
	private final PasswordEncoder passwordEncoder;

	@Bean
	CommandLineRunner seedDatabase() {
		return args -> seed();
	}

	@Transactional
	void seed() {
		Role adminRole = ensureRole(RoleName.ADMIN);
		Role trainerRole = ensureRole(RoleName.TRAINER);
		Role clientRole = ensureRole(RoleName.CLIENT);

		ensureLiftType(LiftTypeName.SQUAT, "Przysiad");
		ensureLiftType(LiftTypeName.BENCH_PRESS, "Wyciskanie lezac");
		ensureLiftType(LiftTypeName.DEADLIFT, "Martwy ciag");

		ensureSpecialization("Przysiad", "Technika i programowanie przysiadu.");
		ensureSpecialization("Wyciskanie lezac", "Technika i progres w wyciskaniu lezac.");
		ensureSpecialization("Martwy ciag", "Technika i progres w martwym ciagu.");
		ensureSpecialization("Przygotowanie startowe", "Przygotowanie do zawodow trojbojowych.");

		ensureTrainingType("Trening techniczny", "Sesja techniczna z trenerem.", BigDecimal.valueOf(180));
		ensureTrainingType("Konsultacja", "Analiza techniki i plan dalszej pracy.", BigDecimal.valueOf(150));
		ensureTrainingType("Przygotowanie startowe", "Sesja przygotowujaca do startu.", BigDecimal.valueOf(220));

		ensureStation("Platforma 1");
		ensureStation("Platforma 2");
		ensureStation("Lawka startowa");
		ensureAdmin(adminRole);
		ensureClient(clientRole, "jan.kowalski@kuznia.local", "Client123!", "Jan", "Kowalski", "500100200");
		ensureClient(clientRole, "anna.nowak@kuznia.local", "Client123!", "Anna", "Nowak", "500300400");
		ensureTrainer(
				trainerRole,
				"marek.sila@kuznia.local",
				"Trainer123!",
				"Marek",
				"Sila",
				"501200300",
				"Specjalista od przysiadu i martwego ciagu. Pomaga budowac technike pod starty.",
				8,
				BigDecimal.valueOf(180),
				List.of("Przysiad", "Martwy ciag")
		);
		ensureTrainer(
				trainerRole,
				"ewa.lawka@kuznia.local",
				"Trainer123!",
				"Ewa",
				"Lawka",
				"501400500",
				"Trenerka wyciskania lezac i przygotowania startowego.",
				6,
				BigDecimal.valueOf(170),
				List.of("Wyciskanie lezac", "Przygotowanie startowe")
		);
	}

	private Role ensureRole(RoleName roleName) {
		return roleRepository.findByName(roleName).orElseGet(() -> {
			Role role = new Role();
			role.setName(roleName);
			return roleRepository.save(role);
		});
	}

	private void ensureLiftType(LiftTypeName name, String displayName) {
		liftTypeRepository.findByName(name).orElseGet(() -> {
			LiftType liftType = new LiftType();
			liftType.setName(name);
			liftType.setDisplayName(displayName);
			return liftTypeRepository.save(liftType);
		});
	}

	private void ensureSpecialization(String name, String description) {
		trainerSpecializationRepository.findByName(name).orElseGet(() -> {
			TrainerSpecialization specialization = new TrainerSpecialization();
			specialization.setName(name);
			specialization.setDescription(description);
			return trainerSpecializationRepository.save(specialization);
		});
	}

	private void ensureTrainingType(String name, String description, BigDecimal price) {
		trainingTypeRepository.findByName(name).orElseGet(() -> {
			TrainingType trainingType = new TrainingType();
			trainingType.setName(name);
			trainingType.setDescription(description);
			trainingType.setDurationMinutes(90);
			trainingType.setPrice(price);
			return trainingTypeRepository.save(trainingType);
		});
	}

	private void ensureStation(String name) {
		trainingStationRepository.findByName(name).orElseGet(() -> {
			TrainingStation station = new TrainingStation();
			station.setName(name);
			return trainingStationRepository.save(station);
		});
	}

	private void ensureAdmin(Role adminRole) {
		userRepository.findByEmail("admin@kuznia.local").orElseGet(() -> {
			User admin = new User();
			admin.setRole(adminRole);
			admin.setEmail("admin@kuznia.local");
			admin.setPasswordHash(passwordEncoder.encode("Admin123!"));
			admin.setFirstName("Admin");
			admin.setLastName("Kuznia");
			return userRepository.save(admin);
		});
	}

	private void ensureClient(Role clientRole, String email, String password, String firstName, String lastName, String phone) {
		userRepository.findByEmail(email).orElseGet(() -> {
			User client = new User();
			client.setRole(clientRole);
			client.setEmail(email);
			client.setPasswordHash(passwordEncoder.encode(password));
			client.setFirstName(firstName);
			client.setLastName(lastName);
			client.setPhone(phone);
			return userRepository.save(client);
		});
	}

	private void ensureTrainer(
			Role trainerRole,
			String email,
			String password,
			String firstName,
			String lastName,
			String phone,
			String bio,
			Integer experienceYears,
			BigDecimal hourlyRate,
			List<String> specializationNames
	) {
		User trainer = userRepository.findByEmail(email).orElseGet(() -> {
			User user = new User();
			user.setRole(trainerRole);
			user.setEmail(email);
			user.setPasswordHash(passwordEncoder.encode(password));
			user.setFirstName(firstName);
			user.setLastName(lastName);
			user.setPhone(phone);
			return userRepository.save(user);
		});

		trainerProfileRepository.findByUser_Id(trainer.getId()).orElseGet(() -> {
			TrainerProfile profile = new TrainerProfile();
			profile.setUser(trainer);
			profile.setBio(bio);
			profile.setExperienceYears(experienceYears);
			profile.setHourlyRate(hourlyRate);
			specializationNames.stream()
					.map(this::getSpecialization)
					.forEach(profile.getSpecializations()::add);
			return trainerProfileRepository.save(profile);
		});
	}

	private TrainerSpecialization getSpecialization(String name) {
		return trainerSpecializationRepository.findByName(name)
				.orElseThrow(() -> new IllegalStateException("Brakuje specjalizacji: " + name));
	}
}
