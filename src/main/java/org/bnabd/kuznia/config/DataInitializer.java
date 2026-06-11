package org.bnabd.kuznia.config;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.bnabd.kuznia.domain.LiftType;
import org.bnabd.kuznia.domain.LiftTypeName;
import org.bnabd.kuznia.domain.Role;
import org.bnabd.kuznia.domain.RoleName;
import org.bnabd.kuznia.domain.TrainerSpecialization;
import org.bnabd.kuznia.domain.TrainingStation;
import org.bnabd.kuznia.domain.TrainingType;
import org.bnabd.kuznia.domain.User;
import org.bnabd.kuznia.repository.LiftTypeRepository;
import org.bnabd.kuznia.repository.RoleRepository;
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
		ensureRole(RoleName.TRAINER);
		ensureRole(RoleName.CLIENT);

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
}
