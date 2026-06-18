package org.bnabd.kuznia.web;

import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import org.bnabd.kuznia.service.DomainException;
import org.bnabd.kuznia.web.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(DomainException.class)
	public ResponseEntity<ErrorResponse> handleDomain(DomainException exception) {
		return error(HttpStatus.BAD_REQUEST, exception.getMessage());
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleBadCredentials() {
		return error(HttpStatus.UNAUTHORIZED, "Nieprawidlowy email lub haslo.");
	}

	@ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class, HttpMessageNotReadableException.class})
	public ResponseEntity<ErrorResponse> handleValidation(Exception exception) {
		return error(HttpStatus.BAD_REQUEST, "Nieprawidlowe dane wejsciowe.");
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception) {
		return error(HttpStatus.INTERNAL_SERVER_ERROR, "Wystapil nieoczekiwany blad serwera.");
	}

	private ResponseEntity<ErrorResponse> error(HttpStatus status, String message) {
		return ResponseEntity.status(status)
				.body(new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message));
	}
}
