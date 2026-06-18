package org.bnabd.kuznia.web;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;
import org.bnabd.kuznia.service.DomainException;
import org.bnabd.kuznia.web.dto.UploadResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/uploads")
public class UploadController {

	private final Path uploadDirectory;

	public UploadController(@Value("${app.upload-dir:uploads}") String uploadDirectory) {
		this.uploadDirectory = Path.of(uploadDirectory);
	}

	@PostMapping(value = "/trainer-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public UploadResponse uploadTrainerPhoto(@RequestParam("file") MultipartFile file) {
		if (file.isEmpty()) {
			throw new DomainException("Plik ze zdjeciem jest pusty.");
		}
		String contentType = file.getContentType();
		if (contentType != null && !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
			throw new DomainException("Mozna przeslac tylko plik graficzny.");
		}

		Path trainerDirectory = uploadDirectory.resolve("trainers").normalize();
		String filename = UUID.randomUUID() + extension(file.getOriginalFilename());
		Path target = trainerDirectory.resolve(filename).normalize();
		if (!target.startsWith(trainerDirectory)) {
			throw new DomainException("Nieprawidlowa nazwa pliku.");
		}

		try {
			Files.createDirectories(trainerDirectory);
			file.transferTo(target);
		} catch (IOException exception) {
			throw new DomainException("Nie udalo sie zapisac zdjecia trenera.");
		}

		return new UploadResponse("/uploads/trainers/" + filename);
	}

	private String extension(String originalFilename) {
		if (originalFilename == null) {
			return "";
		}
		int dotIndex = originalFilename.lastIndexOf('.');
		if (dotIndex < 0 || dotIndex == originalFilename.length() - 1) {
			return "";
		}
		String extension = originalFilename.substring(dotIndex).toLowerCase(Locale.ROOT);
		return extension.matches("\\.[a-z0-9]{1,10}") ? extension : "";
	}
}
