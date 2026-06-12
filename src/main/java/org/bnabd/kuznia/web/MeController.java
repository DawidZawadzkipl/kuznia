package org.bnabd.kuznia.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bnabd.kuznia.domain.User;
import org.bnabd.kuznia.service.CurrentUserService;
import org.bnabd.kuznia.service.UserService;
import org.bnabd.kuznia.web.dto.UpdateProfileRequest;
import org.bnabd.kuznia.web.dto.UserResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {

	private final CurrentUserService currentUserService;
	private final UserService userService;
	private final DtoMapper mapper;

	@GetMapping
	public UserResponse me() {
		return mapper.toUserResponse(currentUserService.getCurrentUser());
	}

	@PutMapping
	public UserResponse updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
		User user = currentUserService.getCurrentUser();
		return mapper.toUserResponse(userService.updateProfile(user.getId(), request));
	}
}
